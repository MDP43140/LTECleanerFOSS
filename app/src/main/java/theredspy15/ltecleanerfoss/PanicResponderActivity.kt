/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
import android.app.Activity
import android.content.Intent
import android.os.Bundle
class PanicResponderActivity: Activity(){
	override fun onCreate(savedInstanceState:Bundle?){
		super.onCreate(savedInstanceState)
		applicationContext.startForegroundService(
			Intent(applicationContext,CleanupService::class.java)
		)
		finishAndRemoveTask()
	}
}