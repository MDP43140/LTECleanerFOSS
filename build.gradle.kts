/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
buildscript {
	repositories {
		google()
		mavenCentral()
		maven("https://jitpack.io")
	}
	dependencies {
		classpath("com.android.tools.build:gradle:8.3.0")
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
	}
}
allprojects {
	tasks.withType<JavaCompile> {
		options.compilerArgs.addAll(listOf("-Xlint:unchecked","-Xlint:deprecation"))
	}
}
tasks.register("clean",Delete::class){
	delete(rootProject.layout.buildDirectory)
}