package com.jebussystems.leaguescheduler.totalgamefilterjob;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import com.jebussystems.leaguescheduler.entities.TotalSchedule;
import com.jebussystems.leaguescheduler.filters.NoDuplicateGameSlotsFilter;
import com.jebussystems.leaguescheduler.filters.ScheduleFilter;
import com.jebussystems.leaguescheduler.filters.ScheduleFilterBase;

public class TotalGameFilterReducer extends MapReduceBase implements Reducer<Text, Text, NullWritable, Text> {

	private final Collection<ScheduleFilterBase<TotalSchedule>> filters = new LinkedList<>();

	public TotalGameFilterReducer() {
		this.filters.add(new NoDuplicateGameSlotsFilter());
	}

	@Override
	public void configure(JobConf job) {
		// do whatever the base class does
		super.configure(job);
		// configure our filters
		for (ScheduleFilterBase<TotalSchedule> filter : filters) {
			filter.configure(job);
		}
	}

	public void reduce(Text key, Iterator<Text> values, OutputCollector<NullWritable, Text> output, Reporter reporter)
			throws IOException {
		// only need a single input per key
		Text value = values.next();

		// parse to an total schedule
		TotalSchedule schedule = TotalSchedule.GSON.fromJson(value.toString(), TotalSchedule.class);

		// go through each filter and check this home schedule for exclusion criteria
		for (ScheduleFilter<TotalSchedule> filter : this.filters) {
			if (false == filter.accept(schedule)) {
				return;
			}
		}

		// if we get here this is a valid schedule
		output.collect(null, value);
	}
}
