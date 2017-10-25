package com.jebussystems.leaguescheduler.filters;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Collection;

import org.apache.hadoop.mapred.JobConf;

import com.google.gson.reflect.TypeToken;
import com.jebussystems.leaguescheduler.entities.ScheduleEntry;
import com.jebussystems.leaguescheduler.entities.Serializer;
import com.jebussystems.leaguescheduler.entities.TeamBlackout;
import com.jebussystems.leaguescheduler.entities.TeamSchedule;

public class TeamBlackoutFilter extends ScheduleFilterBase<TeamSchedule> implements ScheduleFilter<TeamSchedule> {

	private Collection<TeamBlackout> blackoutList = null;

	@Override
	public void configure(JobConf job) {
		// see if we have a blackout configured
		String json = job.get(TeamBlackout.TEAM_BLACKOUTS_PROPERTY);
		if (null != json) {
			// parse the teams
			Type collectionType = new TypeToken<Collection<TeamBlackout>>() {
			}.getType();
			// store the list
			this.blackoutList = Serializer.GSON.fromJson(json, collectionType);

		}
	}

	@Override
	public boolean accept(TeamSchedule schedule) {
		if (null != blackoutList) {
			// go through all blackouts
			for (TeamBlackout blackout : this.blackoutList) {
				// go through all schedule entries
				for (ScheduleEntry scheduleEntry : schedule.getScheduleEntries()) {
					// if this isn't the right team then skip
					if ((false == blackout.getTeam().equals(scheduleEntry.getHomeTeam()))
							&& (false == blackout.getTeam().equals(scheduleEntry.getAwayTeam()))) {
						continue;
					}
					// see if this is a blackout day
					Calendar gameslotCalendar = Calendar.getInstance();
					gameslotCalendar.setTime(scheduleEntry.getGameSlot().getTime());
					Calendar blackoutCalendar = Calendar.getInstance();
					blackoutCalendar.setTime(blackout.getDate());
					boolean sameDay = gameslotCalendar.get(Calendar.YEAR) == blackoutCalendar.get(Calendar.YEAR)
							&& gameslotCalendar.get(Calendar.DAY_OF_YEAR) == blackoutCalendar.get(Calendar.DAY_OF_YEAR);
					if (true == sameDay) {
						// this is a blackout day so return the schedule
						return false;
					}
				}
			}
		}
		// if we get here the schedule is good
		return true;
	}
}
