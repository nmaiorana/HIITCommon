package com.svi.hiit.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SpeedIntervalWorkout implements Serializable {

	private static final long serialVersionUID = 4252744074625204558L;
	protected String title;
	protected List<Interval> intervals = new ArrayList<Interval>();
	protected int timeWorkedOutInSeconds = 0;
	protected int numberOfSpeedIntervals = 0;
	protected int numberOfSpeedIntervalsCompleted = 0;
	protected int speedIntervalVelocity = 0;
	protected int currentInterval = 0;
	protected int graphic;
	protected boolean workoutStarted = false;
	protected boolean workoutEnded = false;
	protected boolean workoutPaused = false;

	public void addInterval(Interval interval) {
		getIntervals().add(interval);
		if (interval.getDescription().equals(Interval.INTERVAL_TYPE_SPRINT)) {
			setSpeedIntervalVelocity(interval.getSpeed());
			numberOfSpeedIntervals++;
		}
	}
	
	public int computeIntervalTimeInSeconds() {
		int intervalTimeInSeconds = 0;
		for(Interval interval: getIntervals()) {
			intervalTimeInSeconds += interval.getDuration();
		}
		return intervalTimeInSeconds;
	}

	public void startWorkout() {
		setWorkoutStarted(true);
		setWorkoutEnded(false);
		setWorkoutPaused(false);
	}

	public void pauseWorkout() {
		setWorkoutPaused(true);
	}

	public void resumeWorkout() {
		if (!isWorkoutPaused()) {
			return;
		}
		setWorkoutPaused(false);
		if (isFirstInterval()) {
			return;
		}
		if (!getIntervals().get(currentInterval).getDescription().equals(Interval.INTERVAL_TYPE_SPRINT)) {
			return;
		}

		/*
		 * Go Back to last warm up, cool down or rest and remove time used for
		 * last sprint
		 */
		currentInterval--;
		timeWorkedOutInSeconds -= getCurrentInterval().getDuration();
	}

	public boolean isFirstInterval() {
		return currentInterval == 0;
	}

	public boolean isLastInterval() {
		return currentInterval >= getIntervals().size() - 1;
	}

	public void advanceToNextInterval() {
		if (!isWorkoutStarted()) {
			return;
		}
		if (isWorkoutEnded())
			return;
		timeWorkedOutInSeconds += getCurrentInterval().getDuration();
		if (getIntervals().get(currentInterval).getDescription().equals(Interval.INTERVAL_TYPE_SPRINT)) {
			numberOfSpeedIntervalsCompleted++;
		}
		if (isLastInterval()) {
			setWorkoutEnded(true);
			return;
		}
		currentInterval++;
	}

	public Interval getCurrentInterval() {
		return getIntervals().get(currentInterval);
	}

	public List<Interval> getIntervals() {
		return intervals;
	}

	public void setIntervals(List<Interval> intervals) {
		this.intervals = intervals;
	}

	public int getNumberOfSpeedIntervals() {
		return numberOfSpeedIntervals;
	}

	public void setNumberOfSpeedIntervals(int numberOfSpeedIntervals) {
		this.numberOfSpeedIntervals = numberOfSpeedIntervals;
	}

	public int getNumberOfSpeedIntervalsCompleted() {
		return numberOfSpeedIntervalsCompleted;
	}

	public void setNumberOfSpeedIntervalsCompleted(int numberOfSpeedIntervalsCompleted) {
		this.numberOfSpeedIntervalsCompleted = numberOfSpeedIntervalsCompleted;
	}

	public int getGraphic() {
		return graphic;
	}

	public void setGraphic(int graphic) {
		this.graphic = graphic;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isWorkoutStarted() {
		return workoutStarted;
	}

	public void setWorkoutStarted(boolean workoutStarted) {
		this.workoutStarted = workoutStarted;
	}

	public boolean isWorkoutEnded() {
		return workoutEnded;
	}

	public void setWorkoutEnded(boolean workoutEnded) {
		this.workoutEnded = workoutEnded;
	}

	public int getTimeWorkedOutInSeconds() {
		return timeWorkedOutInSeconds;
	}

	public void setTimeWorkedOutInSeconds(int timeWorkedOutInSeconds) {
		this.timeWorkedOutInSeconds = timeWorkedOutInSeconds;
	}

	public boolean isWorkoutPaused() {
		return workoutPaused;
	}

	public void setWorkoutPaused(boolean workoutPaused) {
		this.workoutPaused = workoutPaused;
	}

	public int getSpeedIntervalVelocity() {
		return speedIntervalVelocity;
	}

	public void setSpeedIntervalVelocity(int speedIntervalVelocity) {
		this.speedIntervalVelocity = speedIntervalVelocity;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SpeedIntervalWorkout [title=").append(title).append(", intervals=").append(intervals)
				.append(", computeIntervalTimeInSeconds=").append(computeIntervalTimeInSeconds()).append(", timeWorkedOutInSeconds=")
				.append(timeWorkedOutInSeconds).append(", numberOfSpeedIntervals=").append(numberOfSpeedIntervals)
				.append(", numberOfSpeedIntervalsCompleted=").append(numberOfSpeedIntervalsCompleted)
				.append(", speedIntervalVelocity=").append(speedIntervalVelocity).append(", currentInterval=")
				.append(currentInterval).append(", graphic=").append(graphic).append(", workoutStarted=").append(workoutStarted)
				.append(", workoutEnded=").append(workoutEnded).append(", workoutPaused=").append(workoutPaused).append("]");
		return builder.toString();
	}

}
