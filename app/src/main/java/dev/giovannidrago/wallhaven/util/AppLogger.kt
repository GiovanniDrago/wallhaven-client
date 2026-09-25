package dev.giovannidrago.wallhaven.util

import android.util.Log
import javax.inject.Inject

class AppLogger @Inject constructor() {
	fun debug(tag: String, message: String) {
		Log.d(tag, message)
	}

	fun warn(tag: String, message: String) {
		Log.w(tag, message)
	}

	fun error(tag: String, message: String, throwable: Throwable? = null) {
		if (throwable == null) {
			Log.e(tag, message)
		} else {
			Log.e(tag, message, throwable)
		}
	}
}
