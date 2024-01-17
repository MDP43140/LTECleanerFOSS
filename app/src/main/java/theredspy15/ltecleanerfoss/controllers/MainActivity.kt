/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss.controllers
import com.google.android.material.color.DynamicColors
import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import android.content.DialogInterface
import theredspy15.ltecleanerfoss.App
import theredspy15.ltecleanerfoss.FileScanner
import theredspy15.ltecleanerfoss.R
import theredspy15.ltecleanerfoss.databinding.ActivityMainBinding
import java.io.File
import java.text.DecimalFormat
class MainActivity: AppCompatActivity(){
	private lateinit var binding: ActivityMainBinding
	private lateinit var mDialogBuilder: AlertDialog.Builder
	override fun onCreate(savedInstanceState:Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		binding.analyzeBtn.setOnClickListener { analyze() }
		binding.cleanBtn.setOnClickListener { clean() }
		binding.settingsBtn.setOnClickListener { settings() }
		binding.whitelistBtn.setOnClickListener { whitelist() }
		WhitelistActivity.getWhiteList(prefs)
		mDialogBuilder = AlertDialog.Builder(this)
		if (prefs!!.getBoolean("dynamicColor",true)) DynamicColors.applyToActivityIfAvailable(this)
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
			Thread { scan(false) }.start()
		}
	}

	/**
	 * Runs search and delete on background thread
	 */
	private fun clean() {
		if (!FileScanner.isRunning) {
			requestWriteExternalPermission()
			if (prefs == null) println("prefs is null!")
			if (prefs!!.getBoolean("one_click",false)){
				Thread { scan(true) }.start() // one-click enabled
			} else { // one-click disabled
				val mDialog: AlertDialog = mDialogBuilder.create()
				mDialog.setTitle(getString(R.string.are_you_sure_deletion_title))
				mDialog.setMessage(getString(R.string.are_you_sure_deletion))
				mDialog.setCancelable(false)
				mDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.clean)){ dialogInterface: DialogInterface, _: Int ->
					dialogInterface.dismiss()
					Thread { scan(true) }.start()
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
	private fun scan(delete: Boolean) {
		Looper.prepare()
		runOnUiThread {
			binding.cleanBtn.isEnabled = !FileScanner.isRunning
			binding.analyzeBtn.isEnabled = !FileScanner.isRunning
			binding.statusTextView.text = getString(R.string.status_running)
		}
		reset()
		if (prefs!!.getBoolean("clipboard", false)) clearClipboard()
		if (prefs!!.getBoolean("closebgapps", false)) {
			val am = this.getSystemService("activity") as ActivityManager
			for (pkg in getPackageManager().getInstalledApplications(8704)) {
				am.killBackgroundProcesses(pkg.processName)
			}
		}
		val path = Environment.getExternalStorageDirectory()

		// scanner setup
		val fs = FileScanner(path,this)
			.setEmptyDir(prefs!!.getBoolean("empty", false))
			.setAutoWhite(prefs!!.getBoolean("auto_white", true))
			.setDelete(delete)
			.setCorpse(prefs!!.getBoolean("corpse", false))
			.setGUI(binding)
			.setContext(this)
			.setUpFilters(
				prefs!!.getBoolean("generic",true),
				prefs!!.getBoolean("aggressive",false),
				prefs!!.getBoolean("apk",false)
			)

		// failed scan
		if (path.listFiles() == null){ // is this needed? yes.
			runOnUiThread { binding.fileListView.addView(printTextView(getString(R.string.failed_scan),Color.RED)) }
		}

		// run the scan and put KBs found/freed text
		val kilobytesTotal = fs.startScan()
		runOnUiThread {
			binding.statusTextView.text =
				getString(if (delete) R.string.freed else R.string.found) +
				" " + convertSize(kilobytesTotal)
			binding.cleanBtn.isEnabled = !FileScanner.isRunning
			binding.analyzeBtn.isEnabled = !FileScanner.isRunning
		}
		binding.fileScrollView.post { binding.fileScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
		Looper.loop()
	}

	/**
	 * Convenience method to quickly create a textview
	 * @param text - text of textview
	 * @return - created textview
	 */
	private fun printTextView(text: String, color: Int): TextView {
		val textView = TextView(this@MainActivity)
		textView.setTextColor(color)
		textView.text = text
		textView.setPadding(3,3,3,3)
		return textView
	}

	/**
	 * Displaying text for files that have been removed
	 */
	fun displayDeletion(file: File): TextView {
		// creating and adding a text view to the scroll view with path to file
		val textView = printTextView(file.absolutePath, resources.getColor(R.color.colorAccent,resources.newTheme()))

		// adding to scroll view
		runOnUiThread { binding.fileListView.addView(textView) }

		// scroll to bottom
		binding.fileScrollView.post { binding.fileScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
		return textView
	}

	/**
	 * Displays generic text
	 */
	fun displayText(text: String) {
		// creating and adding a text view to the scroll view with path to file
		val textView = printTextView(text, Color.YELLOW)

		// adding to scroll view
		runOnUiThread { binding.fileListView.addView(textView) }

		// scroll to bottom
		binding.fileScrollView.post { binding.fileScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
	}

	/**
	 * Removes all views present in fileListView (linear view), and sets found and removed
	 * files to 0
	 */
	private fun reset() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this)
		runOnUiThread {
			binding.fileListView.removeAllViews()
		}
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
	 * Handles the whether the user grants permission. Shows an alert dialog asking the user to give storage permission.
	 */
	override fun onRequestPermissionsResult(
		requestCode:Int,
		permissions:Array<String>,
		grantResults:IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED){
			val mDialog: AlertDialog = mDialogBuilder.create()
			mDialog.setTitle("Grant a permission")
			mDialog.setMessage(getString(R.string.prompt_string))
			mDialog.setButton(AlertDialog.BUTTON_POSITIVE,getString(R.string.settings_string)){ dialogInterface: DialogInterface, _: Int ->
				val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
				intent.data = Uri.fromParts("package",packageName,null)
				dialogInterface.dismiss()
				startActivity(intent)
			}
			mDialog.show()
		}
	}


	companion object {
		@JvmField var prefs:SharedPreferences? = App.prefs
		@JvmStatic fun convertSize(length: Long): String {
			val format = DecimalFormat("#.##")
			val kib:Long = 1024
			val mib:Long = 1048576
			return if (length > mib) {
				format.format(length / mib) + " MB"
			} else if (length > kib) {
				format.format(length / kib) + " KB"
			} else {
				format.format(length) + " B"
			}
		}
	}
}