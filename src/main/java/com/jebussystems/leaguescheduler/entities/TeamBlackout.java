package com.jebussystems.leaguescheduler.entities;

import java.util.Date;

public class TeamBlackout {
	public static final String TEAM_BLACKOUT_PROPERTY = "TEAM_BLACKOUTS";

	private final String team;
	private final Date day;

	public TeamBlackout(String team, Date day) {
		this.team = team;
		this.day = day;
	}

	public String getTeam() {
		return team;
	}

	public Date getDay() {
		return day;
	}

	@Override
	public String toString() {
		return "[" + "team=" + team + ",day=" + day.toString() + "]";
	}
}
