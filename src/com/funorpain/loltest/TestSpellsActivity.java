package com.funorpain.loltest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.funorpain.loltest.model.Spell;

public class TestSpellsActivity extends Activity {
	private static final String TAG = "TestSpellsActivity";
	private static final String ABORT = "abort";
	private static final int CORRECT_DELAY = 200;
	private static final int INCORRECT_DELAY = 2000;

	private boolean mAbort;
	private Button mOk;

	private List<Spell> mSpells;
	private int mAnswered;
	private int mRight;
	private long mStartTime;
	private Random mRandom;
	private TextView mQuestion;
	private ImageView mIcon;
	private ImageView mResult1;
	private ImageView mResult2;
	private ImageView mResult3;
	private ImageView mResult4;
	private RadioButton mAnswer1;
	private RadioButton mAnswer2;
	private RadioButton mAnswer3;
	private RadioButton mAnswer4;
	private Handler mHandler = new Handler();
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if (mAbort) {
				return;
			}
			mAnswer1.setEnabled(true);
			mAnswer2.setEnabled(true);
			mAnswer3.setEnabled(true);
			mAnswer4.setEnabled(true);
			mResult1.setVisibility(View.INVISIBLE);
			mResult2.setVisibility(View.INVISIBLE);
			mResult3.setVisibility(View.INVISIBLE);
			mResult4.setVisibility(View.INVISIBLE);
			if (mAnswered >= mSpells.size()) {
				int score = 100 * mRight / mAnswered;
				int time = (int) (System.nanoTime() / 1000000 - mStartTime);
				boolean pass = mRight >= mSpells.size();
				if (pass) {
					SharedPreferences settings = getSharedPreferences(
							Config.PREFS_NAME, 0);
					int level = settings.getInt("level", 0);
					int subLevel = settings.getInt("sublevel", 0);
					int score_0_2 = settings.getInt("score.0.2", 0);
					int time_0_2 = settings.getInt("time.0.2",
							Config.DEFAULT_TIME);
					Editor editor = settings.edit();
					if (score_0_2 < score || score_0_2 == score
							&& time_0_2 > time) {
						editor.putInt("score.0.2", score);
						editor.putInt("time.0.2", time);
					}
					if (level == 0 && subLevel == 2) {
						editor.putInt("level", 0).putInt("sublevel", 3);
					}
					editor.commit();
				}

				Intent intent = new Intent(TestSpellsActivity.this,
						StageEndActivity.class);
				intent.putExtra(StageEndActivity.SCORE, score);
				intent.putExtra(StageEndActivity.TIME, time);
				intent.putExtra(StageEndActivity.PASS, pass);
				startActivity(intent);
				finish();
				return;
			}
			nextQuestion();
		}
	};

	private OnClickListener mListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mAnswer1.setEnabled(false);
			mAnswer2.setEnabled(false);
			mAnswer3.setEnabled(false);
			mAnswer4.setEnabled(false);
			String right = mSpells.get(mAnswered).getName();
			RadioButton button = (RadioButton) v;
			ViewGroup vg = (ViewGroup) v.getParent();
			ImageView result = (ImageView) vg.findViewWithTag("result");
			mAnswered++;
			if (button.getText().toString().equals(right)) {
				mRight++;
				result.setImageResource(R.drawable.ic_correct);
				result.setVisibility(View.VISIBLE);
				mHandler.postDelayed(mRunnable, CORRECT_DELAY);
			} else {
				result.setImageResource(R.drawable.ic_incorrect);
				result.setVisibility(View.VISIBLE);
				if (mAnswer1.getText().toString().equals(right)) {
					mResult1.setImageResource(R.drawable.ic_correct);
					mResult1.setVisibility(View.VISIBLE);
				} else if (mAnswer2.getText().toString().equals(right)) {
					mResult2.setImageResource(R.drawable.ic_correct);
					mResult2.setVisibility(View.VISIBLE);
				} else if (mAnswer3.getText().toString().equals(right)) {
					mResult3.setImageResource(R.drawable.ic_correct);
					mResult3.setVisibility(View.VISIBLE);
				} else if (mAnswer4.getText().toString().equals(right)) {
					mResult4.setImageResource(R.drawable.ic_correct);
					mResult4.setVisibility(View.VISIBLE);
				}
				mHandler.postDelayed(mRunnable, INCORRECT_DELAY);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mAbort = savedInstanceState.getBoolean(ABORT);
		}
		if (mAbort) {
			initAbortView();
			return;
		}
		mSpells = parseSpells(readTextFile("spells/spells.txt"));
		if (mSpells == null) {
			finish();
			return;
		}
		mAnswered = 0;
		mRight = 0;
		mRandom = new Random();
		Collections.shuffle(mSpells);
		setContentView(R.layout.activity_test_spells);
		mQuestion = (TextView) findViewById(R.id.question);
		mIcon = (ImageView) findViewById(R.id.icon);
		mResult1 = (ImageView) findViewById(R.id.result1);
		mResult2 = (ImageView) findViewById(R.id.result2);
		mResult3 = (ImageView) findViewById(R.id.result3);
		mResult4 = (ImageView) findViewById(R.id.result4);
		mAnswer1 = (RadioButton) findViewById(R.id.answer1);
		mAnswer2 = (RadioButton) findViewById(R.id.answer2);
		mAnswer3 = (RadioButton) findViewById(R.id.answer3);
		mAnswer4 = (RadioButton) findViewById(R.id.answer4);
		mAnswer1.setOnClickListener(mListener);
		mAnswer2.setOnClickListener(mListener);
		mAnswer3.setOnClickListener(mListener);
		mAnswer4.setOnClickListener(mListener);
		nextQuestion();
		mStartTime = System.nanoTime() / 1000000;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAbort && mOk == null) {
			initAbortView();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mAbort = true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(ABORT, mAbort);
	}

	private void initAbortView() {
		setContentView(R.layout.activity_test_abort);
		mOk = (Button) findViewById(R.id.ok);
		mOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private String readTextFile(String fileName) {
		StringWriter writer = new StringWriter();
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(getAssets().open(fileName), "UTF-8");
			char[] buf = new char[4096];
			while (true) {
				int n = reader.read(buf);
				if (n == -1) {
					break;
				} else if (n > 0) {
					writer.write(buf, 0, n);
				}
			}
			return writer.toString();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private Bitmap readBitmapFile(String fileName) {
		InputStream in = null;
		try {
			in = getAssets().open(fileName);
			return BitmapFactory.decodeStream(in);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private List<Spell> parseSpells(String data) {
		if (data == null) {
			return null;
		}
		List<Spell> result = new ArrayList<Spell>();
		String[] lines = data.split("\n");
		for (String line : lines) {
			String[] parts = line.split("\t");
			Spell spell = new Spell();
			spell.setId(Integer.valueOf(parts[0]));
			spell.setName(parts[1]);
			spell.setLevel(parts[2]);
			spell.setDescription(parts[3]);
			result.add(spell);
		}
		return result;
	}

	private boolean nextQuestion() {
		if (mAnswered >= mSpells.size()) {
			return false;
		}
		mQuestion.setText(getString(R.string.question, mAnswered + 1));
		mIcon.setImageBitmap(readBitmapFile("spells/icons/"
				+ mSpells.get(mAnswered).getId() + ".jpg"));
		Set<Integer> set = new HashSet<Integer>();
		set.add(mAnswered);
		while (set.size() < 4) {
			set.add(mRandom.nextInt(mSpells.size()));
		}
		List<Integer> list = new ArrayList<Integer>();
		list.addAll(set);
		Collections.shuffle(list);

		mAnswer1.setText(mSpells.get(list.get(0)).getName());
		mAnswer1.setChecked(false);
		mAnswer2.setText(mSpells.get(list.get(1)).getName());
		mAnswer2.setChecked(false);
		mAnswer3.setText(mSpells.get(list.get(2)).getName());
		mAnswer3.setChecked(false);
		mAnswer4.setText(mSpells.get(list.get(3)).getName());
		mAnswer4.setChecked(false);
		return true;
	}

}
