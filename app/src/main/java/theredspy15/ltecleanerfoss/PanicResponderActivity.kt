/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.app.Activity
import android.os.Bundle
import android.os.Environment
import androidx.preference.PreferenceManager
class PanicResponderActivity: Activity(){
	override fun onCreate(savedInstanceState:Bundle?) {
		// TODO: when triggered, the ui lags and
		// empty black screen until its done
		// make it run in background
		super.onCreate(savedInstanceState)
		Thread {
			try {
				val path = Environment.getExternalStorageDirectory()
				val prefs = PreferenceManager.getDefaultSharedPreferences(
					applicationContext
				)
				// scanner setup
				val fs = FileScanner(path, applicationContext)
					.setEmptyFile(prefs.getBoolean("emptyFile", false))
					.setEmptyDir(prefs.getBoolean("emptyFolder", false))
					.setAutoWhite(prefs.getBoolean("auto_white", true))
					.setDelete(true)
					.setCorpse(prefs.getBoolean("corpse", false))
					.setUpFilters(
						prefs.getBoolean("generic", true),
						prefs.getBoolean("apk", false)
					)
			} catch (e: Exception) {}
			finishAndRemoveTask()
		}.start()
	}
}