/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss.controllers
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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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
	private lateinit var dialogBuilder: AlertDialog.Builder
	override fun onCreate(savedInstanceState:Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		binding.analyzeBtn.isEnabled = !FileScanner.isRunning
		binding.cleanBtn.isEnabled = !FileScanner.isRunning
		binding.analyzeBtn.setOnClickListener { analyze() }
		binding.cleanBtn.setOnClickListener { clean() }
		binding.settingsBtn.setOnClickListener { settings() }
		binding.whitelistBtn.setOnClickListener { whitelist() }
		WhitelistActivity.getWhiteList(App.prefs)
		dialogBuilder = AlertDialog.Builder(this)

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
				val mDialog: AlertDialog = dialogBuilder.create()
				mDialog.setTitle(getString(R.string.are_you_sure_deletion_title))
				mDialog.setMessage(getString(R.string.are_you_sure_deletion))
				mDialog.setCancelable(false)
				mDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.clean)){ dialogInterface: DialogInterface, _: Int ->
					dialogInterface.dismiss()
					scan(true)
				}
				mDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)){ dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
				mDialog.show()
			}
		}
	}

	private fun clearClipboard() {
		try {
			val mCbm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				mCbm.clearPrimaryClip()
			} else {
				mCbm.setPrimaryClip(ClipData.newPlainText("", ""))
			}
		} catch (e: NullPointerException) {
			runOnUiThread {
				Toast.makeText(
					this,
					R.string.clipboard_clear_failed,
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
	private fun scan(delete: Boolean){
		Thread {
			Looper.prepare()
			runOnUiThread {
				binding.analyzeBtn.isEnabled = !FileScanner.isRunning
				binding.cleanBtn.isEnabled = !FileScanner.isRunning
				binding.statusTextView.text = getString(R.string.status_running)
				binding.fileListView.removeAllViews()
			}
			if (App.prefs!!.getBoolean("clipboard", false)) clearClipboard()
			if (App.prefs!!.getBoolean("closebgapps", false)) stopBgApps()
			val path = Environment.getExternalStorageDirectory()

			// scanner setup
			val fs = FileScanner(path,this)
			fs.setFilters(
				App.prefs!!.getBoolean("generic",true),
				App.prefs!!.getBoolean("apk",false)
			)
			fs.delete = delete
			fs.updateProgress = ::updatePercentage

			// failed scan
			if (path.listFiles() == null){ // is this needed? yes.
				addText(getString(R.string.failed_scan),Color.RED)
			}

			// run the scan and put KBs found/freed text
			val kilobytesTotal = fs.start()
			runOnUiThread {
				binding.statusTextView.text =
					getString(if (delete) R.string.freed else R.string.found) +
					" " + convertSize(kilobytesTotal)
				binding.cleanBtn.isEnabled = !FileScanner.isRunning
				binding.analyzeBtn.isEnabled = !FileScanner.isRunning
			}
			binding.fileScrollView.post { binding.fileScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
			Looper.loop()
		}.start()
	}

	private fun stopBgApps(){
		val am = this.getSystemService("activity") as ActivityManager
		for (pkg in getPackageManager().getInstalledApplications(8704)) {
			am.killBackgroundProcesses(pkg.processName)
		}
	}

	/**
	 * Update GUI Percentage
	 */
	private fun updatePercentage(context: Context, percent: Double){
		(context as MainActivity?)!!.runOnUiThread {
			binding.statusTextView.text = String.format(
				Locale.US, "%s %.0f%%",
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
	fun addText(text: String, type: String): TextView{
		return addText(text, when (type){
			"delete" -> resources.getColor(R.color.colorAccent,resources.newTheme())
			else -> Color.YELLOW
		})
	}
	fun addText(text: String): TextView{
		return addText(text,Color.YELLOW)
	}

	/**
	 * Request write permission
	 */
	private fun requestWriteExternalPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){ // Android 11+
			ActivityCompat.requestPermissions(this,arrayOf(
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.MANAGE_EXTERNAL_STORAGE // Android 11+ requires manage external storage due to storageAccessFramework
			),1)
			if (!Environment.isExternalStorageManager()) { // all files
				Toast.makeText(this, R.string.permission_needed, Toast.LENGTH_LONG).show()
				val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
				intent.data = Uri.fromParts("package",packageName,null)
				startActivity(intent)
			}
		} else {
			ActivityCompat.requestPermissions(this,arrayOf(
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.READ_EXTERNAL_STORAGE
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
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (
			requestCode == 1 &&
			grantResults.isNotEmpty() &&
			grantResults[0] != PackageManager.PERMISSION_GRANTED){
			val dialog: AlertDialog = dialogBuilder.create()
			dialog.setTitle("Grant a permission")
			dialog.setMessage(getString(R.string.prompt_string))
			dialog.setButton(AlertDialog.BUTTON_POSITIVE,getString(R.string.settings_string)){ dialogInterface: DialogInterface, _: Int ->
				val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
				intent.data = Uri.fromParts("package",packageName,null)
				dialogInterface.dismiss()
				startActivity(intent)
			}
			dialog.show()
		}
	}
}