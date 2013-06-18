package com.funorpain.loltest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StageEndActivity extends Activity {
	public static final String SCORE = "com.funorpain.loltest.score";
	public static final String TIME = "com.funorpain.loltest.time";
	public static final String PASS = "com.funorpain.loltest.pass";

	private TextView mScore;
	private TextView mTime;
	private TextView mText;
	private Button mOk;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int score = getIntent().getIntExtra(SCORE, -1);
		int time = getIntent().getIntExtra(TIME, -1);
		if (score == -1 || time == -1) {
			throw new IllegalArgumentException(
					"StageEndActivity must have score and time.");
		}
		boolean pass = getIntent().getBooleanExtra(PASS, true);
		setContentView(R.layout.activity_stage_end);
		mScore = (TextView) findViewById(R.id.score);
		mTime = (TextView) findViewById(R.id.time);
		mText = (TextView) findViewById(R.id.text);
		mOk = (Button) findViewById(R.id.ok);
		mScore.setText(String.valueOf(score));
		mScore.setTextColor(getResources().getColor(
				pass ? R.color.green : R.color.red));
		mTime.setText(StringUtils.formatTime(time));
		mText.setText(getString(pass ? R.string.pass : R.string.failed));
		mOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}
