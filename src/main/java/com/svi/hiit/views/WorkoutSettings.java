/*
 * Copyright (C) 2010 Silent Viper Investments
 */

package com.svi.hiit.views;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.svi.hiit.R;
import com.svi.hiit.models.CoolDownTypes;
import com.svi.hiit.models.WarmUpTypes;
import com.svi.hiit.models.WorkoutBuilder;

public class WorkoutSettings extends Activity {
	public static final int HIITTA_ID = 1;
	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh6306b6HOnBxC2soTtmNgKf1IqlwpEYPi760EOBKcHQ40kk30e5UmsHHoFV+yiBA44kNuOiSeqmrw0rZg74gwD69WXZ+N9X4Sz50y7Z+wMACC6AOgwtMotqNQxffBCec6bkjdcBBSU6iZauKcs7fRbIzptHyk+N8L/ViRfGKzPgRhSG+/PlKbZKFPZTwHEeDzYq/zxSy5TZsYiB+Nw7UoPtn5mVGIr5sAiyWZPWqGm/uFqK3yBXOusYL7rcqvmnHFYpbRxp8LiV2v0v7/Xd7HE488GDJWsAdcmyabBD5JdIjLNxi5zgcdvyAMK2tfd+txjPK1egtQl5ld4BZN2R57QIDAQAB";
	private static final byte[] SALT = new byte[] { -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -11, -21,
			-62, 32, -64, 89 };
	protected boolean licensed = false;
	protected LicenseChecker mChecker;

	private static final String TAG = "WorkoutSettings";

	public static final String WARMUP_TYPE = "WARMUP_TYPE";
	public static final String NUMBER_OF_INTERVALS = "NUMBER_OF_INTERVALS";
	public static final String SPRINT_INTERVAL_TIME = "SPRINT_INTERVAL_TIME";
	public static final String SPRINT_SPEED = "SPRINT_SPEED";
	public static final String REST_INTERVAL_TIME = "REST_INTERVAL_TIME";
	public static final String REST_SPEED = "REST_SPEED";
	public static final String COOLDOWN_TYPE = "COOLDOWN_TYPE";
	public static final String PLAYLIST_NAME = "PLAYLIST_NAME";
	public static final String PLAYLIST_ID = "PLAYLIST_ID";
	public static final String DISCLAIMER_ACCEPTANCE = "DISCLAIMER_ACCEPTANCE";

	public static final String APP_ID = "131287370256893";

	protected static final String DRAWABLES_PATH = "drawable";

	protected WorkoutBuilder workoutBuilder = new WorkoutBuilder();
	protected String intervalSpecDescription;

	protected int[] numberOfIntervalsArray;
	protected int[] sprintIntervalTimeArray;
	protected int[] restIntervalTimeArray;
	protected int[] sprintSpeedArray;
	protected int[] restSpeedArray;

	protected List<Long> playlistIds = new ArrayList<Long>();
	protected String playlistName = HIITSession.PLAYLIST_NONE;
	protected Long playlistId = Long.valueOf(0);

	// TODO Display cumulative workout time

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.workout_settings);

		// TODO Create Helper class for preference
		SharedPreferences myPreferences = getPreferences(MODE_PRIVATE);
		showDisclaimer();

		Intent intent = getIntent();
		intent.setAction(ACTIVITY_SERVICE);
		intent.addCategory(ACTIVITY_SERVICE);

		numberOfIntervalsArray = getApplicationContext().getResources().getIntArray(R.array.numberOfIntervalsValue);
		sprintIntervalTimeArray = getApplicationContext().getResources().getIntArray(R.array.sprint_interval_times_value);
		restIntervalTimeArray = getApplicationContext().getResources().getIntArray(R.array.rest_interval_times_value);
		sprintSpeedArray = getApplicationContext().getResources().getIntArray(R.array.sprintSpeedValue);
		restSpeedArray = getApplicationContext().getResources().getIntArray(R.array.restSpeedValue);

		Spinner warmUpSpinner = (Spinner) findViewById(R.id.warmup_selection);
		warmUpSpinner.setOnItemSelectedListener(warmUpType);
		warmUpSpinner.setSelection(myPreferences.getInt(WARMUP_TYPE, 2));

		Spinner numberOfIntevalsSpinner = (Spinner) findViewById(R.id.intervals_selection);
		numberOfIntevalsSpinner.setOnItemSelectedListener(numberOfIntervals);
		numberOfIntevalsSpinner.setSelection(myPreferences.getInt(NUMBER_OF_INTERVALS, 0));

		Spinner sprintTimeSpinner = (Spinner) findViewById(R.id.sprint_interval_time_selection);
		sprintTimeSpinner.setOnItemSelectedListener(sprintTime);
		sprintTimeSpinner.setSelection(myPreferences.getInt(SPRINT_INTERVAL_TIME, 0));

		Spinner speedSpinner = (Spinner) findViewById(R.id.sprint_speed_selection);
		speedSpinner.setOnItemSelectedListener(selectSpeed);
		speedSpinner.setSelection(myPreferences.getInt(SPRINT_SPEED, 0));

		Spinner restTimeSpinner = (Spinner) findViewById(R.id.rest_interval_time_selection);
		restTimeSpinner.setOnItemSelectedListener(restTime);
		restTimeSpinner.setSelection(myPreferences.getInt(REST_INTERVAL_TIME, 0));

		Spinner restSpeedSpinner = (Spinner) findViewById(R.id.rest_speed_selection);
		restSpeedSpinner.setOnItemSelectedListener(selectRestSpeed);
		restSpeedSpinner.setSelection(myPreferences.getInt(REST_SPEED, 0));

		Spinner coolDownSpinner = (Spinner) findViewById(R.id.cooldown_selection);
		coolDownSpinner.setOnItemSelectedListener(coolDownType);
		coolDownSpinner.setSelection(myPreferences.getInt(COOLDOWN_TYPE, 2));

		Button startWorkoutButton = (Button) findViewById(R.id.start_workout);
		startWorkoutButton.setOnClickListener(startWorkout);

		configurePlaylistSelection(myPreferences);

		/**
		 * License Checks
		 */
		if (!licensed) {
			doCheck();
		}
	}

	/**
	 * Make sure we clean up the license checker
	 */
	protected void onDestroy() {
		super.onDestroy();
		if (mChecker != null) {
			mChecker.onDestroy();
		}
	}

	/**
	 * Find music playlists
	 * 
	 * @param myPreferences
	 */
	protected void configurePlaylistSelection(SharedPreferences myPreferences) {
		ArrayAdapter<CharSequence> playlists;
		playlists = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		playlists.add(HIITSession.PLAYLIST_NONE);
		playlistIds.add(Long.valueOf(-1));
		
		ContentResolver contentResolver = getContentResolver();
		Uri uri = android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
		Cursor cursor = contentResolver.query(uri, null, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {
			int nameColumn = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
			int idColumn = cursor.getColumnIndex(MediaStore.Audio.Playlists._ID);
			Log.i(TAG, "Found Playlist");


			// add each song to mItems
			do {
				Log.i(TAG, "Ading Playlist "+cursor.getString(nameColumn));
				playlists.add(cursor.getString(nameColumn));
				playlistIds.add(cursor.getLong(idColumn));
			} while (cursor.moveToNext());

			cursor.close();
		} else {
			Log.e(TAG, "Could not locate any play lists.");
		}
		
		configureAllMusicSelection(playlists);

		playlists.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner musicintegration = (Spinner) findViewById(R.id.playlist_selection);
		musicintegration.setAdapter(playlists);

		playlistName = myPreferences.getString(PLAYLIST_NAME, HIITSession.PLAYLIST_NONE);
		playlistId = myPreferences.getLong(PLAYLIST_ID, Long.valueOf(-1));
		int selection = playlists.getPosition(playlistName);
		musicintegration.setOnItemSelectedListener(selectPlaylist);
		if (selection < 0) {
			selection = 0;
		}
		musicintegration.setSelection(selection);
	}

	private void configureAllMusicSelection(ArrayAdapter<CharSequence> playlists) {
		ContentResolver contentResolver = getContentResolver();
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = contentResolver.query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
		
		if (cursor == null || !cursor.moveToFirst()) {
			Log.e(TAG, "Could not locate any music on device.");
			return;
		}

		cursor.close();
		playlists.add(HIITSession.PLAYLIST_ALL);
		playlistIds.add(Long.valueOf(-1));
	}

	/**
	 * Display disclaimer and accept or not
	 */
	private void showDisclaimer() {
		SharedPreferences myPreferences = getPreferences(MODE_PRIVATE);
		boolean acceptance = myPreferences.getBoolean(DISCLAIMER_ACCEPTANCE, false);

		// Disclaimer already accepted
		if (acceptance)
			return;

		Builder disclaimer = new Builder(this);
		disclaimer.setMessage(R.string.dislaimer);
		disclaimer.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			// do something when the button is clicked
			public void onClick(DialogInterface arg0, int arg1) {
				SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
				editor.putBoolean(DISCLAIMER_ACCEPTANCE, true).commit();
			}
		});
		disclaimer.setNegativeButton("No", new DialogInterface.OnClickListener() {

			// do something when the button is clicked
			public void onClick(DialogInterface arg0, int arg1) {
				SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
				editor.putBoolean(DISCLAIMER_ACCEPTANCE, false).commit();
			}
		});

		disclaimer.show();
	}

	/**
	 * Warm Up type
	 */
	private OnItemSelectedListener warmUpType = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> list, View view, int selection, long arg3) {
			SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
			editor.putInt(WARMUP_TYPE, selection).commit();
			workoutBuilder.setWarmUpType(WarmUpTypes.values()[selection]);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	/**
	 * Number Of Intervals
	 */
	private OnItemSelectedListener numberOfIntervals = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> list, View view, int selection, long arg3) {
			SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
			editor.putInt(NUMBER_OF_INTERVALS, selection).commit();
			workoutBuilder.setNumberOfIntervals(numberOfIntervalsArray[selection]);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	/**
	 * Sprint Time
	 */
	private OnItemSelectedListener sprintTime = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> list, View view, int selection, long arg3) {
			SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
			editor.putInt(SPRINT_INTERVAL_TIME, selection).commit();
			workoutBuilder.setSprintIntervalTime(sprintIntervalTimeArray[selection]);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	/**
	 * Sprint Speed
	 */
	private OnItemSelectedListener selectSpeed = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> list, View view, int selection, long arg3) {
			SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
			editor.putInt(SPRINT_SPEED, selection).commit();
			workoutBuilder.setSprintSpeed(sprintSpeedArray[selection]);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	/**
	 * Rest Time
	 */
	private OnItemSelectedListener restTime = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> list, View view, int selection, long arg3) {
			SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
			editor.putInt(REST_INTERVAL_TIME, selection).commit();
			workoutBuilder.setRestIntervalTime(restIntervalTimeArray[selection]);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	/**
	 * Rest Speed
	 */
	private OnItemSelectedListener selectRestSpeed = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> list, View view, int selection, long arg3) {
			SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
			editor.putInt(REST_SPEED, selection).commit();
			workoutBuilder.setRestSpeed(restSpeedArray[selection]);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	/**
	 * Cool Down type
	 */
	private OnItemSelectedListener coolDownType = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> list, View view, int selection, long arg3) {
			SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
			editor.putInt(COOLDOWN_TYPE, selection).commit();
			workoutBuilder.setCoolDownType(CoolDownTypes.values()[selection]);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	/**
	 * Start the workout
	 */
	private OnClickListener startWorkout = new OnClickListener() {
		public void onClick(View v) {
			SharedPreferences myPreferences = getPreferences(MODE_PRIVATE);
			boolean acceptance = myPreferences.getBoolean(DISCLAIMER_ACCEPTANCE, false);
			if (!acceptance) {
				showDisclaimer();
				return;
			}

			checkLicense();

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName(WorkoutSettings.this, HIITSession.class.getName());

			intent.putExtra(HIITSession.INTERVAL_WORKOUT, workoutBuilder.createIntervals());
			intent.putExtra(HIITSession.PLAYLIST, playlistName);
			intent.putExtra(HIITSession.PLAYLISTID, playlistId);

			// while (HIITSession.getIntervalWorkout() == null) {
			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// }
			// }
			startActivity(intent);
		}
	};

	/**
	 * Integrate music
	 */
	private OnItemSelectedListener selectPlaylist = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> list, View view, int selection, long arg3) {
			SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
			playlistName = list.getItemAtPosition(selection).toString();
			playlistId = playlistIds.get(selection);

			editor.putString(PLAYLIST_NAME, playlistName).commit();
			editor.putLong(PLAYLIST_ID, playlistId).commit();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	/**
	 * Licensing code
	 */

	protected void doCheck() {
		setProgressBarIndeterminateVisibility(true);
		String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

		// Construct the LicenseChecker with a Policy.
		ServerManagedPolicy policy = new ServerManagedPolicy(this, new AESObfuscator(SALT, getPackageName(), deviceId));
		mChecker = new LicenseChecker(this, policy, BASE64_PUBLIC_KEY);
		mChecker.checkAccess(new MyLicenseCheckerCallback());
	}

	/**
	 * See if we passed licensing checks
	 */
	protected void checkLicense() {
		if (!licensed) {
			showUnlicensed();
			return;
		}
	}

	protected void showUnlicensed() {
		// License validated
		if (licensed)
			return;

		Builder licenseNotifier = new Builder(this);
		licenseNotifier.setTitle(R.string.unlicensed_dialog_title);
		licenseNotifier.setMessage(R.string.unlicensed_dialog_body);
		licenseNotifier.setPositiveButton(R.string.buy_button, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/details?id="
						+ getPackageName()));
				startActivity(marketIntent);
				finish();
			}

		});
		licenseNotifier.setNegativeButton(R.string.exit_button, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		licenseNotifier.show();
	}

	private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
		@Override
		public void allow(int reason) {
			licensed = true;
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
		}

		@Override
		public void dontAllow(int reason) {
			licensed = false;
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			Log.e(TAG, "Licensing denyied reason: " + reason);
			showUnlicensed();
		}

		@Override
		public void applicationError(int errorCode) {
			Log.e(TAG, "Licensing issue code: " + errorCode);
			dontAllow(errorCode);
		}
	}

}
