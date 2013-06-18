package com.funorpain.loltest;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SubSelectActivity extends ListActivity {
	public static final String STAGE = "com.funorpain.loltest.stage";

	private ArrayAdapter<String> mAdapter;
	private int mStage;
	private int mLevel;
	private int mSubLevel;
	private int mScore_0_0;
	private int mTime_0_0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] stages = getResources().getStringArray(R.array.stages);
		String[] subStages = getResources().getStringArray(R.array.sub_stages);
		mStage = this.getIntent().getIntExtra(STAGE, -1);
		if (mStage < 0 || mStage >= stages.length) {
			throw new IllegalArgumentException(
					"SubSelectActivity must have a valid stage.");
		}
		String stageStr = String.valueOf(mStage);
		ArrayList<String> list = new ArrayList<String>();
		for (String s : subStages) {
			String[] parts = s.split("\\|");
			if (parts[0].equals(stageStr)) {
				list.add(s);
			}
		}
		SharedPreferences settings = getSharedPreferences(Config.PREFS_NAME, 0);
		mLevel = settings.getInt("level", 0);
		mSubLevel = settings.getInt("sublevel", 0);
		mScore_0_0 = settings.getInt("score.0.0", 0);
		mTime_0_0 = settings.getInt("time.0.0", Config.DEFAULT_TIME);
		mAdapter = new ListAdapter(this, R.layout.select_list_item, list);
		setListAdapter(mAdapter);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		SharedPreferences settings = getSharedPreferences(Config.PREFS_NAME, 0);
		mLevel = settings.getInt("level", 0);
		mSubLevel = settings.getInt("sublevel", 0);
		mScore_0_0 = settings.getInt("score.0.0", 0);
		mTime_0_0 = settings.getInt("time.0.0", Config.DEFAULT_TIME);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (!canPlay(position)) {
			return;
		}
		if (mStage == 0 && position == 0) {
			Intent intent = new Intent(this, TestActivity.class);
			startActivity(intent);
		}
	}

	private boolean canPlay(int position) {
		return mLevel > mStage || mLevel == mStage && mSubLevel >= position;
	}

	private class ListAdapter extends ArrayAdapter<String> {

		private LayoutInflater mInflater;
		private int mResourceId;

		public ListAdapter(Context context, int textViewResourceId,
				ArrayList<String> objects) {
			super(context, textViewResourceId, objects);
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mResourceId = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = mInflater.inflate(mResourceId, parent, false);
			} else {
				view = convertView;
			}
			TextView text = (TextView) view.findViewById(R.id.text);
			TextView description = (TextView) view
					.findViewById(R.id.description);
			TextView state = (TextView) view.findViewById(R.id.state);
			String item = getItem(position);
			String[] parts = item.split("\\|");
			text.setText(parts[1]);
			description.setText(parts[2]);
			if (canPlay(position)) {
				if (mLevel == mStage && mSubLevel == position) {
					state.setText(R.string.reading);
				} else {
					state.setText(R.string.complete);
					if (mLevel == 0 && position == 0) {
						state.setText(mScore_0_0 < 100 ? String
								.valueOf(mScore_0_0) : StringUtils
								.formatTime(mTime_0_0));
					}
				}
			} else {
				state.setText("");
			}
			return view;
		}
	}

}
