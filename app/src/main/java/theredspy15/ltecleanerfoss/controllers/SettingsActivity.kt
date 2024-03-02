/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss.controllers
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
//import org.acra.ACRA
import org.json.JSONArray
import org.json.JSONObject
import theredspy15.ltecleanerfoss.App
import theredspy15.ltecleanerfoss.CommonFunctions.writeContentToUri
import theredspy15.ltecleanerfoss.ScheduledWorker.Companion.enqueueWork
import theredspy15.ltecleanerfoss.R
class SettingsActivity: AppCompatActivity(){
	override fun onCreate(savedInstanceState:Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		loadFragment()
	}
	fun loadFragment(){
		val preferenceFragment = MyPreferenceFragment()
		preferenceFragment.setImportFileLauncher(registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
			if (uri != null){
				val inputStream = contentResolver.openInputStream(uri)
				val jsonBytes = inputStream?.readBytes()
				inputStream?.close()
				val jsonString = jsonBytes!!.toString(Charsets.UTF_8)
				val jsonObject = JSONObject(jsonString)
				val prefsEditor = App.prefs?.edit()
				for (key in jsonObject.keys()){
					val value = jsonObject.get(key)
					when (value){
						is String -> prefsEditor?.putString(key, value)
						is Int -> prefsEditor?.putInt(key, value)
						is Boolean -> prefsEditor?.putBoolean(key, value)
						is JSONArray -> {
							val stringArray = mutableListOf<String>()
							for (i in 0 until value.length()) stringArray.add(value.optString(i))
							prefsEditor?.putStringSet(key, stringArray.toSet())
						}
						else -> {
							// Handle unsupported data type or provide a fallback
							Toast.makeText(this, "Unsupported data type: $key: $value", Toast.LENGTH_SHORT).show()
						}
					}
				}
				prefsEditor?.apply()
				Toast.makeText(this, "Settings imported!", Toast.LENGTH_SHORT).show()
				loadFragment()
			}
		})
// ANTI UNLUCK 69 //
		preferenceFragment.setExportFileLauncher(registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
			if (uri != null){
				val jsonData: String = JSONObject(App.prefs?.all).toString() // TODO - warning - Type mismatch: inferred type is (Mutable)Map<String!, *>? but (MutableMap<Any?, Any?>..Map<*, *>) was expected
				writeContentToUri(this, uri, jsonData);
				Toast.makeText(this, "Settings exported!", Toast.LENGTH_SHORT).show()
			}
		})
		supportFragmentManager.beginTransaction()
			.replace(R.id.layout, preferenceFragment)
			.commit()
	}
	class MyPreferenceFragment: PreferenceFragmentCompat() {
		private lateinit var importFileLauncher: ActivityResultLauncher<Array<String>>
		private lateinit var exportFileLauncher: ActivityResultLauncher<String>

		override fun onCreate(savedInstanceState: Bundle?) {
			super.onCreate(savedInstanceState)
			setHasOptionsMenu(true)
			findPreference<Preference>("blacklist")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener { _ ->
					startActivity(Intent(requireContext(), BlacklistActivity::class.java))
					false
				}
			findPreference<Preference>("whitelist")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener { _ ->
					startActivity(Intent(requireContext(), WhitelistActivity::class.java))
					false
				}
			findPreference<Preference>("cleanevery")!!.onPreferenceChangeListener =
				Preference.OnPreferenceChangeListener { _:Preference, _:Any? ->
					enqueueWork(requireContext().applicationContext)
					true
				}
			findPreference<Preference>("theme")!!.onPreferenceChangeListener =
				Preference.OnPreferenceChangeListener { _:Preference, value:Any? ->
					AppCompatDelegate.setDefaultNightMode(when (value){
						resources.getStringArray(R.array.themes)[1] -> AppCompatDelegate.MODE_NIGHT_NO
						resources.getStringArray(R.array.themes)[2] -> AppCompatDelegate.MODE_NIGHT_YES
						else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
					})
					true
				}
			findPreference<Preference>("dataImport")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener { _ ->
					importFileLauncher.launch(arrayOf("application/json"))
					false
				}
			findPreference<Preference>("dataExport")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener { _ ->
					exportFileLauncher.launch("LTECleaner_settings.json")
					false
				}
			findPreference<Preference>("__doomedBruhhh")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener { _ ->
					throw IllegalAccessException("Get doomed haha >:)")
				}
		}

		/* TODO: is there a better way to do this? */
		public fun setImportFileLauncher(activityResultLauncher: ActivityResultLauncher<Array<String>>) {
			importFileLauncher = activityResultLauncher
		}
		public fun setExportFileLauncher(activityResultLauncher: ActivityResultLauncher<String>) {
			exportFileLauncher = activityResultLauncher
		}

		/**
		 * Inflate Preferences
		 */
		override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
			setPreferencesFromResource(R.xml.preferences,rootKey)
		}
	}
}