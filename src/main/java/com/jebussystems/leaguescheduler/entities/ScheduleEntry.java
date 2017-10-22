package com.jebussystems.leaguescheduler.entities;

import com.google.gson.Gson;

public class ScheduleEntry {
	public static final Gson GSON = new Gson();

	private final String hometeam;
	private final String awayteam;
	private final GameSlot gameslot;

	public ScheduleEntry(String hometeam, String awayteam, GameSlot gameslot) {
		this.hometeam = hometeam;
		this.awayteam = awayteam;
		this.gameslot = gameslot;
	}

	public String getHomeTeam() {
		return hometeam;
	}

	public String getAwayTeam() {
		return awayteam;
	}

	public GameSlot getGameSlot() {
		return gameslot;
	}

	@Override
	public String toString() {
		return "[" + "hometeam=" + hometeam + ",awayteam=" + awayteam + ",gameslot=" + gameslot.toString() + "]";
	}
}
