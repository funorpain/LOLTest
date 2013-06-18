package com.funorpain.loltest;

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

public class SelectActivity extends ListActivity {

	private ArrayAdapter<String> mAdapter;
	private int mLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences settings = getSharedPreferences(Config.PREFS_NAME, 0);
		mLevel = settings.getInt("level", 0);
		mAdapter = new ListAdapter(this, R.layout.select_list_item,
				getResources().getStringArray(R.array.stages));
		setListAdapter(mAdapter);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		SharedPreferences settings = getSharedPreferences(Config.PREFS_NAME, 0);
		mLevel = settings.getInt("level", 0);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position > mLevel) {
			return;
		}
		Intent intent = new Intent(this, SubSelectActivity.class);
		intent.putExtra(SubSelectActivity.STAGE, position);
		startActivity(intent);
	}

	private class ListAdapter extends ArrayAdapter<String> {

		private LayoutInflater mInflater;
		private int mResourceId;

		public ListAdapter(Context context, int textViewResourceId,
				String[] objects) {
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
			text.setText(parts[0]);
			description.setText(parts[1]);
			if (position < mLevel) {
				state.setText(R.string.graduate);
			} else if (position == mLevel) {
				state.setText(R.string.reading);
			} else {
				state.setText("");
			}
			return view;
		}
	}

}
