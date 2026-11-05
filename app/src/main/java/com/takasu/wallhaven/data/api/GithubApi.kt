package com.takasu.wallhaven.data.api

import com.takasu.wallhaven.data.api.dto.GithubReleaseDto
import retrofit2.http.GET
import retrofit2.http.Header

interface GithubApi {
	@GET("repos/GiovanniDrago/wallhaven-client/releases/latest")
	suspend fun latestRelease(
		@Header("Accept") accept: String,
		@Header("User-Agent") userAgent: String
	): GithubReleaseDto
}
