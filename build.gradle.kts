import org.gradle.api.tasks.bundling.AbstractArchiveTask

// build.gradle.kts (root)
plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.kotlin.android) apply false
	alias(libs.plugins.kotlin.compose) apply false
	alias(libs.plugins.hilt.android) apply false
	alias(libs.plugins.ksp) apply false
	alias(libs.plugins.kotlin.serialization) apply false
}

// Deterministic archives: keep the F-Droid build reproducible.
subprojects {
	tasks.withType<AbstractArchiveTask>().configureEach {
		isPreserveFileTimestamps = false
		isReproducibleFileOrder = true
	}
}
