package com.svi.hiit.models;

import java.util.ArrayList;
import java.util.List;

public class WarmUpTypeBuilder {

	private static final String QUICK_WARMUP = "Quick Warm up";
	private static final String STANDARD_WARMUP = "Standard Warm up";
	
	public static List<Interval> getWarmupIntervals(int warmupType) {
		return getWarmupIntervals(warmupType);
	}

	public static List<Interval> getWarmupIntervals(WarmUpTypes warmupType) {
		switch (warmupType) {
		case NONE:
			return new ArrayList<Interval>();
		case QUICK:
			return quickWarmup();
		default:
			return standardWarmup();
		}
	}

	private static List<Interval> quickWarmup() {
		List<Interval> warmup = new ArrayList<Interval>();
		warmup.add(new Interval(QUICK_WARMUP, 2, 30));
		warmup.add(new Interval(QUICK_WARMUP, 3, 30));
		warmup.add(new Interval(QUICK_WARMUP, 4, 60));
		return warmup;
	}

	private static List<Interval> standardWarmup() {
		List<Interval> warmup = new ArrayList<Interval>();
		warmup.add(new Interval(STANDARD_WARMUP, 2, 60));
		warmup.add(new Interval(STANDARD_WARMUP, 3, 120));
		warmup.add(new Interval(STANDARD_WARMUP, 4, 120));
		return warmup;
	}

}
