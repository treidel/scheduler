package com.jebussystems.leaguescheduler.visitorgameschedulerjob;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InvalidJobConfException;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import com.google.gson.reflect.TypeToken;
import com.jebussystems.leaguescheduler.entities.GameOffer;
import com.jebussystems.leaguescheduler.entities.GameSlot;
import com.jebussystems.leaguescheduler.entities.ScheduleEntry;
import com.jebussystems.leaguescheduler.entities.Team;
import com.jebussystems.leaguescheduler.entities.TeamSchedule;

public class VisitorGameSchedulerReducer extends MapReduceBase implements Reducer<Text, Text, NullWritable, Text> {

	private Map<String, Team> teamLookup = null;

	@Override
	public void configure(JobConf job) {
		// do whatever the base class does
		super.configure(job);
		// parse the teams
		Type collectionType = new TypeToken<Collection<Team>>() {
		}.getType();
		Collection<Team> teamEntities = Team.GSON.fromJson(job.get(Team.TEAMS_PROPERTY), collectionType);
		// setup the teams lookup
		this.teamLookup = new HashMap<>();
		// populate the list
		for (Team teamEntity : teamEntities) {
			this.teamLookup.put(teamEntity.getId(), teamEntity);
		}
	}

	@Override
	public void reduce(Text key, Iterator<Text> values, OutputCollector<NullWritable, Text> output, Reporter reporter)
			throws IOException {
		// find the team
		Team team = teamLookup.get(key.toString());
		if (null == team) {
			throw new InvalidJobConfException("unknown team=" + key.toString());
		}
		// create a list of available game offers
		List<GameOffer> availabilityLists = new LinkedList<>();
		while (true == values.hasNext()) {
			GameOffer gameoffer = GameSlot.GSON.fromJson(values.next().toString(), GameOffer.class);
			availabilityLists.add(gameoffer);
		}
		// iterate through all possible combinations
		for (int i = 1; i <= availabilityLists.size(); i++) {
			// create the schedule array that will contain a unique list of schedule entries
			ScheduleEntry scheduleList[] = new ScheduleEntry[i];
			// iterate through all possible schedules
			combinations(team.getId(), availabilityLists, scheduleList.length, 0, scheduleList, output);
		}
	}

	private void combinations(String team, List<GameOffer> arr, int len, int startPosition, ScheduleEntry[] result,
			OutputCollector<NullWritable, Text> output) throws IOException {
		if (len == 0) {
			// create a list to sort and sort by game slot id
			List<ScheduleEntry> scheduleEntries = new LinkedList<>(Arrays.asList(result));
			scheduleEntries.sort((left, right) -> left.getGameSlot().getId().compareTo(right.getGameSlot().getId()));
			String hashText = TeamSchedule.GSON.toJson(scheduleEntries);
			// create the visiting schedule
			TeamSchedule schedule = new TeamSchedule(DigestUtils.md5Hex(hashText), team, scheduleEntries);
			// serialize the schedule
			String json = TeamSchedule.GSON.toJson(schedule);
			// emit
			Text value = new Text(json);
			output.collect(NullWritable.get(), value);
			return;
		}
		for (int i = startPosition; i <= arr.size() - len; i++) {
			GameOffer gameoffer = arr.get(i);
			ScheduleEntry scheduleEntry = new ScheduleEntry(gameoffer.getTeam(), team, gameoffer.getGameSlot());
			result[result.length - len] = scheduleEntry;
			combinations(team, arr, len - 1, i + 1, result, output);
		}
	}

}
