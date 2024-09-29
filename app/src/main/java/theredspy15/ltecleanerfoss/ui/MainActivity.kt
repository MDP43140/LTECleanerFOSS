/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss.ui
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
import android.provider.Settings
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import theredspy15.ltecleanerfoss.App
import theredspy15.ltecleanerfoss.FileScanner
import theredspy15.ltecleanerfoss.CommonFunctions.convertSize
import theredspy15.ltecleanerfoss.CommonFunctions.makeStatusNotification
import theredspy15.ltecleanerfoss.CommonFunctions.sendNotification
import theredspy15.ltecleanerfoss.Constants
import theredspy15.ltecleanerfoss.databinding.ActivityMainBinding
import theredspy15.ltecleanerfoss.R
import java.io.File
import java.util.Locale
class MainActivity: AppCompatActivity(){
	private lateinit var binding: ActivityMainBinding
	private lateinit var dialogBuilder: MaterialAlertDialogBuilder
	override fun onCreate(savedInstanceState:Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		binding.apply {
			analyzeBtn.isEnabled = !FileScanner.isRunning
			cleanBtn.isEnabled = !FileScanner.isRunning
			analyzeBtn.setOnClickListener { analyze() }
			cleanBtn.setOnClickListener { clean() }
			settingsBtn.setOnClickListener { settings() }
			whitelistBtn.setOnClickListener { whitelist() }
		}
		setContentView(binding.root)
		WhitelistActivity.getWhiteList(App.prefs)
		dialogBuilder = MaterialAlertDialogBuilder(this)

		// Handle intent action (from shortcut stuff)
		val intentAction = intent.getStringExtra("action")
		when (intentAction){
			"cleanup" -> scan(true)
			"stopBgApps" -> stopBgApps()
			else -> if (intentAction != null) Toast.makeText(this, "Invalid intent action: $intentAction", Toast.LENGTH_SHORT).show()
		}
	}
	override fun onBackPressed(){
		// suggested fix by LeakCanary
		super.onBackPressed()
		finishAfterTransition()
	}
	private fun settings(){
		startActivity(Intent(this,SettingsActivity::class.java))
	}
	private fun whitelist(){
		startActivity(Intent(this,WhitelistActivity::class.java))
	}

	private fun analyze(){
		if (!FileScanner.isRunning){
			requestWriteExternalPermission()
			scan(false)
		}
	}

	/**
	 * Runs search and delete on background thread
	 */
	private fun clean() {
		if (!FileScanner.isRunning) {
			requestWriteExternalPermission()
			if (App.prefs == null) println("prefs is null!")
			if (App.prefs!!.getBoolean("one_click",false)){
				scan(true) // one-click enabled
			} else { // one-click disabled
				dialogBuilder.setTitle(getString(R.string.are_you_sure_deletion_title))
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
			val mCbm = getSystemService(ClipboardManager::class.java)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				mCbm.clearPrimaryClip()
			} else {
				mCbm.setPrimaryClip(ClipData.newPlainText("", ""))
			}
		} catch (e: NullPointerException) {
			runOnUiThread {
				Toast.makeText(
					this,
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
			runOnUiThread {
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
			val fs = FileScanner(path,this)
			fs.apply {
				setFilters(
					App.prefs!!.getBoolean("generic",true),
					App.prefs!!.getBoolean("apk",false)
				)
				delete = deleteCache
				updateProgress = ::updatePercentage
			}

			// failed scan
			if (path.listFiles() == null){ // is this needed? yes.
				addText(getString(R.string.failed_scan),Color.RED)
			}

			// run the scan and put KBs found/freed text
			val kilobytesTotal = fs.start()
			runOnUiThread {
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

	private fun stopBgApps(){
		Thread {
			val am = getSystemService(ActivityManager::class.java)
			val pkgName = getPackageName()
			val memInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()

			am.getMemoryInfo(memInfo)
			val memAvailBefore = Math.round(memInfo.availMem / 1048576f).toInt()
			for (pkg in getPackageManager().getInstalledApplications(8704)){
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
		val textView = TextView(this@MainActivity)
		textView.setTextColor(color)
		textView.text = text
		textView.setPadding(3,3,3,3)
		// adding to scroll view
		runOnUiThread { binding.fileListView.addView(textView) }
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

	/**
	 * Request write permission
	 */
	private fun requestWriteExternalPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){ // Android 11+
			ActivityCompat.requestPermissions(this,arrayOf(
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.MANAGE_EXTERNAL_STORAGE // Android 11+ requires manage external storage due to storageAccessFramework
			),1)
			if (!Environment.isExternalStorageManager()) { // all files
				Toast.makeText(this, R.string.permission_needed, Toast.LENGTH_LONG).show()
				startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
					data = Uri.fromParts("package",packageName,null)
				})
			}
		} else {
			ActivityCompat.requestPermissions(this,arrayOf(
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			),1)
		}
	}

	/**
	 * Handles whether the user grants permission.
	 * Shows an alert dialog asking
	 * user to give storage permission.
	 */
	override fun onRequestPermissionsResult(
		requestCode:Int,
		permissions:Array<String>,
		grantResults:IntArray
	){
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
			permissions.contains("android.permission.READ_EXTERNAL_STORAGE") &&
			permissions.contains("android.permission.WRITE_EXTERNAL_STORAGE") &&
			permissions.contains("android.permission.MANAGE_EXTERNAL_STORAGE")){
			// Since Android 13, external storage access wasnt longer a thing anymore
			Toast.makeText(this,"Sadly, Android 13+ no longer have access to external storage",Toast.LENGTH_SHORT).show();
		}
		else if (
			requestCode == 1 &&
			grantResults.isNotEmpty() &&
			grantResults[0] != PackageManager.PERMISSION_GRANTED)
			dialogBuilder.setTitle(getString(R.string.permission_needed))
				.setMessage(getString(R.string.grantPermissions_sum) + permissions.map { "\n- " + it.replaceFirst("android.permission.","") }.joinToString(""))
				.setPositiveButton(getString(R.string.settings_string)){ dialogInterface: DialogInterface, _: Int ->
					startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
						data = Uri.fromParts("package",packageName,null)
					})
					dialogInterface.dismiss()
				}
				.show()
	}
}