package com.jebussystems.leaguescheduler.totalgameschedulerjob;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.log4j.Logger;

import com.google.gson.reflect.TypeToken;
import com.jebussystems.leaguescheduler.entities.Team;

public class TotalGameSchedulerDriver {

	private static final Logger LOGGER = Logger.getLogger(TotalGameSchedulerDriver.class);

	private static final String JOB_NAME = "TOTAL_GAME_SCHEDULER_JOB";
	private static final String INPUT_PARAM = "input";
	private static final String OUTPUT_PARAM = "output";
	private static final String TEAMS_PARAM = "teams";

	public static void main(String[] args) throws Exception {

		// setup the command line options
		Options options = new Options();
		options.addOption(INPUT_PARAM, true, "input file(s)");
		options.addOption(OUTPUT_PARAM, true, "output folder");
		options.addOption(TEAMS_PARAM, true, "JSON file containing the input teams");

		// parse the comand line
		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse(options, args);

		// extract the command line params we need
		String inputFiles = cmd.getOptionValue(INPUT_PARAM);
		if (null == inputFiles) {
			LOGGER.error(INPUT_PARAM + " parameter not provided");
			return;
		}
		String outputFiles = cmd.getOptionValue(OUTPUT_PARAM);
		if (null == outputFiles) {
			LOGGER.error(OUTPUT_PARAM + " parameter not provided");
			return;
		}
		String teamsFile = cmd.getOptionValue(TEAMS_PARAM);
		if (null == teamsFile) {
			LOGGER.error(TEAMS_PARAM + " parameter not provided");
			return;
		}

		// create the job configuraton
		JobConf conf = new JobConf(TotalGameSchedulerDriver.class);
		// set the basic job info
		conf.setJobName(JOB_NAME);
		conf.setOutputKeyClass(NullWritable.class);
		conf.setOutputValueClass(Text.class);

		// read in and parse the list of teams
		Reader reader = new FileReader(new File(teamsFile));
		Type collectionType = new TypeToken<Collection<Team>>() {
		}.getType();
		Collection<Team> teams = Team.GSON.fromJson(reader, collectionType);

		// store the list of teams in the job context
		conf.set(Team.TEAMS_PROPERTY, Team.GSON.toJson(teams));

		// configure the mapper + reducer classes
		conf.setMapperClass(TotalGameSchedulerMapper.class);
		conf.setReducerClass(TotalGameSchedulerReducer.class);

		// configure the input + output formats
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		// configure the input + output locations
		FileInputFormat.addInputPath(conf, new Path(inputFiles));
		FileOutputFormat.setOutputPath(conf, new Path(outputFiles));

		JobClient.runJob(conf);
	}

}
