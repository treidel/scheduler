package com.jebussystems.leaguescheduler.homegameschedulerjob;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

import com.google.gson.reflect.TypeToken;
import com.jebussystems.leaguescheduler.entities.Team;

public class HomeGameSchedulerDriver {

	private static final String JOB_NAME = "HOME_GAME_SCHEDULER_JOB";
	static final String TEAMS_PROPERTY = "TEAMS";

	public static void main(String[] args) throws Exception {

		// create the job configuraton
		JobConf conf = new JobConf(HomeGameSchedulerDriver.class);
		// set the basic job info
		conf.setJobName(JOB_NAME);
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		// read in and parse the list of teams
		Reader reader = new FileReader(new File(args[0]));
		Type collectionType = new TypeToken<Collection<Team>>() {
		}.getType();
		Collection<Team> teams = Team.GSON.fromJson(reader, collectionType);

		// store the list of teams in the job context
		conf.set(TEAMS_PROPERTY, Team.GSON.toJson(teams));

		// configure the mapper + reducer classes
		conf.setMapperClass(HomeGameSchedulerMapper.class);
		conf.setReducerClass(HomeGameSchedulerReducer.class);

		// configure the input + output formats
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		// configure the input + output locations
		FileInputFormat.addInputPath(conf, new Path(args[1]));
		FileOutputFormat.setOutputPath(conf, new Path(args[2]));

		JobClient.runJob(conf);
	}

}
