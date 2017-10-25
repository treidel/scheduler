package com.jebussystems.leaguescheduler.entities;

import java.util.Collection;

public class TotalSchedule extends Schedule {

	public TotalSchedule(String id, Collection<ScheduleEntry> scheduleEntries) {
		super(id, scheduleEntries);
	}

	@Override
	public String toString() {
		return "[" + "id=" + getId() + ",scheduleentries=" + getScheduleEntries().toString() + "]";
	}
}
