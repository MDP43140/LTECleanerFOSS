package io.mdp43140.ltecleanerfoss.util
import android.content.SharedPreferences
import org.json.JSONArray
fun SharedPreferences.Editor.putData(key: String,value: Any): Boolean {
	when (value){
		is Boolean -> this.putBoolean(key,value)
		is Float -> this.putFloat(key,value)
		is Long -> this.putLong(key,value)
		is Int -> this.putInt(key,value)
		is String -> this.putString(key,value)
		is JSONArray -> this.putStringSet(key,
			(0 until value.length())
				.map { value.optString(it) }
				.toSet()
		)
		is Collection<*> -> this.putStringSet(key,
			value.filterIsInstance<String>().toSet()
		)
		else -> {
			return false
		}
	}
	return true
}