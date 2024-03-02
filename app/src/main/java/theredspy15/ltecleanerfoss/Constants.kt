/*
 * (C) 2020-2023 Hunter J Drum
 * (C) 2024 MDP43140
 */
package theredspy15.ltecleanerfoss
object Constants {
	const val BGCLEAN_WORK_NAME = "scheduled_cleanup_work"
	const val BGCLEAN_WORK_TAG = "cleanup_work_tag"
	const val NOTIFICATION_ID_SERVICE = 1
	const val NOTIFICATION_CHANNEL_SERVICE = "CLEANUP_SERVICE"
	val blacklistDefault: Set<String> = setOf(
		".*\\.log",
		".*\\.tmp",
		".*/log",
		".*/Logs",
		"/storage/emulated/0/.*albumthumbs\\?",
		"/storage/emulated/0/Android/data/.*/files/tombstone_.*",
		"/storage/emulated/0/Android/data/.*/files/il2cpp",
		"/storage/emulated/0/Android/data/.*/files/.*UnityAdsVideoCache",
		"/storage/emulated/0/Android/data/.*/files/.*mobvista",
		"/storage/emulated/0/Android/data/.*/files/Unity/.*/Analytics",
		"/storage/emulated/0/Android/data/.*/files/supersonicads",
		"/storage/emulated/0/Android/data/.*/files/.*splashad",
		"/storage/emulated/0/.*Analytics",
		"/storage/emulated/0/.*Cache",
		"/storage/emulated/0/.*cache",
		"/storage/emulated/0/.*\\.exo",
		"/storage/emulated/0/.*\\.thumb[0-9]",
		"/storage/emulated/0/.*\\.thumbnails\\?",
		"/storage/emulated/0/.*thumbs?\\.db",
		"/storage/emulated/0/.*/\\.spotlight-V100",
		"/storage/emulated/0/.*/\\.DS_Store",
		"/storage/emulated/0/.*/\\.Trash",
		"/storage/emulated/0/.*/bugreports",
		"/storage/emulated/0/.*/Bugreport",
		"/storage/emulated/0/.*/desktop.ini",
		"/storage/emulated/0/.*/fseventd",
		"/storage/emulated/0/.*/leakcanary",
		"/storage/emulated/0/.*/LOST\\.DIR"
	)
	val blacklistOnDefault: Set<String> = setOf(
		".*\\.log",
		".*\\.tmp",
		".*/log",
		".*/Logs",
		"/storage/emulated/0/.*Analytics",
		"/storage/emulated/0/.*Cache",
		"/storage/emulated/0/.*cache"
	)
	val whitelistDefault: Set<String> = setOf(
		".*/backup",
		".*/copy",
		".*/copies",
		".*/important",
		".*/do_not_edit"
	)
	val whitelistOnDefault: Set<String> = setOf(
		".*/backup",
		".*/copy",
		".*/copies",
		".*/important",
		".*/do_not_edit"
	)
}