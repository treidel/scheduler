package com.jebussystems.leaguescheduler.visitorgamefilterjob;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import com.jebussystems.leaguescheduler.entities.Serializer;
import com.jebussystems.leaguescheduler.entities.TeamSchedule;

public class VisitorGameFilterMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

	@Override
	public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
			throws IOException {
		// get the raw line
		String line = value.toString();
		// parse to an visitor schedule
		TeamSchedule visitorSchedule = Serializer.GSON.fromJson(line, TeamSchedule.class);
		// emit the schedule
		output.collect(new Text(visitorSchedule.getId()), value);
	}
}
