package com.jebussystems.leaguescheduler.totalgameschedulerjob;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import com.google.gson.reflect.TypeToken;
import com.jebussystems.leaguescheduler.entities.ScheduleEntry;
import com.jebussystems.leaguescheduler.entities.Team;
import com.jebussystems.leaguescheduler.entities.TeamSchedule;
import com.jebussystems.leaguescheduler.entities.TotalSchedule;

public class TotalGameSchedulerReducer extends MapReduceBase
		implements Reducer<NullWritable, Text, NullWritable, Text> {

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

	public void reduce(NullWritable key, Iterator<Text> values, OutputCollector<NullWritable, Text> output,
			Reporter reporter) throws IOException {
		// create an index of home schedules provided for every team
		Map<String, Collection<TeamSchedule>> index = new HashMap<>();
		// go through all schedules
		while (values.hasNext()) {
			// decode the game slot
			TeamSchedule schedule = TeamSchedule.GSON.fromJson(values.next().toString(), TeamSchedule.class);
			Collection<TeamSchedule> schedules = index.get(schedule.getTeam());
			if (null == schedules) {
				schedules = new LinkedList<>();
				index.put(schedule.getTeam(), schedules);
			}
			schedules.add(schedule);
		}
		// get a flat list of each team's home schedules
		List<Collection<TeamSchedule>> list = new ArrayList<>(index.values());
		List<TeamSchedule> combo = new ArrayList<>(list.size());
		combinations(0, index.size(), list, combo, new Finalizer<List<TeamSchedule>>() {

			@Override
			public void finalize(List<TeamSchedule> value) throws IOException {
				// create the total schedule entries
				List<ScheduleEntry> scheduleEntries = new LinkedList<>();
				for (TeamSchedule schedule : value) {
					// go through all entries
					for (ScheduleEntry scheduleEntry : schedule.getScheduleEntries()) {
						// create the home schedule entry
						scheduleEntries.add(scheduleEntry);
					}
				}
				// sort the schedule entries
				scheduleEntries
						.sort((left, right) -> left.getGameSlot().getId().compareTo(right.getGameSlot().getId()));
				String hashText = TeamSchedule.GSON.toJson(scheduleEntries);
				// create the new total schedule
				TotalSchedule schedule = new TotalSchedule(DigestUtils.md5Hex(hashText), scheduleEntries);
				// serialize and emit
				String json = TotalSchedule.GSON.toJson(schedule);
				output.collect(null, new Text(json));
			};

		});
	}

	private void combinations(int index, int length, List<Collection<TeamSchedule>> list, List<TeamSchedule> combo,
			Finalizer<List<TeamSchedule>> finalizer) throws IOException {
		if (index == length) {
			// complete combination found
			finalizer.finalize(combo);
			return;
		}
		// go through all values
		Collection<TeamSchedule> values = list.get(index);
		for (TeamSchedule value : values) {
			// set the value
			combo.add(value);
			// recurse for the next team
			combinations(index + 1, length, list, combo, finalizer);
			combo.remove(index);
		}
	}

	private static interface Finalizer<T> {
		void finalize(T value) throws IOException;
	}
}
