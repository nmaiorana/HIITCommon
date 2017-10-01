package com.svi.hiit.models;

/*
 * Copyright (C) 2010 Silent Viper Investments
 */

import java.util.List;

public class WorkoutBuilder {

	public static final int DEFAULT_NUMBER_OF_INTERVALS = 4;
	public static final int DEFAULT_SPRINT_INTERVAL_TIME = 30;
	public static final int DEFAULT_REST_INTERVAL_TIME = 30;
	public static final int DEFAULT_SPRINT_SPEED = 5;
	public static final int DEFAULT_REST_SPEED = 3;

	private WarmUpTypes warmUpType = WarmUpTypes.STANDARD;
	private CoolDownTypes coolDownType = CoolDownTypes.STANDARD;

	private int numberOfIntervals = DEFAULT_NUMBER_OF_INTERVALS;
	private int sprintIntervalTime = DEFAULT_SPRINT_INTERVAL_TIME;
	private int restIntervalTime = DEFAULT_REST_INTERVAL_TIME;

	private int sprintSpeed = DEFAULT_SPRINT_SPEED;
	private int restSpeed = DEFAULT_REST_SPEED;

	public static int workoutTimeInSeconds(List<Interval> workout) {
		int workoutTime = 0;
		for (Interval interval : workout) {
			workoutTime += interval.getDuration();
		}
		return workoutTime;
	}

	public WorkoutBuilder() {
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WorkoutBuilder [title=").append(generateTitle()).append(", warmUpType=").append(warmUpType).append(", coolDownType=")
				.append(coolDownType).append(", numberOfIntervals=").append(numberOfIntervals).append(", sprintIntervalTime=")
				.append(sprintIntervalTime).append(", restIntervalTime=").append(restIntervalTime).append(", sprintSpeed=")
				.append(sprintSpeed).append(", restSpeed=").append(restSpeed).append("]");
		return builder.toString();
	}

	public SpeedIntervalWorkout createIntervals() {
		SpeedIntervalWorkout intervalWorkout = new SpeedIntervalWorkout();
		intervalWorkout.setTitle(generateTitle());
		if (numberOfIntervals != 1)
			createWarmUp(intervalWorkout);
		for (int i = 0; i < getNumberOfIntervals(); i++) {
			intervalWorkout.addInterval(new Interval(Interval.INTERVAL_TYPE_SPRINT, getSprintSpeed(), getSprintIntervalTime()));
			intervalWorkout.addInterval(new Interval(Interval.INTERVAL_TYPE_REST, getRestSpeed(), getRestIntervalTime()));
		}
		if (numberOfIntervals != 1)
			createCoolDown(intervalWorkout);
		return intervalWorkout;
	}

	private String generateTitle() {
		StringBuilder builder = new StringBuilder();
		builder.append(getNumberOfIntervals()).append(" Intervals ").append(getSprintIntervalTime()).append("/").append(getRestIntervalTime());
		return builder.toString();
	}

	public void createWarmUp(SpeedIntervalWorkout intervalWorkout) {
		intervalWorkout.getIntervals().addAll(WarmUpTypeBuilder.getWarmupIntervals(getWarmUpType()));
	}

	public void createCoolDown(SpeedIntervalWorkout intervalWorkout) {
		intervalWorkout.getIntervals().addAll(CoolDownTypeBuilder.getCoolDownIntervals(getCoolDownType()));
	}

	public int getSprintSpeed() {
		return sprintSpeed;
	}

	public void setSprintSpeed(int sprintSpeed) {
		this.sprintSpeed = sprintSpeed;
	}

	public int getRestSpeed() {
		return restSpeed;
	}

	public void setRestSpeed(int restSpeed) {
		this.restSpeed = restSpeed;
	}

	public WarmUpTypes getWarmUpType() {
		return warmUpType;
	}

	public void setWarmUpType(WarmUpTypes warmUpType) {
		this.warmUpType = warmUpType;
	}

	public CoolDownTypes getCoolDownType() {
		return coolDownType;
	}

	public void setCoolDownType(CoolDownTypes coolDownType) {
		this.coolDownType = coolDownType;
	}

	public int getNumberOfIntervals() {
		return numberOfIntervals;
	}

	public void setNumberOfIntervals(int numberOfIntervals) {
		this.numberOfIntervals = numberOfIntervals;
	}

	public int getSprintIntervalTime() {
		return sprintIntervalTime;
	}

	public void setSprintIntervalTime(int sprintIntervalTime) {
		this.sprintIntervalTime = sprintIntervalTime;
	}

	public int getRestIntervalTime() {
		return restIntervalTime;
	}

	public void setRestIntervalTime(int restIntervalTime) {
		this.restIntervalTime = restIntervalTime;
	}

}
