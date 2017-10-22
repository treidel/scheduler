package com.jebussystems.leaguescheduler.entities;

import java.util.Collection;

public abstract class Schedule {

	private final String id;
	private final Collection<ScheduleEntry> scheduleEntries;

	protected Schedule(String id, Collection<ScheduleEntry> scheduleEntries) {
		this.id = id;
		this.scheduleEntries = scheduleEntries;
	}

	public String getId() {
		return id;
	}

	public Collection<ScheduleEntry> getScheduleEntries() {
		return scheduleEntries;
	}

}
