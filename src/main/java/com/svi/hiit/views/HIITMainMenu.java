package com.svi.hiit.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.svi.hiit.R;

public class HIITMainMenu extends Activity {
	
	
	private Button intervalButton;
	private Button progressiveButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		
		intervalButton = (Button) findViewById(R.id.menu_interval_button);
		intervalButton.setOnClickListener(intervalWorkout);

		progressiveButton = (Button) findViewById(R.id.menu_progressive_button);
		progressiveButton.setOnClickListener(progressiveWorkout);

	}
	
	private OnClickListener intervalWorkout = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName(HIITMainMenu.this, WorkoutSettings.class.getName());
			startActivity(intent);

		}
	};

	private OnClickListener progressiveWorkout = new OnClickListener() {
		public void onClick(View v) {
		}
	};



}
