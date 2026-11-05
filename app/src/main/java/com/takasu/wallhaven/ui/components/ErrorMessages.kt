package com.takasu.wallhaven.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.takasu.wallhaven.R
import java.io.IOException
import retrofit2.HttpException

@Composable
fun errorMessage(error: Throwable?): String = when {
	error is HttpException && error.code() == 401 -> stringResource(R.string.error_invalid_key)
	error is HttpException && error.code() == 429 -> stringResource(R.string.error_rate_limit)
	error is IOException -> stringResource(R.string.error_network)
	else -> stringResource(R.string.error_generic)
}
