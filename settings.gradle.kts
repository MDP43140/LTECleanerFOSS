/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
		maven("https://jitpack.io")
	}
}

rootProject.name = "LTE Cleaner"
include(":app")