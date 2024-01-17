/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss.controllers
import android.content.DialogInterface
import android.os.Bundle
import android.net.Uri
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
import theredspy15.ltecleanerfoss.ScheduledWorker.Companion.enqueueWork
import theredspy15.ltecleanerfoss.R
import org.json.JSONArray
import org.json.JSONObject
class SettingsActivity: AppCompatActivity(){
	override fun onCreate(savedInstanceState:Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		val prefs = PreferenceManager.getDefaultSharedPreferences(this)
		val preferenceFragment = MyPreferenceFragment()
		preferenceFragment.setImportFileLauncher(registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
			try {
				if (uri != null){
					val inputStream = contentResolver.openInputStream(uri)
					val jsonBytes = inputStream?.readBytes()
					inputStream?.close()
					if (jsonBytes != null && prefs != null) {
						val jsonString = jsonBytes.toString(Charsets.UTF_8)
						val jsonObject = JSONObject(jsonString)
						val prefsEditor = prefs.edit()

						for (key in jsonObject.keys()){
							val value = jsonObject.get(key)
							when (value) {
								is String -> prefsEditor.putString(key, value)
								is Int -> prefsEditor.putInt(key, value)
								is Boolean -> prefsEditor.putBoolean(key, value)
								is JSONArray -> {
									val stringArray = mutableListOf<String>()
									for (i in 0 until value.length()) stringArray.add(value.optString(i))
									prefsEditor.putStringSet(key, stringArray.toSet())
								}
								else -> {
									// Handle unsupported data type or provide a fallback
									Toast.makeText(this,
										String.format("Unsupported data type: %s = %s",key,value),
										Toast.LENGTH_SHORT
									).show()
								}
							}
						}

						prefsEditor.apply()
						Toast.makeText(this, "Settings imported successfully!", Toast.LENGTH_SHORT).show()

						supportFragmentManager.beginTransaction()
							.replace(R.id.layout, MyPreferenceFragment())
							.commit()
					}
				}
			} catch (e: Exception){Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()}
		})
		preferenceFragment.setExportFileLauncher(registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
			val jsonData: String = JSONObject(prefs?.all).toString() // TODO - warning - Type mismatch: inferred type is (Mutable)Map<String!, *>? but (MutableMap<Any?, Any?>..Map<*, *>) was expected
			if (uri != null){
				writeContentToUri(uri, jsonData);
				Toast.makeText(this, "Settings imported successfully!", Toast.LENGTH_SHORT).show()
			}
		})
		supportFragmentManager.beginTransaction()
			.replace(R.id.layout, preferenceFragment)
			.commit()
	}

	private fun writeContentToUri(uri: Uri, content: String) {
		this.contentResolver.openOutputStream(uri)?.use { outputStream ->
			outputStream.write(content.toByteArray())
		}
	}

	class MyPreferenceFragment: PreferenceFragmentCompat() {
		private lateinit var importFileLauncher: ActivityResultLauncher<Array<String>>
		private lateinit var exportFileLauncher: ActivityResultLauncher<String>

		override fun onCreate(savedInstanceState: Bundle?) {
			super.onCreate(savedInstanceState)
			setHasOptionsMenu(true)
			findPreference<Preference>("aggressive")!!.onPreferenceChangeListener =
				Preference.OnPreferenceChangeListener { _:Preference, value:Any? ->
					val isChecked = (value as Boolean)
					if (isChecked){
						val filtersFiles = resources.getStringArray(R.array.aggressive_filter_folders)
						val alertDialog = AlertDialog.Builder(requireContext()).create()
						alertDialog.setTitle(getString(R.string.aggressive_filter_what_title))
						alertDialog.setMessage(
							getString(R.string.adds_the_following) + " " + filtersFiles.contentToString()
						)
						alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK"){ dialog:DialogInterface, _:Int -> dialog.dismiss() }
						alertDialog.show()
					}
					true
				}
			findPreference<Preference>("cleanevery")!!.onPreferenceChangeListener =
				Preference.OnPreferenceChangeListener { _:Preference, _:Any? ->
					enqueueWork(requireContext().applicationContext)
					true
				}
			findPreference<Preference>("theme")!!.onPreferenceChangeListener =
				Preference.OnPreferenceChangeListener { _:Preference, value:Any? ->
					val light = resources.getStringArray(R.array.themes)[1]
					val dark = resources.getStringArray(R.array.themes)[2]
					if (value == dark) { // dark
						AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
					} else if (value == light) { // light
						AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
					} else { // auto
						AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
					}
					true
				}
			findPreference<Preference>("dataImport")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener { _ ->
					importFileLauncher.launch(arrayOf("application/json"))
					true
				}
			findPreference<Preference>("dataExport")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener { _ ->
					exportFileLauncher.launch("LTECleaner_settings.json")
					true
				}
			findPreference<Preference>("crash")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener { _ ->
					val result = 10 / 0
					result == 0
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