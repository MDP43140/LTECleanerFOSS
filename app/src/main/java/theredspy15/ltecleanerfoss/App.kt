/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
//import theredspy15.ltecleanerfoss.CommonFunctions
import io.mdp43140.ael.ErrorLogger
import theredspy15.ltecleanerfoss.ui.MainActivity
class App: Application(){
	private var runCount = 0
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
		CommonFunctions.updateTheme(this,prefs)
		if (prefs!!.getBoolean("dynamicColor",true)){
			DynamicColors.applyToActivitiesIfAvailable(this)
			DynamicColors.wrapContextIfAvailable(this)
		}
	}
	companion object {
		var prefs:SharedPreferences? = null
	}
}