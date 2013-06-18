package com.funorpain.loltest;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView mWelcome;
	private Button mStart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mWelcome = (TextView) findViewById(R.id.welcome);
		mStart = (Button) findViewById(R.id.start);
		mStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						SelectActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		SharedPreferences settings = getSharedPreferences(Config.PREFS_NAME, 0);
		int level = settings.getInt("level", 0);
		String[] levels = getResources().getStringArray(R.array.levels);
		mWelcome.setText(getString(R.string.welcome, levels[level]));
	}

}
