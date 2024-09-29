/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
plugins {
	id("com.android.application") version "8.6.1" apply false
	id("org.jetbrains.kotlin.android") version "2.0.20" apply false
}
tasks.withType(JavaCompile::class.java){
	options.compilerArgs.add("-Xlint:all")
}
tasks.register("clean",Delete::class){
	delete(rootProject.layout.buildDirectory)
}