package com.jebussystems.leaguescheduler.homegamefilterjob;

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

import com.jebussystems.leaguescheduler.entities.Serializer;
import com.jebussystems.leaguescheduler.entities.TeamSchedule;
import com.jebussystems.leaguescheduler.filters.MustPlayEveryAwayTeamFilter;
import com.jebussystems.leaguescheduler.filters.OneGamePerAwayTeamFilter;
import com.jebussystems.leaguescheduler.filters.SameDayNotAllowedFilter;
import com.jebussystems.leaguescheduler.filters.ScheduleFilter;
import com.jebussystems.leaguescheduler.filters.ScheduleFilterBase;
import com.jebussystems.leaguescheduler.filters.TeamBlackoutFilter;

public class HomeGameFilterReducer extends MapReduceBase implements Reducer<Text, Text, NullWritable, Text> {

	private final Collection<ScheduleFilterBase<TeamSchedule>> filters = new LinkedList<>();

	public HomeGameFilterReducer() {
		this.filters.add(new OneGamePerAwayTeamFilter());
		this.filters.add(new MustPlayEveryAwayTeamFilter());
		this.filters.add(new SameDayNotAllowedFilter());
		this.filters.add(new TeamBlackoutFilter());
	}

	@Override
	public void configure(JobConf job) {
		// do whatever the base class does
		super.configure(job);

		// configure our filters
		for (ScheduleFilterBase<TeamSchedule> filter : filters) {
			filter.configure(job);
		}
	}

	public void reduce(Text key, Iterator<Text> values, OutputCollector<NullWritable, Text> output, Reporter reporter)
			throws IOException {
		// only need a single input per key
		Text value = values.next();

		// parse to an home schedule
		TeamSchedule schedule = Serializer.GSON.fromJson(value.toString(), TeamSchedule.class);

		// go through each filter and check this home schedule for exclusion criteria
		for (ScheduleFilter<TeamSchedule> filter : this.filters) {
			if (false == filter.accept(schedule)) {
				return;
			}
		}

		// if we get here this is a valid home schedule
		output.collect(null, value);
	}
}
