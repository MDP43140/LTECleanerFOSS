/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
import java.util.Properties // used by signingConfigs.release (ksProps variable)

plugins {
	id("com.android.application")
	kotlin("android")
}
kotlin {
	jvmToolchain(21)
}
android {
	compileSdk = 34
	buildToolsVersion = "34.0.0"
	namespace = "theredspy15.ltecleanerfoss"
	defaultConfig {
		applicationId = namespace
		minSdk = 21
		targetSdk = compileSdk
		versionCode = 63
		versionName = "5.0.0"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		vectorDrawables {
			useSupportLibrary = false
		}
	}
	signingConfigs {
		create("main"){
			val ksPropsFile = rootProject.file(".signing/keystore.properties")
			if (ksPropsFile.exists()){
				val ksProps = Properties().apply {
					load(ksPropsFile.inputStream())
				}
				keyAlias = ksProps["keyAlias"] as String
				keyPassword = ksProps["keyPassword"] as String
				storeFile = file(ksProps["storeFile"] as String)
				storePassword = ksProps["storePassword"] as String
			}
		}
	}
	lint {
		abortOnError = false
		lintConfig = file("lint.xml")
	}
	buildTypes {
		getByName("debug") {
			isDebuggable = true
			applicationIdSuffix = ".debug"
		}
		getByName("release") {
			isMinifyEnabled = true
			isShrinkResources = true
		//crunchPngs = true // no longer needed, since the PNGs are optimized in the first place before compiling
			isDebuggable = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
			signingConfig = signingConfigs.getByName("main")
			postprocessing {
				isRemoveUnusedCode = true
				isRemoveUnusedResources = true
				isObfuscate = false
				isOptimizeCode = true
			}
		}
	}
	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_21.toString()
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
	buildFeatures {
		buildConfig = false
		compose = false
		viewBinding = true
	}
	packaging {
		// TODO: how can we get rid of assets/dexopt, and META-INF/com/android/build/gradle/app-metadata.properties ?
		resources {
			excludes += listOf(
				"assets/dexopt/baseline.prof",
				"assets/dexopt/baseline.profm",
				"kotlin/**.kotlin_builtins",
				"META-INF/**", // including com/android/build/gradle/app-metadata.properties, services/**, version-control-info.textproto
				"DebugProbesKt.bin",
				"kotlin-tooling-metadata.json"
			)
		}
	}
	dependenciesInfo {
		// https://gitlab.com/IzzyOnDroid/repo/-/issues/491
		includeInApk = false
		includeInBundle = false
	}
}
dependencies {
	//implementation fileTree(dir: 'libs', include: ['*.jar'])

	// Basic UIs
	//noinspection GradleCompatible
	// AndroidX App Compatibility
	implementation("androidx.appcompat:appcompat:1.7.0-alpha03")
	// AndroidX Kotlin
	implementation("androidx.core:core-ktx:1.13.0-alpha05")
	// GridLayout (used in MainActivity for 2x2 grid buttons, implementation without this is much more preferred)
	implementation("androidx.gridlayout:gridlayout:1.1.0-beta01")
	// Preference
	implementation("androidx.preference:preference-ktx:1.2.1")
	// Background service
	implementation("androidx.work:work-runtime-ktx:2.9.0")
	// MD3 on different Android versions
	implementation("com.google.android.material:material:1.12.0-alpha03")

	// Tests (AndroidJUnitRunner & JUnit Rules, Assertions)
	androidTestImplementation("androidx.test:runner:1.5.2")
	androidTestImplementation("androidx.test.ext:junit:1.1.5")
}