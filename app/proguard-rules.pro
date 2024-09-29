// We're open source, so dont obfuscate
-dontobfuscate

// Optimize multiple times
-optimizationpasses 9

// Don't obfuscate causes the gradle build to fail after optimization step
// !code/allocation/variable is needed to prevent it
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

-allowaccessmodification

// Keep important stacktrace
-keepattributes SourceFile,LineNumberTable

// Needed just for androidx.preference.PreferenceManager
-renamesourcefileattribute SourceFile