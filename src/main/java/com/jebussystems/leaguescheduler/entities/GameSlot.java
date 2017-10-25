package com.jebussystems.leaguescheduler.entities;

import java.util.Collection;
import java.util.Date;

public class GameSlot implements Comparable<GameSlot> {

	private final String id;
	private final Collection<String> availability;
	private final Date time;
	private final String location;

	public GameSlot(String id, Collection<String> availability, Date time, String location) {
		this.id = id;
		this.availability = availability;
		this.time = time;
		this.location = location;
	}

	public String getId() {
		return id;
	}

	public Collection<String> getAvailability() {
		return availability;
	}

	public Date getTime() {
		return time;
	}

	public String getLocation() {
		return location;
	}

	@Override
	public int compareTo(GameSlot entity) {
		return entity.id.compareTo(this.id);
	}

	@Override
	public String toString() {
		return "[" + "id=" + id + ",availability=" + availability + ",time=" + time.toString() + ",location=" + location
				+ "]";
	}
}
