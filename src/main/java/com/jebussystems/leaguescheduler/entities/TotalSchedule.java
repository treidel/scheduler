package com.jebussystems.leaguescheduler.entities;

import java.util.List;

public class TotalSchedule extends Schedule {

	public TotalSchedule(String id, List<ScheduleEntry> scheduleEntries) {
		super(id, scheduleEntries);
	}

	@Override
	public String toString() {
		return "[" + "id=" + getId() + ",scheduleentries=" + getScheduleEntries().toString() + "]";
	}
}
