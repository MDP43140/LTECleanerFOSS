/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.widget.TextView
import androidx.preference.PreferenceManager
import theredspy15.ltecleanerfoss.ui.MainActivity
import theredspy15.ltecleanerfoss.ui.BlacklistActivity
import theredspy15.ltecleanerfoss.ui.WhitelistActivity
import java.io.File
import java.util.Locale
class FileScanner(private val path: File, context: Context){
	// TODO: Ability to clean SD Card? Already tried SAF implementation, but its really hard, and soon i realized it has storage access restrictions: https://developer.android.com/training/data-storage/shared/documents-files#document-tree-access-restrictions
	private var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
	private val context = context
	private var res: Resources = context.resources
	private var filesRemoved = 0
	private var kilobytesTotal: Long = 0
	var delete = false
	var emptyFile = prefs.getBoolean("emptyFile", false)
	var emptyDir = prefs.getBoolean("emptyFolder", false)
	var autoWhite = prefs.getBoolean("auto_white", true)
	var corpse = prefs.getBoolean("corpse", false)
	var updateProgress: ((context: Context, percent: Double) -> Unit)? = null
	private val listFiles: List<File>
		get() = getListFiles(path)
	private var guiScanProgressMax = 0
	private var guiScanProgressProgress = 0

	/**
	 * Used to generate a list of all files on device
	 * @param parentDirectory where to start searching from
	 * @return List of all files on device (besides whitelisted ones)
	 */
	private fun getListFiles(parentDirectory: File): List<File> {
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

	/**
	 * Runs a for each loop through the black/white list, and compares the path of the file to each path in
	 * the list
	 * @param file file to check if in the whitelist
	 * @return true if is the file is in the black/white list, false if not
	 */
	private fun isWhiteListed(file: File): Boolean {
		for (path in WhitelistActivity.getWhitelistOn(prefs)) when {
			path.equals(file.absolutePath) ||
			path.equals(file.name, ignoreCase = true) -> return true
		}
		return false
	}
	private fun isBlackListed(file: File): Boolean {
		for (path in BlacklistActivity.getBlacklistOn(prefs)){
			val pattern = path!!.toRegex()
			if (file.absolutePath.matches(pattern) ||
					file.name.matches(pattern)) return true
		}
		return false
	}

	/**
	 * Runs before anything is filtered/cleaned. Automatically adds folders to the whitelist based on
	 * the name of the folder itself
	 * @param file file to check whether it should be added to the whitelist
	 */
	private fun autoWhiteList(file: File): Boolean {
		whitelist.forEach { protectedFile ->
			val whiteLists = WhitelistActivity.getWhiteList(prefs)
			if (
				file.name.lowercase(Locale.getDefault()).contains(protectedFile) &&
				!whiteLists.contains(file.absolutePath.lowercase(Locale.getDefault()))
			) {
				whiteLists
					.toMutableList()
					.add(file.absolutePath.lowercase(Locale.getDefault()))
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
		try {
			if (
				// corpse checking
				// Android/Data/[file != .nomedia]
				corpse &&
				file.parentFile!!.name == "data" &&
				file.parentFile!!.parentFile!!.name == "Android" &&
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
			val filterIterator = filters.iterator()
			while (filterIterator.hasNext()) {
				val filter = filterIterator.next()
				if (file.absolutePath.lowercase(Locale.getDefault()).matches(filter.lowercase(Locale.getDefault()).toRegex()))
					return true
			}
		} catch (e: NullPointerException) {
			return false
		}
		return false // not empty folder or file in filter
	}

	private val installedPackages: List<String>
		get() {
			val pm = context.packageManager
			val pkgs = pm.getInstalledApplications(PackageManager.GET_META_DATA)
			val pkgsStr: MutableList<String> = ArrayList()
			for (pkg in pkgs) {
				pkgsStr.add(pkg.packageName)
			}
			return pkgsStr
		}

	/**
	 * lists the contents of the file to an array, if the array length is 0, then return true, else
	 * false
	 * @param directory directory to test
	 * @return true if empty, false if containing a file(s)
	 */
	private fun isDirectoryEmpty(directory: File): Boolean {
		return directory.isDirectory && directory.list()!!.isEmpty()
	}
	private fun isFileEmpty(file: File): Boolean {
		return !file.isDirectory && file.length() == 0L
	}

	/**
	 * Adds paths to the white list that are not to be cleaned. As well as adds extensions to filter.
	 * 'generic', and 'apk' should be assigned by calling preferences.getBoolean()
	 */
	fun setFilters(generic: Boolean, apk: Boolean){
		filters.clear()
		// filters
		if (generic){
			for (folder in Constants.filter_genericFolders) filters.add(getRegexForFolder(folder))
			for (file in Constants.filter_genericFiles) filters.add(getRegexForFile(file))
		}
		// apk
		if (apk) filters.add(getRegexForFile(".apk"))
		// whitelist
		if (autoWhite){
			whitelist.clear()
			whitelist.addAll(Constants.filter_autoWhite)
		}
	}

	fun start(): Long {
		isRunning = true
		var cycles: Byte = 0
		var maxCycles: Byte = if (delete) prefs.getInt("multirun",1).toByte() else 1
		var foundFiles: List<File>

		// removes the need to 'clean' multiple times to get everything
		while (cycles < maxCycles) {

			// cycle indicator
			if (context is MainActivity) context.addText(
				"Running Cycle " + (cycles + 1) + "/" + maxCycles
			)

			// find/scan files
			foundFiles = listFiles // fetching this variable (List) triggers get function getListFiles(path)
			guiScanProgressMax = guiScanProgressMax + foundFiles.size

			// filter & delete
			for (file in foundFiles){
				if (filter(file)){ // filter
					var tv: TextView? = null
					if (context is MainActivity) tv = context.addText(file.absolutePath,"delete")
					kilobytesTotal += file.length()
					if (delete){
						++filesRemoved
						// deletion
						// failed to remove file and the textView is visible (not null)
						if (!file.delete() && context is MainActivity){
							context.runOnUiThread {
								// error effect - red looks too concerning
								tv!!.setTextColor(Color.GRAY)
							}
						}
					}
				}
				guiScanProgressProgress = guiScanProgressProgress + 1
				updateProgress!!.invoke(context,guiScanProgressProgress * 100.0 / guiScanProgressMax);
			}
			if (filesRemoved == 0) break
			filesRemoved = 0
			++cycles
		}
		// cycle indicator
		if (context is MainActivity) context.addText("Finished!")
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
		@JvmField
		var isRunning = false
		private val filters = ArrayList<String>()
		private val whitelist: MutableList<String> = ArrayList<String>()
	}

	init {
		BlacklistActivity.getBlackList(prefs)
		WhitelistActivity.getWhiteList(prefs)
	}
}
