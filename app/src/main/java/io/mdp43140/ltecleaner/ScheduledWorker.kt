/*
 * SPDX-FileCopyrightText: 2020-2023 Hunter J Drum
 * SPDX-FileCopyrightText: 2024-2025 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.ltecleaner
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
//import io.mdp43140.ltecleaner.App
//import io.mdp43140.ltecleaner.Constants
class ScheduledWorker(appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {
	override fun doWork(): Result {
		try {
			applicationContext.startForegroundService(
				Intent(applicationContext,CleanupService::class.java)
			)
			return Result.success()
		} catch (e: Exception) {
			return Result.failure()
		}
	}
	companion object {
		fun enqueueWork(ctx: Context) {
			val dailyCleanupInterval: Long = App.prefs!!.cleanEvery.toLong()
			val constraints = Constraints.Builder()
				.setRequiresBatteryNotLow(true)
				.setRequiresDeviceIdle(true)
				.build()
			WorkManager.getInstance(ctx).cancelAllWorkByTag(Constants.BGCLEAN_WORK_NAME)
			if (dailyCleanupInterval > 0){
				val myPeriodicWork = PeriodicWorkRequestBuilder<ScheduledWorker>(
						dailyCleanupInterval, TimeUnit.HOURS, // Interval
						15, TimeUnit.MINUTES // Flex interval for battery optimization
					)
					.addTag(Constants.BGCLEAN_WORK_TAG)
					.setConstraints(constraints)
					.build()
				WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
					Constants.BGCLEAN_WORK_NAME,
					ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
					myPeriodicWork
				)
			}
		}
	}
}
