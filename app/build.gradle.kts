import com.android.build.api.variant.HostTestBuilder
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.hilt.android)
	alias(libs.plugins.ksp)
	alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
	val file = rootProject.file("local.properties")
	if (file.exists()) {
		file.inputStream()
			.use { load(it) }
	}
}

val keystorePassword: String? = System.getenv("KEYSTORE_PASSWORD") ?: localProperties.getProperty("KEYSTORE_PASSWORD")

val donationUrl: String = (project.findProperty("DONATION_URL") as String?)
	?.trim()
	?: "https://buymeacoffee.com/_takasu_"

android {
	namespace = "dev.giovannidrago.wallhaven"
	compileSdk = 37

	defaultConfig {
		applicationId = "dev.giovannidrago.wallhaven"
		minSdk = 26
		targetSdk = 37
		versionCode = 5
		versionName = "0.5.0"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		buildConfigField("String", "DONATION_URL", "\"$donationUrl\"")
	}

	signingConfigs {
		create("release") {
			storeFile = file("../release.keystore")
			storePassword = keystorePassword
			keyAlias = "wallhavend"
			keyPassword = keystorePassword
		}
	}

	buildTypes {
		release {
			signingConfig = signingConfigs.getByName("release")
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	buildFeatures {
		compose = true
		buildConfig = true
	}

	testOptions {
		unitTests {
			isReturnDefaultValues = true
			all {
				it.testLogging {
					events("passed", "skipped", "failed")
				}
			}
		}
	}
}

// Unit-test coverage for the debug variant (AGP 9 per-variant host test DSL).
// AGP applies JaCoCo itself and registers `createDebugUnitTestCoverageReport`,
// which writes HTML plus `report.xml` under build/reports/coverage/.
androidComponents {
	beforeVariants(selector().withBuildType("debug")) { variantBuilder ->
		variantBuilder.hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enableCodeCoverage = true
	}
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_17
	}
}

dependencies {
	val composeBom = platform(libs.compose.bom)
	implementation(composeBom)
	implementation(libs.compose.ui)
	implementation(libs.compose.ui.tooling.preview)
	implementation(libs.compose.material3)
	implementation(libs.compose.material.icons.extended)
	debugImplementation(libs.compose.ui.tooling)

	implementation(libs.androidx.navigation.compose)
	implementation(libs.androidx.lifecycle.viewmodel.compose)
	implementation(libs.androidx.lifecycle.runtime.compose)

	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)
	implementation(libs.androidx.hilt.navigation.compose)

	implementation(libs.retrofit)
	implementation(libs.retrofit.kotlinx.serialization.converter)
	implementation(libs.okhttp)
	implementation(libs.okhttp.logging.interceptor)
	implementation(libs.kotlinx.serialization.json)

	implementation(libs.androidx.datastore.preferences)
	implementation(libs.coil.compose)
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.activity.compose)

	implementation(libs.room.runtime)
	implementation(libs.room.ktx)
	implementation(libs.room.paging)
	ksp(libs.room.compiler)

	implementation(libs.paging.runtime)
	implementation(libs.paging.compose)

	testImplementation(libs.junit)
	testImplementation(libs.kotlinx.coroutines.test)
	testImplementation(libs.mockk)
	testImplementation(libs.okhttp.mockwebserver)
}
