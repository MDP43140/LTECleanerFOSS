/*
 * SPDX-FileCopyrightText: 2020-2023 Hunter J Drum
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package theredspy15.ltecleanerfoss.ui
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import io.mdp43140.ael.ErrorLogger
import io.mdp43140.ltecleanerfoss.util.putData // SharedPreferencesExtension.kt
import org.json.JSONArray
import org.json.JSONObject
import theredspy15.ltecleanerfoss.App
import theredspy15.ltecleanerfoss.CleanupService
import theredspy15.ltecleanerfoss.CommonFunctions
import theredspy15.ltecleanerfoss.ScheduledWorker.Companion.enqueueWork
import theredspy15.ltecleanerfoss.databinding.ActivitySettingsBinding
import theredspy15.ltecleanerfoss.R
class SettingsActivity: AppCompatActivity(){
	private lateinit var binding: ActivitySettingsBinding
	private val importFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
		if (uri != null){
			val jsonObject = JSONObject(
				contentResolver.openInputStream(uri)
					?.use { it.readBytes() }
					!!.toString(Charsets.UTF_8)
			)
			val prefsEditor = App.prefs?.edit()
			val buffer = StringBuilder();
			for (key in jsonObject.keys()){
				val value = jsonObject.get(key)
				if (prefsEditor?.putData(key,value) == false)
					buffer.append("\n- $key: $value")
			}
			prefsEditor?.apply()
			val text = buffer.toString()
			Snackbar.make(
				binding.root,
				if (text == "") "Settings imported!" else "Unsupported data type:${text}",
				Snackbar.LENGTH_SHORT
			).let {
				it.setAction(getString(android.R.string.ok)){ _: View ->
					it.dismiss()
				}
				it.show()
			}
			loadFragment()
		}
	}
	private val exportFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
		if (uri != null){
			// TODO - warning - Type mismatch: inferred type is (Mutable)Map<String!, *>? but (MutableMap<Any?, Any?>..Map<*, *>) was expected
			val jsonData: String = JSONObject(App.prefs?.all).toString()
			CommonFunctions.writeContentToUri(this, uri, jsonData);
			Snackbar.make(
				binding.root,
				"Settings exported!",
				Snackbar.LENGTH_SHORT
			).let {
				it.setAction(getString(android.R.string.ok)){ _: View ->
					it.dismiss()
				}
				it.show()
			}
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySettingsBinding.inflate(layoutInflater)
		setContentView(binding.root)
		loadFragment()
	}
	override fun onBackPressed(){
		// suggested fix by LeakCanary
		super.onBackPressed()
		finishAfterTransition()
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
					val themeStr = resources.getStringArray(R.array.themes_key)
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