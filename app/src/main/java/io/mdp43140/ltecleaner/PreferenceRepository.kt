/*
 * SPDX-FileCopyrightText: 2025 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.ltecleaner
import android.content.Context
import androidx.preference.PreferenceManager
class PreferenceRepository(ctx: Context){
	private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
	companion object {
		private const val autoWhite_key = "auto_white"
		private const val autoWhite_defVal = true
		private const val blacklist_key = "blacklist"
		private var blacklist_defVal = Constants.blacklistDefault
		private const val blacklistOn_key = "blacklistOn"
		private var blacklistOn_defVal = Constants.blacklistOnDefault
		private const val bootCleanup_key = "boot_cleanup"
		private const val bootCleanup_defVal = false
		private const val cleanApk_key = "clean_apk"
		private const val cleanApk_defVal = true
		private const val cleanClipboard_key = "clean_clipboard"
		private const val cleanClipboard_defVal = false
		private const val cleanCorpse_key = "clean_corpse"
		private const val cleanCorpse_defVal = false
		private const val cleanEmptyFile_key = "clean_empty_file"
		private const val cleanEmptyFile_defVal = true
		private const val cleanEmptyFolder_key = "clean_empty_folder"
		private const val cleanEmptyFolder_defVal = true
		private const val cleanEvery_key = "clean_every"
		private const val cleanEvery_defVal = -1
		private const val cleanGeneric_key = "clean_generic"
		private const val cleanGeneric_defVal = true
		private const val closeBgApps_key = "close_bg_apps"
		private const val closeBgApps_defVal = true
		private const val dynamicColor_key = "dynamic_color"
		private const val dynamicColor_defVal = true
		private const val multiRun_key = "multi_run"
		private const val multiRun_defVal = 1
		private const val oneClick_key = "one_click"
		private const val oneClick_defVal = false
		private const val pitchBlack_key = "pitch_black"
		private const val pitchBlack_defVal = false
		private const val runCount_key = "run_count"
		private const val runCount_defVal = 0
		private const val theme_key = "theme"
		private const val theme_defVal = "auto"
		private const val whitelist_key = "whitelist"
		private var whitelist_defVal = Constants.whitelistDefault
		private const val whitelistOn_key = "whitelistOn"
		private var whitelistOn_defVal = Constants.whitelistOnDefault
	}
	var autoWhite: Boolean
		get() = prefs.getBoolean(autoWhite_key,autoWhite_defVal)
		set(v) = prefs.edit().putBoolean(autoWhite_key,v).apply()
	var bootCleanup: Boolean
		get() = prefs.getBoolean(bootCleanup_key,bootCleanup_defVal)
		set(v) = prefs.edit().putBoolean(bootCleanup_key,v).apply()
	var blacklist: Set<String>
		get() = prefs.getStringSet(blacklist_key,blacklist_defVal) ?: blacklist_defVal
		set(v) = prefs.edit().putStringSet(blacklist_key,v).apply()
	var blacklistOn: Set<String>
		get() = prefs.getStringSet(blacklistOn_key,blacklistOn_defVal) ?: blacklistOn_defVal
		set(v) = prefs.edit().putStringSet(blacklistOn_key,v).apply()
	var cleanApk: Boolean
		get() = prefs.getBoolean(cleanApk_key,cleanApk_defVal)
		set(v) = prefs.edit().putBoolean(cleanApk_key,v).apply()
	var cleanClipboard: Boolean
		get() = prefs.getBoolean(cleanClipboard_key,cleanClipboard_defVal)
		set(v) = prefs.edit().putBoolean(cleanClipboard_key,v).apply()
	var cleanCorpse: Boolean
		get() = prefs.getBoolean(cleanCorpse_key,cleanCorpse_defVal)
		set(v) = prefs.edit().putBoolean(cleanCorpse_key,v).apply()
	var cleanEmptyFile: Boolean
		get() = prefs.getBoolean(cleanEmptyFile_key,cleanEmptyFile_defVal)
		set(v) = prefs.edit().putBoolean(cleanEmptyFile_key,v).apply()
	var cleanEmptyFolder: Boolean
		get() = prefs.getBoolean(cleanEmptyFolder_key,cleanEmptyFolder_defVal)
		set(v) = prefs.edit().putBoolean(cleanEmptyFolder_key,v).apply()
	var cleanEvery: Int
		get() = prefs.getInt(cleanEvery_key,cleanEvery_defVal)
		set(v) = prefs.edit().putInt(cleanEvery_key,v).apply()
	var cleanGeneric: Boolean
		get() = prefs.getBoolean(cleanGeneric_key,cleanGeneric_defVal)
		set(v) = prefs.edit().putBoolean(cleanGeneric_key,v).apply()
	var closeBgApps: Boolean
		get() = prefs.getBoolean(closeBgApps_key,closeBgApps_defVal)
		set(v) = prefs.edit().putBoolean(closeBgApps_key,v).apply()
	var dynamicColor: Boolean
		get() = prefs.getBoolean(dynamicColor_key,dynamicColor_defVal)
		set(v) = prefs.edit().putBoolean(dynamicColor_key,v).apply()
	var multiRun: Int
		get() = prefs.getInt(multiRun_key,multiRun_defVal)
		set(v) = prefs.edit().putInt(multiRun_key,v).apply()
	var oneClick: Boolean
		get() = prefs.getBoolean(oneClick_key,oneClick_defVal)
		set(v) = prefs.edit().putBoolean(oneClick_key,v).apply()
	var pitchBlack: Boolean
		get() = prefs.getBoolean(pitchBlack_key,pitchBlack_defVal)
		set(v) = prefs.edit().putBoolean(pitchBlack_key,v).apply()
	var runCount: Int
		get() = prefs.getInt(runCount_key,runCount_defVal)
		set(v) = prefs.edit().putInt(runCount_key,v).apply()
	var theme: String
		get() = prefs.getString(theme_key,theme_defVal) ?: theme_defVal
		set(v) = prefs.edit().putString(theme_key,v).apply()
	var whitelist: Set<String>
		get() = prefs.getStringSet(whitelist_key,whitelist_defVal) ?: whitelist_defVal
		set(v) = prefs.edit().putStringSet(whitelist_key,v).apply()
	var whitelistOn: Set<String>
		get() = prefs.getStringSet(whitelistOn_key,whitelistOn_defVal) ?: whitelistOn_defVal
		set(v) = prefs.edit().putStringSet(whitelistOn_key,v).apply()
}
