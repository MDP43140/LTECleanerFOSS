/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
		maven("https://jitpack.io")
	}
}
// TODO: replacing project with dep substitution throws "unresolved reference ErrorLogger" error
//includeBuild("../ael"){
//	dependencySubstitution {
//		substitute(module("io.mdp43140:ael-kt")).using(project(":ael_kt"))
//	}
//}

rootProject.name = "LTE Cleaner"
include(":app",":ael_kt")