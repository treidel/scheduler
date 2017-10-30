package com.jebussystems.leaguescheduler.filters;

import java.util.List;

import com.jebussystems.leaguescheduler.entities.ScheduleEntry;
import com.jebussystems.leaguescheduler.entities.TeamSchedule;

public class UniqueGameslotsFilter extends ScheduleFilterBase<TeamSchedule> implements ScheduleFilter<TeamSchedule> {

	@Override
	public boolean accept(TeamSchedule schedule) {
		List<ScheduleEntry> scheduleEntryList = schedule.getScheduleEntries();
		// make sure there's no duplicate game slots
		for (int i = 0; i < scheduleEntryList.size() - 1; i++) {
			ScheduleEntry entry1 = scheduleEntryList.get(i);
			ScheduleEntry entry2 = scheduleEntryList.get(i + 1);
			if (true == entry1.getGameSlot().getId().equals(entry2.getGameSlot().getId())) {
				return false;
			}
		}

		// if we get here it's ok
		return true;
	}

}
