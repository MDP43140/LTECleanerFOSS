/*
 * SPDX-FileCopyrightText: 2020-2023 Hunter J Drum
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
import java.util.Properties // used by signingConfigs.release (ksProps variable)
import com.android.build.gradle.tasks.PackageAndroidArtifact // used by empty app-metadata.properties

plugins {
	id("com.android.application")
	kotlin("android")
}
kotlin {
	// Used as defaults for android.kotlinOptions.jvmTarget and android.compileOptions.*Compatibility
	jvmToolchain(21)
}
android {
	compileSdk = 35
	buildToolsVersion = "35.0.0"
	namespace = "theredspy15.ltecleanerfoss"
	defaultConfig {
		applicationId = "io.mdp43140.ltecleaner"
		minSdk = 24
		targetSdk = compileSdk
		versionCode = 64
		versionName = "5.0.1"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		testInstrumentationRunnerArguments["disableAnalytics"] = "true"
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
		checkReleaseBuilds = false // we did thousands of these on debug builds already...
		lintConfig = file("lint.xml")
	}
	buildTypes {
		debug {
			applicationIdSuffix = ".debug"
			isDebuggable = true
		}
		release {
			isMinifyEnabled = true
			isShrinkResources = true
		//isCrunchPngs = true // no longer needed, since the PNGs are optimized in the first place before compiling
			isDebuggable = false
			isProfileable = false
			isJniDebuggable = false
			isPseudoLocalesEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
			signingConfig = signingConfigs.getByName("main")
			vcsInfo.include = false
			postprocessing {
				isRemoveUnusedCode = true
				isRemoveUnusedResources = true
				isObfuscate = false
				isOptimizeCode = true
			}
		}
	}
	androidResources {
		// Allows changing app language through Android settings
		// Does sacrifice couple kilobytes though, but probably worth it
		generateLocaleConfig = true
	}
	buildFeatures {
		buildConfig = false
		compose = false
		viewBinding = true
	}
	packaging {
	// makes the app 1MB bigger :(
		dex {
			useLegacyPackaging = false
		}
		jniLibs {
			useLegacyPackaging = false
		}
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
	// empty app-metadata.properties (not removing it sadly)
	// https://stackoverflow.com/a/77745844
	tasks.withType<PackageAndroidArtifact> {
		doFirst { appMetadata.asFile.orNull?.writeText("") }
	}
}
dependencies {
	// AndroidX App Compatibility
	implementation("androidx.appcompat:appcompat:1.7.0")
	// AndroidX Kotlin
	implementation("androidx.core:core-ktx:1.15.0")
	// GridLayout (used in MainActivity for 2x2 grid buttons, implementation without this is much more preferred)
	implementation("androidx.gridlayout:gridlayout:1.1.0-beta01")
	// Preference
	implementation("androidx.preference:preference-ktx:1.2.1")
	// Background service
	implementation("androidx.work:work-runtime-ktx:2.10.0")
	// MD3 on different Android versions
	implementation("com.google.android.material:material:1.12.0")
	// Leak detection
	debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
	// Error logger
	implementation("com.github.mdp43140.ael:ael_kt:1.0.1-hf")
	// Tests (AndroidJUnitRunner & JUnit Rules, Assertions)
	androidTestImplementation("androidx.test:runner:1.6.2")
	androidTestImplementation("androidx.test.ext:junit:1.2.1")
}