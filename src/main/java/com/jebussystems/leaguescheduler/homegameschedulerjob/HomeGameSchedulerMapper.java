package com.jebussystems.leaguescheduler.homegameschedulerjob;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import com.jebussystems.leaguescheduler.entities.ScheduleEntry;
import com.jebussystems.leaguescheduler.entities.Serializer;
import com.jebussystems.leaguescheduler.entities.TeamSchedule;

public class HomeGameSchedulerMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

	@Override
	public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
			throws IOException {
		// get the raw line
		String line = value.toString();
		// parse to an visitor schedule
		TeamSchedule visitorSchedule = Serializer.GSON.fromJson(line, TeamSchedule.class);
		// iterate through and get a list of all the teams involved
		Set<String> teams = new HashSet<>(visitorSchedule.getScheduleEntries().size());
		for (ScheduleEntry scheduleEntryEntity : visitorSchedule.getScheduleEntries()) {
			teams.add(scheduleEntryEntity.getHomeTeam());
		}
		// send the schedule to all home teams
		for (ScheduleEntry scheduleEntry : visitorSchedule.getScheduleEntries()) {
			output.collect(new Text(scheduleEntry.getHomeTeam()), value);
		}
	}
}
