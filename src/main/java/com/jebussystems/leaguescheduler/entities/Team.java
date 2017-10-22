package com.jebussystems.leaguescheduler.entities;

import com.google.gson.Gson;

public class Team implements Comparable<Team> {

	public static final String TEAMS_PROPERTY = "TEAMS";

	public static final Gson GSON = new Gson();

	private final String id;
	private final String name;

	public Team(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public int compareTo(Team entity) {
		return entity.id.compareTo(this.id);
	}

	@Override
	public String toString() {
		return "[" + "id=" + id + ",name=" + name + "]";
	}
}
