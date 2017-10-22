package com.jebussystems.leaguescheduler.filters;

import com.jebussystems.leaguescheduler.entities.Schedule;

public interface ScheduleFilter<T extends Schedule> {

	boolean accept(T schedule);
}
