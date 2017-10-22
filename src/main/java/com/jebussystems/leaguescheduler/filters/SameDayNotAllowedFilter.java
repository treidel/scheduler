package com.jebussystems.leaguescheduler.filters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.jebussystems.leaguescheduler.entities.TeamSchedule;
import com.jebussystems.leaguescheduler.entities.ScheduleEntry;

public class SameDayNotAllowedFilter extends ScheduleFilterBase<TeamSchedule> implements ScheduleFilter<TeamSchedule> {

	@Override
	public boolean accept(TeamSchedule schedule) {
		// make a list copy of the schedule entries
		List<ScheduleEntry> list = new ArrayList<>(schedule.getScheduleEntries());
		// sort the list by date
		list.sort((left, right) -> left.getGameSlot().getTime().compareTo(right.getGameSlot().getTime()));
		// iterate manually through all entries
		for (int i = 0; i < (list.size() - 1); i++) {
			ScheduleEntry entry1 = list.get(i);
			ScheduleEntry entry2 = list.get(i + 1);
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(entry1.getGameSlot().getTime());
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(entry2.getGameSlot().getTime());
			boolean sameDay = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
					&& calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
			if (true == sameDay) {
				return false;
			}
		}
		// if we get here it's ok
		return true;
	}

}
