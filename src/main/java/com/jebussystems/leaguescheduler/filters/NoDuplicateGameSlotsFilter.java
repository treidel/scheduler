package com.jebussystems.leaguescheduler.filters;

import java.util.HashSet;
import java.util.Set;

import com.jebussystems.leaguescheduler.entities.ScheduleEntry;
import com.jebussystems.leaguescheduler.entities.TotalSchedule;

public class NoDuplicateGameSlotsFilter extends ScheduleFilterBase<TotalSchedule>
		implements ScheduleFilter<TotalSchedule> {

	@Override
	public boolean accept(TotalSchedule schedule) {
		// create a set that we'll add gameslot ids into
		// if we find a dup in the set then we have an invalid schedule
		Set<String> gameslots = new HashSet<>(schedule.getScheduleEntries().size());
		for (ScheduleEntry entry : schedule.getScheduleEntries()) {
			if (false == gameslots.add(entry.getGameSlot().getId())) {
				return false;
			}
		}
		// if we get here it's ok
		return true;
	}

}
