/*
 * SPDX-FileCopyrightText: 2020-2023 Hunter J Drum
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package theredspy15.ltecleanerfoss
import android.content.Context
import android.content.res.Resources
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
@RunWith(AndroidJUnit4::class)
class InstrumentedTest {
	private lateinit var fs: FileScanner
	@Before
	fun init() {
		val appContext = ApplicationProvider.getApplicationContext<Context>()
		val path = File(Environment.getExternalStorageDirectory().toString())
		val res = appContext.resources
		fs = FileScanner(path, appContext)
		fs.autoWhite = false
		fs.delete = true
	}
	@Test
	fun useAppContext() {
		val appContext = ApplicationProvider.getApplicationContext<Context>()
		assertEquals("theredspy15.ltecleanerfoss", appContext.packageName)
	}
	@Test
	@Throws(IOException::class)
	fun checkLogFiles() {
		val logFile = createFile("testfile.loG")
		val clogFile = createFile("clogs.pnG")
		logFile.writeText("[i] Starting service...")
		clogFile.writeText("PNG IHDR IDAT IEND")
		fs.setUpFilters(true, false)
		fs.start()
		assertTrue(clogFile.exists())
		assertFalse(logFile.exists())
	}
	@Test
	@Throws(IOException::class)
	fun checkTempFiles() {
		val tmpFile = createFile("testfile.tMp")
		tmpFile.writeText("test")
		fs.setUpFilters(true, false)
		fs.start()
		assertFalse(tmpFile.exists())
	}
	@Test
	@Throws(IOException::class)
	fun checkThumbFiles() {
		val thumbFile = createFile("thumbs.Db")
		thumbFile.writeText("DB")
		fs.setUpFilters(false, false)
		fs.start()
		assertFalse(thumbFile.exists())
	}
	@Test
	@Throws(IOException::class)
	fun checkAPKFiles() {
		val thumbFile = createFile("chrome.aPk")
		thumbFile.writeText("ZIP AndroidManifest.xml")
		fs.setUpFilters(true, true)
		fs.start()
		assertFalse(thumbFile.exists())
	}
	@Test
	@Throws(IOException::class)
	fun checkEmptyFile() {
		val testFile = createDir("testFile")
		val emptyFile = createDir("testFile")
		testFile.writeText("not empty file")
		fs.setUpFilters(true, false)
		fs.emptyDir = true
		fs.start()
		assertFalse(emptyFile.exists())
	}
	@Test
	@Throws(IOException::class)
	fun checkEmptyFolder() {
		val emptyDir = createDir("testFolder")
		fs.setUpFilters(true, false)
		fs.emptyDir = true
		fs.start()
		assertFalse(emptyDir.exists())
	}
	private fun createFile(name: String): File {
		val file = File(Environment.getExternalStorageDirectory(), name)
		file.createNewFile()
		assertTrue(file.exists())
		return file
	}
	private fun createDir(name: String): File {
		val file = File(Environment.getExternalStorageDirectory(), name)
		file.mkdir()
		assertTrue(file.exists())
		return file
	}
}