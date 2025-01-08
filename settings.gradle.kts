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
//includeBuild("../ael"){
//	dependencySubstitution {
//		substitute(module("com.github.mdp43140.ael:ael_kt")).using(project(":ael_kt"))
//	}
//}

rootProject.name = "LTE Cleaner"
include(":app")