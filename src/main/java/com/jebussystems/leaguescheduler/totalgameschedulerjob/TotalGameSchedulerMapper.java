package com.jebussystems.leaguescheduler.totalgameschedulerjob;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class TotalGameSchedulerMapper extends MapReduceBase implements Mapper<LongWritable, Text, NullWritable, Text> {

	@Override
	public void map(LongWritable key, Text value, OutputCollector<NullWritable, Text> output, Reporter reporter)
			throws IOException {
		// output straight to the reducer
		output.collect(NullWritable.get(), value);
	}
}
