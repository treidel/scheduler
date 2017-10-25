package com.jebussystems.leaguescheduler.visitorgameschedulerjob;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InvalidJobConfException;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import com.google.gson.reflect.TypeToken;
import com.jebussystems.leaguescheduler.entities.GameOffer;
import com.jebussystems.leaguescheduler.entities.GameSlot;
import com.jebussystems.leaguescheduler.entities.Serializer;
import com.jebussystems.leaguescheduler.entities.Team;

public class VisitorGameSchedulerMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

	private Map<String, Team> teamLookup = null;

	@Override
	public void configure(JobConf job) {
		// do whatever the base class does
		super.configure(job);
		// parse the teams
		Type collectionType = new TypeToken<Collection<Team>>() {
		}.getType();
		String json = job.get(Team.TEAMS_PROPERTY);
		Collection<Team> teamEntities = Serializer.GSON.fromJson(json, collectionType);
		// setup the teams lookup
		this.teamLookup = new HashMap<>();
		// populate the list
		for (Team teamEntity : teamEntities) {
			this.teamLookup.put(teamEntity.getId(), teamEntity);
		}
	}

	@Override
	public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
			throws IOException {
		// get the raw line
		String line = value.toString();
		// parse to an object
		GameSlot gameslot = Serializer.GSON.fromJson(line, GameSlot.class);
		// iterate for all teams this gameslot is available to
		for (String id : gameslot.getAvailability()) {
			// validate the team availability values
			if (false == this.teamLookup.containsKey(id)) {
				throw new InvalidJobConfException("unknown team=" + id + " for availability=" + gameslot.getId());
			}
			// create the game offer
			GameOffer gameOffer = new GameOffer(id, gameslot);
			// create the output value
			String json = Serializer.GSON.toJson(gameOffer);
			Text outputValue = new Text(json);
			// publish this availability to all teams including those that it's available to
			for (Team team : this.teamLookup.values()) {
				// don't the offer send to ourselves
				if (true == team.getId().equals(id)) {
					continue;
				}
				// create the output key
				Text outputKey = new Text(team.getId());
				// emit
				output.collect(outputKey, outputValue);
			}
		}
	}
}
