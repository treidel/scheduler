package com.jebussystems.leaguescheduler.visitorgameschedulerjob;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InvalidJobConfException;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import com.google.gson.reflect.TypeToken;
import com.jebussystems.leaguescheduler.combo.ComboFinalizer;
import com.jebussystems.leaguescheduler.combo.ComboUtils;
import com.jebussystems.leaguescheduler.entities.GameOffer;
import com.jebussystems.leaguescheduler.entities.ScheduleEntry;
import com.jebussystems.leaguescheduler.entities.Serializer;
import com.jebussystems.leaguescheduler.entities.Team;
import com.jebussystems.leaguescheduler.entities.TeamSchedule;
import com.jebussystems.leaguescheduler.filters.MustPlayEveryHomeTeamFilter;
import com.jebussystems.leaguescheduler.filters.OneGamePerHomeTeamFilter;
import com.jebussystems.leaguescheduler.filters.SameDayNotAllowedFilter;
import com.jebussystems.leaguescheduler.filters.ScheduleFilter;
import com.jebussystems.leaguescheduler.filters.ScheduleFilterBase;
import com.jebussystems.leaguescheduler.filters.TeamBlackoutFilter;
import com.jebussystems.leaguescheduler.filters.UniqueGameslotsFilter;

public class VisitorGameSchedulerReducer extends MapReduceBase implements Reducer<Text, Text, NullWritable, Text> {

	private final Collection<ScheduleFilterBase<TeamSchedule>> filters = new LinkedList<>();
	private Map<String, Team> teamLookup = null;
	private Integer maximumGamesPerOpponent = null;

	public VisitorGameSchedulerReducer() {
		this.filters.add(new UniqueGameslotsFilter());
		this.filters.add(new OneGamePerHomeTeamFilter());
		this.filters.add(new MustPlayEveryHomeTeamFilter());
		this.filters.add(new SameDayNotAllowedFilter());
		this.filters.add(new TeamBlackoutFilter());
	}

	@Override
	public void configure(JobConf job) {
		// do whatever the base class does
		super.configure(job);
		// configure our filters
		for (ScheduleFilterBase<TeamSchedule> filter : filters) {
			filter.configure(job);
		}
		// parse the teams
		Type collectionType = new TypeToken<Collection<Team>>() {
		}.getType();
		Collection<Team> teams = Serializer.GSON.fromJson(job.get(Team.TEAMS_PROPERTY), collectionType);
		// setup the teams lookup
		this.teamLookup = new HashMap<>();
		// populate the list
		for (Team teamEntity : teams) {
			this.teamLookup.put(teamEntity.getId(), teamEntity);
		}
		// get the max games per opponent count with default = 1
		this.maximumGamesPerOpponent = job.getInt(VisitorGameSchedulerDriver.MAXIMUM_GAMES_PER_OPPONENT_PROPERTY, 1);
	}

	@Override
	public void reduce(Text key, Iterator<Text> values, OutputCollector<NullWritable, Text> output, Reporter reporter)
			throws IOException {
		// find the team
		Team team = teamLookup.get(key.toString());
		if (null == team) {
			throw new InvalidJobConfException("unknown team=" + key.toString());
		}
		// create a map of available game offers
		Map<String, List<GameOffer>> index = new HashMap<>();
		while (true == values.hasNext()) {
			GameOffer gameoffer = Serializer.GSON.fromJson(values.next().toString(), GameOffer.class);
			List<GameOffer> offers = index.get(gameoffer.getTeam());
			if (null == offers) {
				offers = new LinkedList<>();
				index.put(gameoffer.getTeam(), offers);
			}
			offers.add(gameoffer);
		}

		// create a flat list of offers per team
		List<List<GameOffer>> offersList = new ArrayList<>(index.values());
		// create a new list sized to the number of teams
		List<Collection<Collection<GameOffer>>> offersCombo = new ArrayList<>(index.size());
		for (int i = 0; i < offersList.size(); i++) {
			List<GameOffer> offers = offersList.get(i);
			Collection<Collection<GameOffer>> combos = generateAllGameOffersCombosForTeam(offers);
			offersCombo.add(combos);
		}

		// create the finalizer
		ComboFinalizer<Collection<GameOffer>> finalizer = new ComboFinalizer<Collection<GameOffer>>() {

			@Override
			public void finalize(List<Collection<GameOffer>> values) throws IOException {
				// create a list to hold the schedule
				List<ScheduleEntry> scheduleEntryList = new ArrayList<>(values.size() * maximumGamesPerOpponent);
				for (Collection<GameOffer> offers : values) {
					for (GameOffer offer : offers) {
						// create schedule entry
						ScheduleEntry scheduleEntry = new ScheduleEntry(offer.getTeam(), team.getId(),
								offer.getGameSlot());
						scheduleEntryList.add(scheduleEntry);
					}
				}
				// sort the schedule
				scheduleEntryList
						.sort((left, right) -> left.getGameSlot().getId().compareTo(right.getGameSlot().getId()));

				// create the schedule
				MessageDigest digest = DigestUtils.getMd5Digest();
				for (ScheduleEntry entry : scheduleEntryList) {
					digest.digest(entry.getGameSlot().getId().getBytes());
				}
				String hashText = Hex.encodeHexString(digest.digest());
				TeamSchedule teamSchedule = new TeamSchedule(hashText, team.getId(), scheduleEntryList);

				// evaluate the filters
				if (false == evaluateFilters(teamSchedule)) {
					return;
				}
				// create the output json and write it
				String json = Serializer.GSON.toJson(teamSchedule);
				output.collect(null, new Text(json));
				// tell the framework we're alive
				reporter.progress();
			};

		};
		// iterate through all combinations
		List<Collection<GameOffer>> list = new ArrayList<>(index.size());
		ComboUtils.combinations(0, index.size(), offersCombo, list, finalizer);
	}

	private Collection<Collection<GameOffer>> generateAllGameOffersCombosForTeam(List<GameOffer> teamOffers) {
		Collection<Collection<GameOffer>> comboOffers = new LinkedList<>();
		for (int i = 0; i < this.maximumGamesPerOpponent; i++) {
			// get the array of index combinations
			Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(teamOffers.size(), i + 1);
			while (true == iterator.hasNext()) {
				// get the next set of indexes
				int[] indexes = iterator.next();
				// setup an array to take the values
				GameOffer values[] = new GameOffer[i + 1];
				// iterate through each set of indexes and populate the offers
				for (int j = 0; j < indexes.length; j++) {
					int index = indexes[j];
					values[j] = teamOffers.get(index);
				}
				// wrap as a collection
				Collection<GameOffer> comboOffer = Arrays.asList(values);
				// add to the crazy collection we return
				comboOffers.add(comboOffer);
			}
		}
		return comboOffers;
	}

	private boolean evaluateFilters(TeamSchedule schedule) {
		// go through each filter and check this home schedule for exclusion criteria
		for (ScheduleFilter<TeamSchedule> filter : this.filters) {
			if (false == filter.accept(schedule)) {
				return false;
			}
		}
		return true;
	}

}
