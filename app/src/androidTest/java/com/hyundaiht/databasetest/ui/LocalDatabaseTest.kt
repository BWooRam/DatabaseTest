package com.hyundaiht.databasetest.ui

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hyundaiht.databasetest.createRandomEntity
import com.hyundaiht.databasetest.createRandomUser
import kotlinx.coroutines.cancel
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
            userDao.deleteAllList()
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

    @Test
    fun pagingAllListTest() {
        val userDao = db.userDao()
        runBlocking {
            userDao.deleteAllList()
            val limit = 20
            val maxIndex = 5
            val maxLimit = limit * maxIndex

            for (index in 0 until maxLimit) {
                val random = createRandomUser()
                userDao.insert(random)
            }

            val pageAll = userDao.allList()
            println("LocalDatabaseTest pagingTest pageAll size = ${pageAll.size}")

            for (index in 0 until maxIndex) {
                val page = userDao.allList(limit, index * limit)
                println("LocalDatabaseTest pagingTest page size = ${page.size}, list = $page")
                val comparePage = pageAll.subList(index * limit, (index + 1) * limit)
                println("LocalDatabaseTest pagingTest comparePage size = ${comparePage.size}, list = $comparePage")
                assertEquals(page, comparePage)
            }
        }
    }

    @Test
    fun pagingSourceLoad() {
        val userDao = db.userDao()
        runBlocking {
            userDao.deleteAllList()
            for (index in 0..100) {
                userDao.insert(createRandomUser())
            }

            val pagingSource = userDao.pagingSource().load(PagingSource.LoadParams.Append(0, 20, false))
            when(pagingSource){
                is PagingSource.LoadResult.Page -> {
                    println("LocalDatabaseTest pagingSourceLoad data = ${pagingSource.data}")
                }
                is PagingSource.LoadResult.Error -> {
                    println("LocalDatabaseTest pagingSourceLoad data = ${pagingSource.throwable}")
                }
                is PagingSource.LoadResult.Invalid -> {
                    println("LocalDatabaseTest pagingSourceLoad data = ${pagingSource.toString()}")
                }
            }
        }
    }

    @Test
    fun getItemCount() {
        val userDao = db.userDao()
        runBlocking {
            userDao.deleteAllList()
            for (index in 0 until 100) {
                userDao.insert(createRandomUser())
            }

            val totalItemCount = userDao.getItemCount()
            assertEquals(100, totalItemCount)
        }
    }

}