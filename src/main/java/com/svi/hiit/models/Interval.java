/*
 * Copyright (C) 2010 Silent Viper Investments
 */

package com.svi.hiit.models;

import java.io.Serializable;

public class Interval implements Serializable{
	
	private static final long serialVersionUID = -6961799832988847819L;
	public static final String INTERVAL_TYPE_WARMUP = "Warm Up";
	public static final String INTERVAL_TYPE_COOLDOWN = "Cool Down";
	public static final String INTERVAL_TYPE_SPRINT = "Sprint";
	public static final String INTERVAL_TYPE_REST = "Rest";

	private String description;
	private int speed;
	private int duration;

	public Interval(String description, int speed, int duration) {
		setDescription(description);
		setSpeed(speed);
		setDuration(duration);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getDuration() {
		return duration;
	}

	public int getDurationInMillis() {
		return duration * 1000 ;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Interval [description=").append(description).append(", speed=").append(speed).append(", duration=")
				.append(duration).append("]");
		return builder.toString();
	}
}
