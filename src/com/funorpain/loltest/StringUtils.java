package com.funorpain.loltest;

import java.util.Locale;

public class StringUtils {
	public static String formatTime(int time) {
		int min = time / 1000 / 60;
		int second = (time / 1000) % 60;
		return String.format(Locale.US, "%d:%02d", min, second);
	}
}
