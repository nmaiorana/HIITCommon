package com.svi.hiit.models;

import java.util.ArrayList;
import java.util.List;

public class CoolDownTypeBuilder {

	private static final String QUICK_COOLDDOWN = "Quick Cool Down";
	private static final String STANDARD_COOLDOWN = "Standard Cool Down";
	
	public static List<Interval> getWarmupIntervals(int warmupType) {
		return getWarmupIntervals(warmupType);
	}

	public static List<Interval> getCoolDownIntervals(CoolDownTypes coolDownType) {
		switch (coolDownType) {
		case NONE:
			return new ArrayList<Interval>();
		case QUICK:
			return quickCoolDown();
		default:
			return standardCoolDown();
		}
	}

	private static List<Interval> quickCoolDown() {
		List<Interval> warmup = new ArrayList<Interval>();
		warmup.add(new Interval(QUICK_COOLDDOWN, 4, 60));
		warmup.add(new Interval(QUICK_COOLDDOWN, 3, 60));
		warmup.add(new Interval(QUICK_COOLDDOWN, 2, 60));
		return warmup;
	}

	private static List<Interval> standardCoolDown() {
		List<Interval> warmup = new ArrayList<Interval>();
		warmup.add(new Interval(STANDARD_COOLDOWN, 4, 60));
		warmup.add(new Interval(STANDARD_COOLDOWN, 3, 120));
		warmup.add(new Interval(STANDARD_COOLDOWN, 2, 120));
		return warmup;
	}

}
