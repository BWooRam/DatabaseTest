package com.hyundaiht.databasetest.paging

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hyundaiht.databasetest.ui.MyDatabase
import com.hyundaiht.databasetest.ui.UserDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.jvm.Throws


@RunWith(AndroidJUnit4::class)
class UserPagingSourceTest {
    private lateinit var appContext: Context
    private lateinit var db: MyDatabase
    private lateinit var userDao: UserDao
    private lateinit var userPagingSource: UserPagingSource

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context = appContext, MyDatabase::class.java).build()
        userDao = db.userDao()
        userPagingSource = UserPagingSource(db.userDao())
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun getPrevKeyAndNextKey() {
        runBlocking {
            val page = 5
            val pageSize = 20
            val toIndex = page * pageSize

            //기본 동작 테스트
            val key1 = userPagingSource.getPrevKeyAndNextKey(
                currentPage = page,
                totalItemSize = 101,
                toIndex = toIndex
            )
            Assert.assertEquals(key1.first, 4)
            Assert.assertEquals(key1.second, 6)

            //마지막 페이지 일때 테스트
            val key2 = userPagingSource.getPrevKeyAndNextKey(
                currentPage = page,
                totalItemSize = 100,
                toIndex = toIndex
            )
            Assert.assertEquals(key2.first, 4)
            Assert.assertEquals(key2.second, null)

            //아무 것도 없을때 테스트
            val key3 = userPagingSource.getPrevKeyAndNextKey(
                currentPage = 1,
                totalItemSize = 0,
                toIndex = 0
            )
            Assert.assertEquals(key3.first, null)
            Assert.assertEquals(key3.second, null)
        }
    }

    @Test
    fun getIndexFromAndTo() {
        runBlocking {
            val maxPage = 5
            val maxPageSize = 20
            val totalItemSize = maxPage * maxPageSize

            //첫 Index 계산 테스트
            val index1 = userPagingSource.getIndexFromAndTo(
                1,
                maxPageSize,
                totalItemSize
            )
            Assert.assertEquals(index1.first, 0)
            Assert.assertEquals(index1.second, 20)

            //마지막 Index 계산 테스트
            val index2 = userPagingSource.getIndexFromAndTo(
                maxPageSize,
                maxPageSize,
                totalItemSize
            )
            Assert.assertEquals(index2.first, 80)
            Assert.assertEquals(index2.second, 100)
        }
    }

}