/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss.ui
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.json.JSONArray
import org.json.JSONObject
import theredspy15.ltecleanerfoss.App
import theredspy15.ltecleanerfoss.CleanupService
import theredspy15.ltecleanerfoss.CommonFunctions
import theredspy15.ltecleanerfoss.ScheduledWorker.Companion.enqueueWork
import theredspy15.ltecleanerfoss.R
class SettingsActivity: AppCompatActivity(){
	private val importFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
		if (uri != null){
			val inputStream = contentResolver.openInputStream(uri)
			val jsonBytes = inputStream?.readBytes()
			inputStream?.close()
			val jsonObject = JSONObject(jsonBytes!!.toString(Charsets.UTF_8))
			val prefsEditor = App.prefs?.edit()
			for (key in jsonObject.keys()){
				val value = jsonObject.get(key)
				when (value){
					is Boolean -> prefsEditor?.putBoolean(key, value)
					is Int -> prefsEditor?.putInt(key, value)
					is String -> prefsEditor?.putString(key, value)
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
	}
	private val exportFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
		if (uri != null){
			val jsonData: String = JSONObject(App.prefs?.all).toString() // TODO - warning - Type mismatch: inferred type is (Mutable)Map<String!, *>? but (MutableMap<Any?, Any?>..Map<*, *>) was expected
			CommonFunctions.writeContentToUri(this, uri, jsonData);
			Toast.makeText(this, "Settings exported!", Toast.LENGTH_SHORT).show()
		}
	}
	override fun onCreate(savedInstanceState:Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		loadFragment()
	}
	fun loadFragment(){
		val preferenceFragment = MyPreferenceFragment()
		/* TODO: is there a better way to do this? */
		preferenceFragment.importFileLauncher = importFileLauncher
		preferenceFragment.exportFileLauncher = exportFileLauncher
		supportFragmentManager.beginTransaction()
			.replace(R.id.layout, preferenceFragment)
			.commit()
	}
	class MyPreferenceFragment: PreferenceFragmentCompat() {
		lateinit var importFileLauncher: ActivityResultLauncher<Array<String>>
		lateinit var exportFileLauncher: ActivityResultLauncher<String>
		override fun onCreate(savedInstanceState: Bundle?) {
			super.onCreate(savedInstanceState)
			findPreference<Preference>("blacklist")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener {
					startActivity(Intent(requireContext(), BlacklistActivity::class.java))
					false
				}
			findPreference<Preference>("whitelist")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener {
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
					val themeStr = resources.getStringArray(R.array.themes)
					AppCompatDelegate.setDefaultNightMode(when (value){
						themeStr[1] -> AppCompatDelegate.MODE_NIGHT_NO
						themeStr[2] -> AppCompatDelegate.MODE_NIGHT_YES
						else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
					})
					true
				}
			findPreference<Preference>("dataImport")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener {
					importFileLauncher.launch(arrayOf("application/json"))
					false
				}
			findPreference<Preference>("dataExport")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener {
					exportFileLauncher.launch("LTECleaner_settings.json")
					false
				}
			findPreference<Preference>("__doomedBruhhh")!!.onPreferenceClickListener =
				Preference.OnPreferenceClickListener {
					throw IllegalAccessException("Get doomed haha >:)")
				}
		}
		override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
			setPreferencesFromResource(R.xml.preferences,rootKey)
		}
	}
}