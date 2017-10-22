package com.jebussystems.leaguescheduler.entities;

public class GameOffer {
	private final String team;
	private final GameSlot gameslot;

	public GameOffer(String team, GameSlot gameslot) {
		this.team = team;
		this.gameslot = gameslot;
	}

	public String getTeam() {
		return team;
	}

	public GameSlot getGameSlot() {
		return gameslot;
	}

	@Override
	public String toString() {
		return "[" + "team=" + team + ",gameslot=" + gameslot + "]";
	}
}
