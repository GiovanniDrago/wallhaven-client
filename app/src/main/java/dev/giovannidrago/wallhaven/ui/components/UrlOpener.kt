package dev.giovannidrago.wallhaven.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openUrl(context: Context, url: String): Boolean {
	if (url.isBlank()) {
		return false
	}

	return runCatching {
		context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
	}.isSuccess
}
