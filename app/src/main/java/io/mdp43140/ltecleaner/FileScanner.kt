/*
 * SPDX-FileCopyrightText: 2020-2023 Hunter J Drum
 * SPDX-FileCopyrightText: 2024-2025 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.ltecleaner
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.widget.TextView
import androidx.preference.PreferenceManager
import io.mdp43140.ltecleaner.fragment.BlacklistFragment
import io.mdp43140.ltecleaner.fragment.WhitelistFragment
import java.io.File
import java.util.Locale
class FileScanner(private val path: File, context: Context){
	// TODO: Ability to clean SD Card? Already tried SAF implementation, but its really hard, and soon i realized it has storage access restrictions: https://developer.android.com/training/data-storage/shared/documents-files#document-tree-access-restrictions
	// TODO: do whitelist & blacklist system in the same function instead of being separate
	private var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
	private val context = context
	private var res: Resources = context.resources
	private var filesRemoved = 0
	private var kilobytesTotal: Long = 0
	var delete = false
	var emptyFile = prefs.getBoolean("clean_empty_file", false)
	var emptyDir = prefs.getBoolean("clean_empty_folder", false)
	var autoWhite = prefs.getBoolean("auto_white", true)
	var corpse = prefs.getBoolean("clean_corpse", false)
	var updateProgress: ((context: Context, percent: Double) -> Unit)? = null
	var addText: ((context: Context, path: String, type: Int) -> TextView?)? = null
	private var installedPackages = getInstalledPackages()
	private var guiScanProgressMax = 0
	private var guiScanProgressProgress = 0
	private var foundFiles: ArrayList<File>? = null

	/**
	 * Used to generate a list of all files on device
	 * @param parentDirectory where to start searching from
	 * @return List of all files on device (besides whitelisted ones)
	 */
	private fun getListFiles(parentDirectory: File): ArrayList<File> {
		val inFiles = ArrayList<File>()
		val files = parentDirectory.listFiles()
		if (files != null) {
			for (file in files) {
				if (file != null && !isWhiteListed(file)) { // hopefully to fix crashes on a very limited number of devices && won't touch if whitelisted
					if (file.isDirectory) { // folder
						if (autoWhite) { // if auto whitelist enabled
							if (!autoWhiteList(file)) inFiles.add(file) // if file is not in autowhitelist index, add it
						} else inFiles.add(file) // add folder itself
						inFiles.addAll(getListFiles(file)) // add contents to returned list
					} else inFiles.add(file) // add file
				}
			}
		}
		return inFiles
	}
	private fun getInstalledPackages(): ArrayList<String> {
		val pm = context.packageManager
		val pkgs = pm.getInstalledApplications(PackageManager.GET_META_DATA)
		val pkgsStr: ArrayList<String> = ArrayList()
		for (pkg in pkgs) {
			pkgsStr.add(pkg.packageName)
		}
		return pkgsStr
	}

	/**
	 * Runs a for each loop through the black/white list, and compares the path of the file to each path in
	 * the list
	 * @param file file to check if in the whitelist
	 * @return true if is the file is in the black/white list, false if not
	 */
	private fun isWhiteListed(file: File): Boolean {
		val absolutePath = file.absolutePath
		val name = file.name
		for (path in whitelist){
			if (path.equals(absolutePath) ||
					path.equals(name, ignoreCase = true)) return true
		}
		return false
	}
	private fun isBlackListed(file: File): Boolean {
		val absolutePath = file.absolutePath
		for (pattern in blacklist){
			if (absolutePath.matches(pattern)) return true
		}
		return false
	}

	/**
	 * Runs before anything is filtered/cleaned. Automatically adds folders to the whitelist based on
	 * the name of the folder itself
	 * @param file file to check whether it should be added to the whitelist
	 */
	private fun autoWhiteList(file: File): Boolean {
		for (protectedFile in autoWhitelist){
			val whiteLists = whitelist
			if (
				file.name.lowercase().contains(protectedFile) &&
				!whiteLists.contains(file.absolutePath.lowercase())
			) {
				whiteLists
					.toMutableList()
					.add(file.absolutePath.lowercase())
				prefs
					.edit()
					.putStringSet("whitelist", HashSet(whiteLists))
					.apply()
				return true
			}
		}
		return false
	}

	/**
	 * Runs as for each loop through the filter, and checks if the file matches any filters
	 * @param file file to check
	 * @return true if the file matches certain rules, otherwise false
	 */
	fun filter(file: File): Boolean {
		if (
			// corpse checking
			// Android/Data/[file != .nomedia]
			corpse &&
			file.parentFile?.name == "data" &&
			file.parentFile?.parentFile?.name == "Android" &&
			file.name != ".nomedia" &&
			!installedPackages.contains(file.name) ||
			// empty file
			emptyFile &&
			isFileEmpty(file) ||
			// empty folder
			emptyDir &&
			isDirectoryEmpty(file) ||
			// blacklist (targeted to get deleted)
			isBlackListed(file)
		) return true

		// file
		val absolutePath = file.absolutePath.lowercase()
		for (filter in filters){
			if (absolutePath.matches(filter.lowercase().toRegex())) return true
		}
		return false
	}

	/**
	 * lists the contents of the file to an array, if the array length is 0, then return true, else
	 * false
	 * @param directory directory to test
	 * @return true if empty, false if containing a file(s)
	 */
	private fun isDirectoryEmpty(directory: File): Boolean {
		// Not folder
		if (!directory.isDirectory) return false
		// Empty folder // Folders with another folder and empty file
		val list = directory.list()
		// access denied folder (eg. Android/data)
		if (list == null) return false
		return list!!.isNullOrEmpty() // || list!!.all { child ->
//		// Another folder
//		if (child.isDirectory) isDirectoryEmpty(child)
//		// Empty file
//		else isFileEmpty(child)
//	}
	}
	private fun isFileEmpty(file: File): Boolean {
		return file.isFile && file.length() == 0L
	}

	/**
	 * Adds paths to the white list that are not to be cleaned. As well as adds extensions to filter.
	 * 'generic', and 'apk' should be assigned by calling preferences.getBoolean()
	 */
	fun setFilters(generic: Boolean, apk: Boolean){
		filters.clear()
		// Filters
		if (generic){
			for (folder in Constants.filter_genericFolders) filters.add(getRegexForFolder(folder))
			for (file in Constants.filter_genericFiles) filters.add(getRegexForFile(file))
		}
		// Android APKs and various split APK extensions
		if (apk){
			for (apk in Constants.filter_apkFiles) filters.add(getRegexForFile(apk))
		}

		// cached whitelist/blacklist values
		whitelist = WhitelistFragment.whiteListOn.mapNotNull { it }
		blacklist = BlacklistFragment.blackListOn.mapNotNull { it?.toRegex() }
		// Auto whitelist
		if (autoWhite){
			autoWhitelist.clear()
			autoWhitelist.addAll(Constants.filter_autoWhite)
		}
	}

	fun start(): Long {
		isRunning = true
		var cycles: Byte = 0
		var maxCycles: Byte = if (delete) prefs.getInt("multi_run",1).toByte() else 1

		// removes the need to 'clean' multiple times to get everything
		while (cycles < maxCycles) {

			// cycle indicator
			addText?.invoke(context,"Running Cycle " + cycles + "/" + maxCycles,0);

			// find/scan files
			if (foundFiles == null) foundFiles = getListFiles(path)
			guiScanProgressMax = guiScanProgressMax + foundFiles!!.size

			// filter & delete
			for (file in foundFiles!!){
				if (filter(file)){ // filter
					var tv: TextView? = addText?.invoke(context,file.absolutePath,1)
					kilobytesTotal += file.length()
					if (delete){
						++filesRemoved
						// deletion
						val isDeleted = if (file.isDirectory) file.deleteRecursively() else file.delete()
						// failed to remove file and the textView is visible (not null)
						if (!isDeleted){
							// error effect - red looks too concerning
							tv!!.setTextColor(Color.GRAY)
						}
					}
				}
				guiScanProgressProgress = guiScanProgressProgress + 1
				updateProgress!!.invoke(context,guiScanProgressProgress * 100.0 / guiScanProgressMax);
			}
			foundFiles = null
			if (filesRemoved == 0) break
			filesRemoved = 0
			++cycles
		}
		// cycle indicator
		addText?.invoke(context,"Finished!",1);
		isRunning = false
		return kilobytesTotal
	}

	private fun getRegexForFolder(folder: String): String {
		return ".*(\\\\|/)$folder(\\\\|/|$).*"
	}

	private fun getRegexForFile(file: String): String {
		return ".+" + file.replace(".", "\\.") + "$"
	}

	companion object {
		// TODO remove local prefs objects, create setter for one instead
		var isRunning = false
		private val filters = ArrayList<String>()
		private var blacklist: List<Regex> = emptyList()
		private var whitelist: List<String> = emptyList()
		private val autoWhitelist: MutableList<String> = ArrayList<String>()
	}

	init {
		BlacklistFragment.getBlackList(prefs)
		BlacklistFragment.getBlacklistOn(prefs)
		WhitelistFragment.getWhiteList(prefs)
		WhitelistFragment.getWhitelistOn(prefs)
	}
}
