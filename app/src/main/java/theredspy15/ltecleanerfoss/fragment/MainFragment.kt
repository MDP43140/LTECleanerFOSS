/*
 * SPDX-FileCopyrightText: 2020-2023 Hunter J Drum
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package theredspy15.ltecleanerfoss.fragment
import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.provider.Settings
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import theredspy15.ltecleanerfoss.App
import theredspy15.ltecleanerfoss.CommonFunctions.convertSize
import theredspy15.ltecleanerfoss.CommonFunctions.makeStatusNotification
import theredspy15.ltecleanerfoss.CommonFunctions.sendNotification
import theredspy15.ltecleanerfoss.Constants
import theredspy15.ltecleanerfoss.FileScanner
import theredspy15.ltecleanerfoss.MainActivity
import theredspy15.ltecleanerfoss.databinding.FragmentMainBinding
import theredspy15.ltecleanerfoss.R
import java.io.File
import java.util.Locale

class MainFragment: BaseFragment(){
	private lateinit var binding: FragmentMainBinding
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentMainBinding.inflate(inflater, container, false)
		binding.apply {
			analyzeBtn.isEnabled = !FileScanner.isRunning
			cleanBtn.isEnabled = !FileScanner.isRunning
			analyzeBtn.setOnClickListener { analyze() }
			cleanBtn.setOnClickListener { clean() }
			settingsBtn.setOnClickListener { (requireActivity() as MainActivity).startFragment(SettingsFragment()) }
			whitelistBtn.setOnClickListener { (requireActivity() as MainActivity).startFragment(WhitelistFragment()) }
		}
		return binding.root
	}

	fun analyze(){
		if (!FileScanner.isRunning){
			requestWriteExternalPermission()
			scan(false)
		}
	}

	/**
	 * Runs search and delete on background thread
	 */
	fun clean() {
		if (!FileScanner.isRunning) {
			requestWriteExternalPermission()
			if (App.prefs == null) println("prefs is null!")
			if (App.prefs!!.getBoolean("one_click",false)){
				scan(true) // one-click enabled
			} else { // one-click disabled
				(requireActivity() as MainActivity).dialogBuilder.setTitle(getString(R.string.are_you_sure_deletion_title))
					.setMessage(getString(R.string.are_you_sure_deletion))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.clean)){ dialogInterface: DialogInterface, _: Int ->
						dialogInterface.dismiss()
						scan(true)
					}
					.setNegativeButton(getString(android.R.string.cancel)){ dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
					.show()
			}
		}
	}

	private fun clearClipboard() {
		try {
			val mCbm = requireContext().getSystemService(ClipboardManager::class.java)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				mCbm.clearPrimaryClip()
			} else {
				mCbm.setPrimaryClip(ClipData.newPlainText("", ""))
			}
		} catch (e: NullPointerException) {
			requireActivity().runOnUiThread {
				Toast.makeText(
					requireActivity(),
					R.string.clear_clipboard_failed,
					Toast.LENGTH_SHORT
				).show()
			}
		}
	}

	/**
	 * Searches entire device, adds all files to a list, then a for each loop filters
	 * out files for deletion. Repeats the process as long as it keeps finding files to clean,
	 * unless nothing is found to begin with
	 */
	@SuppressLint("SetTextI18n")
	private fun scan(deleteCache: Boolean){
		Thread {
			Looper.prepare()
			requireActivity().runOnUiThread {
				binding.apply {
					analyzeBtn.isEnabled = !FileScanner.isRunning
					cleanBtn.isEnabled = !FileScanner.isRunning
					statusTextView.text = getString(R.string.status_running)
					fileListView.removeAllViews()
				}
			}
			if (App.prefs!!.getBoolean("clipboard", false)) clearClipboard()
			if (App.prefs!!.getBoolean("closebgapps", false)) stopBgApps()
			val path = Environment.getExternalStorageDirectory()

			// scanner setup
			val fs = FileScanner(path,requireActivity())
			fs.apply {
				setFilters(
					App.prefs!!.getBoolean("generic",true),
					App.prefs!!.getBoolean("apk",false)
				)
				delete = deleteCache
				updateProgress = ::updatePercentage
				addText = ::addText
			}

			// failed scan
			if (path.listFiles() == null){ // is this needed? yes.
				addText(getString(R.string.failed_scan),Color.RED)
			}

			// run the scan and put KBs found/freed text
			val kilobytesTotal = fs.start()
			requireActivity().runOnUiThread {
				binding.apply {
					statusTextView.text =
						getString(if (deleteCache) R.string.freed else R.string.found) +
						" " + convertSize(kilobytesTotal)
					cleanBtn.isEnabled = !FileScanner.isRunning
					analyzeBtn.isEnabled = !FileScanner.isRunning
					fileScrollView.post { fileScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
				}
			}
			Looper.loop()
		}.start()
	}

	fun stopBgApps(){
		Thread {
			val am = requireContext().getSystemService(ActivityManager::class.java)
			val pkgName = requireContext().getPackageName()
			val memInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()

			am.getMemoryInfo(memInfo)
			val memAvailBefore = Math.round(memInfo.availMem / 1048576f).toInt()
			for (pkg in requireContext().getPackageManager().getInstalledApplications(8704)){
				if (pkg.processName != pkgName){
					am.killBackgroundProcesses(pkg.processName);
				}
			}
			am.getMemoryInfo(memInfo)
			val memAvailAfter = Math.round(memInfo.availMem / 1048576f).toInt()

			var memFreed = memAvailAfter - memAvailBefore
			if (memFreed < 0){memFreed = 0}
			addText(String.format(
				"Available RAM: %dMB > %dMB / %dMB (%dMB freed)",
				memAvailBefore,
				memAvailAfter,
				Math.round(memInfo.totalMem / 1048576f).toInt(),
				memFreed
			))
		}.start()
	}

	/**
	 * Update GUI Percentage
	 */
	private fun updatePercentage(context: Context, percent: Double){
		(context as MainActivity?)!!.runOnUiThread {
			binding.statusTextView.text = String.format(
				"%s %.0f%%",
				context.getString(R.string.status_running),
				percent
			) // dont remove .0 part or crash
		}
	}

	/**
	 * Convenient method to
	 * quickly add a text
	 * @param text - text of textView
	 * @return - created textView
	 */
	fun addText(text: String, color: Int): TextView {
		val textView = TextView(requireActivity())
		textView.setTextColor(color)
		textView.text = text
		textView.setPadding(3,3,3,3)
		// adding to scroll view
		requireActivity().runOnUiThread { binding.fileListView.addView(textView) }
		// scroll to bottom
		binding.fileScrollView.post { binding.fileScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
		return textView
	}
	fun addText(text: String, type: String): TextView {
		return addText(text, when (type){
			"delete" -> resources.getColor(R.color.colorAccent,resources.newTheme())
			else -> Color.YELLOW
		})
	}
	fun addText(text: String): TextView {
		return addText(text,Color.YELLOW)
	}
	fun addText(ctx: Context, text: String, type: Int): TextView {
		// used for FileScanner
		return addText(text, when (type){
			1 -> resources.getColor(R.color.colorAccent,resources.newTheme()) // deleting file/folder
			2 -> Color.GRAY // delete error, red looks too concerning
			else -> Color.YELLOW // everything else colored yellow
		})
	}

	/**
	 * Request write permission
	 */
	private fun requestWriteExternalPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){ // Android 11+
			requireActivity().requestPermissions(arrayOf(
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.MANAGE_EXTERNAL_STORAGE // Android 11+ requires manage external storage due to storageAccessFramework
			),1)
			if (!Environment.isExternalStorageManager()) { // all files
				Toast.makeText(requireActivity(), R.string.permission_needed, Toast.LENGTH_LONG).show()
				startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
					data = Uri.fromParts("package",requireContext().packageName,null)
				})
			}
		} else {
			requireActivity().requestPermissions(arrayOf(
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			),1)
		}
	}
}