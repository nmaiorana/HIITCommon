/*
 * Copyright (C) 2010 Silent Viper Investments
 */

package com.svi.hiit.views;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Playlists.Members;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.svi.hiit.R;
import com.svi.hiit.models.Interval;
import com.svi.hiit.models.SpeedIntervalWorkout;
import com.svi.hiit.util.IntervalTimer;

public class HIITSession extends Activity implements OnInitListener, OnCompletionListener {

	public static final String INTERVAL_WORKOUT = "INTERVAL_WORKOUT";
	private static final String TAG = "HIITSession";
	private static final int HIITA_ID = 1;

	protected static final String RAW_PATH = "raw";

	public static final String PLAYLIST = "com.svi.hiitlib.models.Playlist";
	public static final String PLAYLISTID = "com.svi.hiitlib.models.PlaylistId";

	public static final String PLAYLIST_ALL = "Play all";
	public static final String PLAYLIST_NONE = "None";

	private static final double TALK_FAST = 1.3;
	private static final double TALK_NORMAL = 1;

	// Feature Checking Codes
	private static final int DATA_CHECK_TEXT_TO_SPEECH = 0;

	private SpeedIntervalWorkout intervalWorkout;

	protected TextToSpeech myTextToSpeech;
	protected boolean textToSpeachActive = false;
	protected int utteranceId = 0;

	protected Button endWorkoutButton;
	protected Button pauseWorkoutButton;
	protected Button resumeWorkoutButton;
	protected long pauseTime;
	protected Chronometer workoutTime;
	protected Chronometer intervalTime;
	protected String playlistName;
	protected Long playlistId;
	protected ProgressBar workoutProgress;
	protected int workoutGraph;

	protected MediaPlayer alarmSound;
	protected MediaPlayer musicPlayer = null;
	protected List<String> songList = new ArrayList<String>();
	protected int songIndex = 0;

	protected Thread notifyingThread;
	protected boolean continueTimer = false;
	final Handler intervalHandler = new Handler();
	protected IntervalTimer timer;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		Intent intent = getIntent();
		intent.setAction(ACTIVITY_SERVICE);
		intent.addCategory(ACTIVITY_SERVICE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.workout_session);

		/*
		 * Alarm
		 */
		alarmSound = MediaPlayer.create(getBaseContext(), R.raw.short_whistle);

		/*
		 * User Controls
		 */
		endWorkoutButton = (Button) findViewById(R.id.end_workout);
		endWorkoutButton.setOnClickListener(endWorkout);
		pauseWorkoutButton = (Button) findViewById(R.id.pause_workout);
		pauseWorkoutButton.setOnClickListener(pauseWorkout);
		resumeWorkoutButton = (Button) findViewById(R.id.resume_workout);
		resumeWorkoutButton.setOnClickListener(resumeWorkout);
		pauseWorkoutButton.setVisibility(Button.INVISIBLE);
		resumeWorkoutButton.setVisibility(Button.INVISIBLE);
		endWorkoutButton.setVisibility(Button.INVISIBLE);

		/*
		 * Get the workout
		 */
		intervalWorkout = (SpeedIntervalWorkout) intent.getExtras().get(INTERVAL_WORKOUT);
		playlistName = (String) intent.getStringExtra(PLAYLIST);
		playlistId = intent.getLongExtra(PLAYLISTID, 0);

		/*
		 * Create a workout
		 */
		workoutProgress = (ProgressBar) findViewById(R.id.workout_progress);
		if (workoutProgress != null) {
			workoutProgress.setMax(intervalWorkout.computeIntervalTimeInSeconds());
		}

		/*
		 * Workout stats for the user
		 */
		workoutTime = (Chronometer) findViewById(R.id.workout_time);
		intervalTime = (Chronometer) findViewById(R.id.interval_time);

		/*
		 * Setup text-to-speech. Start the workout once it's ready.
		 */
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, DATA_CHECK_TEXT_TO_SPEECH);

		setupMusicPlayer();
		intervalWorkout.startWorkout();

	}

	/**
	 * Notification Bar
	 */

//	@SuppressLint("NewApi")
//	private void setupNotification() {
//		Notification.Builder mBuilder = 
//		        new Notification.Builder(this)
//		        .setSmallIcon(R.drawable.notification_icon)
//		        .setContentTitle(getText(R.string.app_name) + " Started")
//		        .setContentText(intervalWorkout.getTitle())
//		        .setWhen(System.currentTimeMillis());
////		Context context = getApplicationContext();
////		CharSequence contentTitle = "Started";
////		CharSequence contentText = intervalWorkout.getTitle();
////		Intent notificationIntent = new Intent(this, HIITSession.class);
////		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
////		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
//		
//		Intent resultIntent = new Intent(this, HIITSession.class);
//		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
//
//		mBuilder.setContentIntent(resultPendingIntent);
//
//		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		mNotificationManager.notify(HIITA_ID, mBuilder.build());
//	}
//
//	private void removeNotification() {
//		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		mNotificationManager.cancel(HIITA_ID);
//	}

	/**
	 * UI Controls
	 */

	private OnClickListener endWorkout = new OnClickListener() {
		public void onClick(View v) {
			endWorkout();
		}
	};

	private OnClickListener pauseWorkout = new OnClickListener() {
		public void onClick(View v) {
			pauseWorkout();
		}
	};

	private OnClickListener resumeWorkout = new OnClickListener() {
		public void onClick(View v) {
			resumeWorkout();
		}
	};

	@Override
	public void onBackPressed() {
		if (!intervalWorkout.isWorkoutStarted())
			return;
		if (intervalWorkout.isWorkoutPaused()) {
			endWorkout();
			return;
		}
		if (pauseWorkoutButton.isShown()) {
			pauseWorkout();
		}
	}

	/**
	 * Work out Controls
	 */

	private void endWorkout() {
		stopTimer();

		if (musicPlayer != null) {
			musicPlayer.release();
			musicPlayer = null;
		}

		alarmSound.release();

		if (intervalWorkout.isWorkoutEnded()) {
			speakText("Workout completed.  Congratualations!", TALK_NORMAL);
		} else {
			speakText("Workout ended by user.", TALK_NORMAL);
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// Do nothing
		}
		
		while (myTextToSpeech.isSpeaking()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
		myTextToSpeech.shutdown();

		workoutTime.stop();
		intervalTime.stop();

		finish();
	}

	private void pauseWorkout() {
		pauseMusic();
		intervalWorkout.pauseWorkout();
		stopTimer();
		speakText("Pausing workout.", TALK_NORMAL);
		pauseTime = SystemClock.elapsedRealtime();
		intervalTime.stop();
		workoutTime.stop();
		pauseWorkoutButton.setVisibility(Button.INVISIBLE);
		resumeWorkoutButton.setVisibility(Button.VISIBLE);
		endWorkoutButton.setVisibility(Button.VISIBLE);
	}

	private void resumeWorkout() {
		speakText("Resuming workout.", TALK_NORMAL);
		workoutTime.start();
		pauseWorkoutButton.setVisibility(Button.VISIBLE);
		resumeWorkoutButton.setVisibility(Button.INVISIBLE);
		endWorkoutButton.setVisibility(Button.INVISIBLE);
		long timePaused = SystemClock.elapsedRealtime() - pauseTime;
		workoutTime.setBase(workoutTime.getBase() + timePaused);
		intervalWorkout.resumeWorkout();
		updateInterval();
		resumeMusic();
	}

	protected void updateInterval() {
		pauseWorkoutButton.setVisibility(Button.INVISIBLE);
		resumeWorkoutButton.setVisibility(Button.INVISIBLE);
		endWorkoutButton.setVisibility(Button.INVISIBLE);
		workoutProgress.setProgress(intervalWorkout.getTimeWorkedOutInSeconds());

		if (intervalWorkout.isWorkoutEnded()) {
			endWorkout();
			return;
		}

		speakCurrentIntervalInformation();

		TextView currentIntervalView = (TextView) findViewById(R.id.current_interval);
		TextView currentSpeedView = (TextView) findViewById(R.id.current_speed);
		currentIntervalView.setText(intervalWorkout.getCurrentInterval().getDescription());
		currentSpeedView.setText("Treadmill: " + String.valueOf(intervalWorkout.getCurrentInterval().getSpeed()));
		intervalTime.setBase(SystemClock.elapsedRealtime());
		intervalTime.start();

		soundAlarm();

		timer = new IntervalTimer(intervalHandler, updateInterval, intervalWorkout.getCurrentInterval().getDurationInMillis(), true);
		timer.start();
		
		pauseWorkoutButton.setVisibility(Button.VISIBLE);
	}

	final Runnable updateInterval = new Runnable() {
		public void run() {
			intervalWorkout.advanceToNextInterval();
			updateInterval();
		}
	};

	/**
	 * Timer Controls
	 */

	Runnable intervalTimer = new Runnable() {
		public void run() {
			while (continueTimer) {
				try {
					Thread.sleep(intervalWorkout.getCurrentInterval().getDurationInMillis());
					intervalWorkout.advanceToNextInterval();
					intervalHandler.post(updateInterval);
				} catch (InterruptedException e) {
					Log.w(TAG, "Timer was interupted");
				}
			}
		}
	};

	private void stopTimer() {
		timer.stop();
	}

	/**
	 * Media Player Controls
	 */

	public void onCompletion(MediaPlayer mp) {
		mp.release();
		startMusic();
	}

	// TODO: Get new playlist data
	private void setupMusicPlayer() {
		if (playlistName.equals(PLAYLIST_NONE)) {
			return;
		}

		if (playlistName.equals(PLAYLIST_ALL)) {
			ContentResolver contentResolver = getContentResolver();
			Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			Cursor cursor = contentResolver.query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);

			if (cursor == null) {
				Log.e(TAG, "Could not locate any music on device: ");
				return;
			}

			if (!cursor.moveToFirst()) {
				// Nothing to query. There is no music on the device. How
				// boring.
				Log.e(TAG, "Failed to find any music on device");
				return;
			}

			int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

			// add each song to mItems
			do {
				songList.add(cursor.getString(dataColumn));
			} while (cursor.moveToNext());

			cursor.close();
			Collections.shuffle(songList);
			return;
		}
		
		// Just get the songs from the selected playlist

		Uri membersURI = Members.getContentUri("external", playlistId);
		String[] projection = new String[] { Members.DATA };
		Cursor cursor = getContentResolver().query(membersURI, projection, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				songList.add(cursor.getString(cursor.getColumnIndex(Members.DATA)));
			} while (cursor.moveToNext());
		}
		cursor.close();
		Collections.shuffle(songList);
	}

	private void startMusic() {
		if (songList.size() <= 0) {
			musicPlayer = null;
			return;
		}
		try {
			if (songIndex >= songList.size()) {
				songIndex = 0;
			}

			String data = songList.get(songIndex++);
			musicPlayer = new MediaPlayer();
			musicPlayer.setOnCompletionListener(this);
			musicPlayer.setDataSource(data);
			musicPlayer.prepare();
			musicPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void pauseMusic() {
		if (musicPlayer == null) {
			return;
		}

		if (musicPlayer.isPlaying()) {
			musicPlayer.pause();
		}
	}

	private void resumeMusic() {
		if (musicPlayer == null) {
			return;
		}

		if (!musicPlayer.isPlaying()) {
			musicPlayer.start();
		}
	}

	private void soundAlarm() {
		if (alarmSound.isPlaying()) {
			alarmSound.stop();
		}
		alarmSound.start();
	}

	/**
	 * Text To Speech Controls
	 */

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DATA_CHECK_TEXT_TO_SPEECH) {
			// success, create the TTS instance
			myTextToSpeech = new TextToSpeech(this, this);
			myTextToSpeech.setSpeechRate((float) 2);
		}
	}

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			textToSpeachActive = true;
		}
		speakText("Ready Set Go!", TALK_NORMAL);
		startMusic();
		workoutTime.setBase(SystemClock.elapsedRealtime());
		workoutTime.start();
		updateInterval();
		pauseWorkoutButton.setVisibility(Button.VISIBLE);
	}

	protected void speakText(String text, double speechRate) {

		if (myTextToSpeech == null || !textToSpeachActive) {
			return;
		}
		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(utteranceId++));
		myTextToSpeech.setSpeechRate((float) speechRate);
		myTextToSpeech.speak(text, TextToSpeech.QUEUE_ADD, myHashAlarm);
		while (myTextToSpeech.isSpeaking()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
		myTextToSpeech.stop();
	}

	protected void speakCurrentIntervalInformation() {
		pauseMusic();
		speakText(intervalWorkout.getCurrentInterval().getDescription() + " at " + intervalWorkout.getCurrentInterval().getSpeed()
				+ " miles per hour. " + intervalWorkout.getCurrentInterval().getDuration() + " seconds.", TALK_FAST);

		if (intervalWorkout.getCurrentInterval().getDescription().equals(Interval.INTERVAL_TYPE_SPRINT)) {
			speakText(
					"Interval " + (intervalWorkout.getNumberOfSpeedIntervalsCompleted() + 1) + " of "
							+ intervalWorkout.getNumberOfSpeedIntervals(), TALK_NORMAL);
		} else if (intervalWorkout.getCurrentInterval().getDescription().equals(Interval.INTERVAL_TYPE_REST)) {
			speakText(
					"Interval " + intervalWorkout.getNumberOfSpeedIntervalsCompleted() + " of "
							+ intervalWorkout.getNumberOfSpeedIntervals() + " completed", TALK_NORMAL);
		}
		resumeMusic();
	}

}
