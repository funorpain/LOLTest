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

import com.funorpain.loltest.model.Item;

public class TestItemsActivity extends Activity {
	private static final String TAG = "TestItemsActivity";
	private static final String ABORT = "abort";
	private static final int CORRECT_DELAY = 200;
	private static final int INCORRECT_DELAY = 2000;
	private static final int QUESTION_COUNT = 50;
	private static final int PASS_COUNT = 30;

	private boolean mAbort;
	private Button mOk;

	private List<Item> mItems;
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
			if (mAnswered >= QUESTION_COUNT) {
				int score = 100 * mRight / mAnswered;
				int time = (int) (System.nanoTime() / 1000000 - mStartTime);
				boolean pass = mRight >= PASS_COUNT;
				if (pass) {
					SharedPreferences settings = getSharedPreferences(
							Config.PREFS_NAME, 0);
					int level = settings.getInt("level", 0);
					int subLevel = settings.getInt("sublevel", 0);
					int score_0_1 = settings.getInt("score.0.1", 0);
					int time_0_1 = settings.getInt("time.0.1",
							Config.DEFAULT_TIME);
					Editor editor = settings.edit();
					if (score_0_1 < score || score_0_1 == score
							&& time_0_1 > time) {
						editor.putInt("score.0.1", score);
						editor.putInt("time.0.1", time);
					}
					if (level == 0 && subLevel == 1) {
						editor.putInt("level", 0).putInt("sublevel", 2);
					}
					editor.commit();
				}

				Intent intent = new Intent(TestItemsActivity.this,
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
			String right = mItems.get(mAnswered).getName();
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
		mItems = parseItems(readTextFile("items/items.txt"));
		if (mItems == null) {
			finish();
			return;
		}
		mAnswered = 0;
		mRight = 0;
		mRandom = new Random();
		Collections.shuffle(mItems);
		setContentView(R.layout.activity_test_items);
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

	private List<Item> parseItems(String data) {
		if (data == null) {
			return null;
		}
		List<Item> result = new ArrayList<Item>();
		String[] lines = data.split("\n");
		for (String line : lines) {
			String[] parts = line.split(" ");
			String[] filters = parts[3].split(",");
			Set<String> filterSet = new HashSet<String>();
			for (String filter : filters) {
				filterSet.add(filter);
			}
			Item item = new Item();
			item.setId(Integer.valueOf(parts[0]));
			item.setName(parts[1]);
			item.setPrice(Integer.valueOf(parts[2]));
			item.setFilters(filterSet);
			item.setAttr(parts[4]);
			result.add(item);
		}
		return result;
	}

	private boolean nextQuestion() {
		if (mAnswered >= mItems.size()) {
			return false;
		}
		mQuestion.setText(getString(R.string.question, mAnswered + 1));
		mIcon.setImageBitmap(readBitmapFile("items/icons/"
				+ mItems.get(mAnswered).getId() + ".png"));
		Set<Integer> set = new HashSet<Integer>();
		set.add(mAnswered);
		while (set.size() < 4) {
			set.add(mRandom.nextInt(mItems.size()));
		}
		List<Integer> list = new ArrayList<Integer>();
		list.addAll(set);
		Collections.shuffle(list);

		mAnswer1.setText(mItems.get(list.get(0)).getName());
		mAnswer1.setChecked(false);
		mAnswer2.setText(mItems.get(list.get(1)).getName());
		mAnswer2.setChecked(false);
		mAnswer3.setText(mItems.get(list.get(2)).getName());
		mAnswer3.setChecked(false);
		mAnswer4.setText(mItems.get(list.get(3)).getName());
		mAnswer4.setChecked(false);
		return true;
	}

}
