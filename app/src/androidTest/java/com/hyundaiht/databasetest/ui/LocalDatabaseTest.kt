package com.hyundaiht.databasetest.ui

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hyundaiht.databasetest.createRandomEntity
import com.hyundaiht.databasetest.createRandomUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.jvm.Throws


@RunWith(AndroidJUnit4::class)
class LocalDatabaseTest {
    private lateinit var appContext: Context
    private lateinit var db: MyDatabase

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context = appContext, MyDatabase::class.java).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeUserAndReadUser() {
        val userDao = db.userDao()
        runBlocking {
            val randomUser1 = createRandomUser()
            val randomUser2 = createRandomUser()
            userDao.insert(randomUser1)
            userDao.insert(randomUser2)
            val list = userDao.allList()
            println("LocalDatabaseTest writeUserAndReadUser list = $list")
            assertEquals(randomUser1, list[1])
            assertEquals(randomUser2, list[0])
        }
    }

    @Test
    fun writeExampleAndReadExample() {
        val exampleDao = db.exampleDao()
        runBlocking {
            val randomExample1 = createRandomEntity()
            val randomExample2 = createRandomEntity()
            exampleDao.insert(randomExample1)
            exampleDao.insert(randomExample2)
            val list = exampleDao.allList()
            println("LocalDatabaseTest writeExampleAndReadExample list = $list")
            assertEquals(randomExample1, list[1])
            assertEquals(randomExample2, list[0])
        }
    }
}