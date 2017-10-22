package com.jebussystems.leaguescheduler.entities;

import java.util.Collection;

import com.google.gson.Gson;

public class TotalSchedule extends Schedule {
	public static final Gson GSON = new Gson();

	public TotalSchedule(String id, Collection<ScheduleEntry> scheduleEntries) {
		super(id, scheduleEntries);
	}

	@Override
	public String toString() {
		return "[" + "id=" + getId() + ",scheduleentries=" + getScheduleEntries().toString() + "]";
	}
}
