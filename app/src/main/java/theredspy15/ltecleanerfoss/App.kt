/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.os.Build
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
//import theredspy15.ltecleanerfoss.R
//import theredspy15.ltecleanerfoss.CommonFunctions
import io.mdp43140.ael.ErrorLogger
import theredspy15.ltecleanerfoss.ui.MainActivity
class App: Application(){
	var runCount = 0
	override fun attachBaseContext(base: Context){
		super.attachBaseContext(base);
		// Catches bugs and crashes, and makes it easy to report the bug
		if (ErrorLogger.instance == null){
			ErrorLogger(base)
			ErrorLogger.instance?.isNotification = true // sends a notification
			ErrorLogger.reportUrl = "https://github.com/mdp43140/LTECleanerFOSS/issues/new"
		}
	}
	override fun onCreate(){
		super.onCreate()
		prefs = PreferenceManager.getDefaultSharedPreferences(this)
		// Stores how many times the app has been opened,
		// Can also be used for first run related codes in the future, who knows...
		runCount = prefs!!.getInt("runCount",0)
		prefs!!.edit().putInt("runCount",runCount + 1).apply()
		// Update theme and apply dynamic color
		CommonFunctions.updateTheme(prefs)
		if (prefs!!.getBoolean("dynamicColor",true)) DynamicColors.applyToActivitiesIfAvailable(this)
		// Quick shortcut
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1){
			val shortcutMgr = (getSystemService(ShortcutManager::class.java))
			if (shortcutMgr!!.isRequestPinShortcutSupported){
				val icon = Icon.createWithResource(this, R.drawable.ic_baseline_cleanup_24)
				val int1 = Intent(this, MainActivity::class.java).apply {
					action = "cleanup"
					putExtra("action","cleanup")
				}
				val int2 = Intent(this, MainActivity::class.java).apply {
					action = "stopBgApps"
					putExtra("action","stopBgApps")
				}
				val sct1 = ShortcutInfo.Builder(this, "sct1")
					.setShortLabel("Cleanup")
					.setIcon(icon)
					.setIntent(int1)
					.build()
				val sct2 = ShortcutInfo.Builder(this, "sct2")
					.setShortLabel("Stop background apps")
					.setIcon(icon)
					.setIntent(int2)
					.build()
				shortcutMgr.dynamicShortcuts = listOf(sct1,sct2)
			}
		}
	}
	companion object {
		var prefs:SharedPreferences? = null
	}
}