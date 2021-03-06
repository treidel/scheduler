package com.jebussystems.leaguescheduler.filters;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.mapred.JobConf;

import com.google.gson.reflect.TypeToken;
import com.jebussystems.leaguescheduler.entities.TeamSchedule;
import com.jebussystems.leaguescheduler.entities.ScheduleEntry;
import com.jebussystems.leaguescheduler.entities.Serializer;
import com.jebussystems.leaguescheduler.entities.Team;

public class OneGamePerHomeTeamFilter extends ScheduleFilterBase<TeamSchedule> implements ScheduleFilter<TeamSchedule> {
	protected Map<String, Team> teamLookup = null;

	@Override
	public void configure(JobConf job) {
		// parse the teams
		Type collectionType = new TypeToken<Collection<Team>>() {
		}.getType();
		Collection<Team> teamEntities = Serializer.GSON.fromJson(job.get(Team.TEAMS_PROPERTY), collectionType);
		// setup the teams lookup
		this.teamLookup = new HashMap<>();
		// populate the list
		for (Team teamEntity : teamEntities) {
			this.teamLookup.put(teamEntity.getId(), teamEntity);
		}
	}

	@Override
	public boolean accept(TeamSchedule schedule) {
		// make a set of all of the teams
		Set<String> teamSet = new HashSet<>();
		// iterate through all schedule entries
		for (ScheduleEntry scheduleEntry : schedule.getScheduleEntries()) {
			// remove the visitor team from the set
			if (false == teamSet.add(scheduleEntry.getHomeTeam())) {
				// already playing this team
				return false;
			}
		}
		// if we get here we've only played each team once
		return true;
	}

}
