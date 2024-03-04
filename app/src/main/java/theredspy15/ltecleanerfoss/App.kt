/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import theredspy15.ltecleanerfoss.controllers.MainActivity
import theredspy15.ltecleanerfoss.R
import theredspy15.ltecleanerfoss.CommonFunctions
class App: Application(){
	var runCount = 0
	override fun onCreate(){
		super.onCreate()
		// Catches bugs and crashes, and makes it easy to report the bug
		Thread.setDefaultUncaughtExceptionHandler { _, paramThrowable ->
			CommonFunctions.handleError(this, 3, paramThrowable)
		}
		prefs = PreferenceManager.getDefaultSharedPreferences(this)
		// Stores how many times the app has been opened,
		// Can also be used for first run related codes in the future, who knows...
		runCount = prefs!!.getInt("runCount",0)
		prefs!!.edit().putInt("runCount",runCount + 1).commit()
		// Update theme and apply dynamic color
		CommonFunctions.updateTheme(prefs)
		if (prefs!!.getBoolean("dynamicColor",true)) DynamicColors.applyToActivitiesIfAvailable(this)
	}
	companion object {
		@JvmField var prefs:SharedPreferences? = null
	}
}