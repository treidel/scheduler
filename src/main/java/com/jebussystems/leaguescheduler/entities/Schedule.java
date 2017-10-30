package com.jebussystems.leaguescheduler.entities;

import java.util.List;

public abstract class Schedule {

	private final String id;
	private final List<ScheduleEntry> scheduleEntries;

	protected Schedule(String id, List<ScheduleEntry> scheduleEntries) {
		this.id = id;
		this.scheduleEntries = scheduleEntries;
	}

	public String getId() {
		return id;
	}

	public List<ScheduleEntry> getScheduleEntries() {
		return scheduleEntries;
	}

}
