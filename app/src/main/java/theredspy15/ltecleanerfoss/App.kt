/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import theredspy15.ltecleanerfoss.R
class App: Application(){
	override fun onCreate(){
		super.onCreate()
		prefs = PreferenceManager.getDefaultSharedPreferences(this)
		updateTheme()
	}
	protected fun updateTheme(){
		val auto = resources.getStringArray(R.array.themes)[0]
		val light = resources.getStringArray(R.array.themes)[1]
		val dark = resources.getStringArray(R.array.themes)[2]
		val theme = prefs!!.getString("theme",auto)
		AppCompatDelegate.setDefaultNightMode(when (theme) {
			light -> AppCompatDelegate.MODE_NIGHT_NO
			dark -> AppCompatDelegate.MODE_NIGHT_YES
			else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
		})
	}
	companion object {
		@JvmField var prefs:SharedPreferences? = null
	}
}