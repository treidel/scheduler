package com.jebussystems.leaguescheduler.filters;

import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobConfigurable;

import com.jebussystems.leaguescheduler.entities.Schedule;

public abstract class ScheduleFilterBase<T extends Schedule> implements ScheduleFilter<T>, JobConfigurable {

	@Override
	public void configure(JobConf job) {
		// do nothing
	}
}
