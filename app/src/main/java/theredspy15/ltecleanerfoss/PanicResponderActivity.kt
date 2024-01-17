/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.preference.PreferenceManager
class PanicResponderActivity: Activity(){
	override fun onCreate(savedInstanceState:Bundle?) {
		super.onCreate(savedInstanceState)
		val path = Environment.getExternalStorageDirectory()
		val prefs = PreferenceManager.getDefaultSharedPreferences(
			applicationContext
		)
		FileScanner(path, applicationContext)
			.setEmptyDir(prefs.getBoolean("empty",false))
			.setAutoWhite(prefs.getBoolean("auto_white",true))
			.setDelete(true)
			.setCorpse(prefs.getBoolean("corpse", false))
			.setGUI(null)
			.setContext(applicationContext)
			.setUpFilters(
				prefs.getBoolean("generic", true),
				prefs.getBoolean("aggressive", false),
				prefs.getBoolean("apk", false)
			)
			.startScan()
		finishAndRemoveTask()
	}
}