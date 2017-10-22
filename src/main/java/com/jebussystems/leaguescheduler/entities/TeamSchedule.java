package com.jebussystems.leaguescheduler.entities;

import java.util.Collection;

import com.google.gson.Gson;

public class TeamSchedule extends Schedule {
	public static final Gson GSON = new Gson();

	private final String team;

	public TeamSchedule(String id, String team, Collection<ScheduleEntry> scheduleEntries) {
		super(id, scheduleEntries);
		this.team = team;
	}

	public String getTeam() {
		return team;
	}

	@Override
	public String toString() {
		return "[" + "id=" + getId() + ",team=" + team + ",scheduleentries=" + getScheduleEntries().toString() + "]";
	}
}
