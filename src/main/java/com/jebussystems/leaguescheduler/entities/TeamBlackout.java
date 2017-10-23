package com.jebussystems.leaguescheduler.entities;

import java.util.Date;

public class TeamBlackout {
	public static final String TEAM_BLACKOUTS_PROPERTY = "TEAM_BLACKOUTS";

	private final String team;
	private final Date date;

	public TeamBlackout(String team, Date date) {
		this.team = team;
		this.date = date;
	}

	public String getTeam() {
		return team;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public String toString() {
		return "[" + "team=" + team + ",date=" + date.toString() + "]";
	}
}
