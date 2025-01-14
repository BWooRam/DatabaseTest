package com.hyundaiht.databasetest.paging

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PagingFactorTest {
    private lateinit var appContext: Context
    private val factor = PagingFactor()

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun getPrevKeyAndNextKey_DB_데이터_있을때() {
        runBlocking {
            val page = 5
            val pageSize = 20
            val toIndex = page * pageSize

            //기본 동작 테스트
            val key1 = factor.getPrevKeyAndNextKey(
                currentPage = page,
                totalItemSize = 101,
                toIndex = toIndex
            )
            Assert.assertEquals(key1.first, 4)
            Assert.assertEquals(key1.second, 6)

            //마지막 페이지 일때 테스트
            val key2 = factor.getPrevKeyAndNextKey(
                currentPage = page,
                totalItemSize = 100,
                toIndex = toIndex
            )
            Assert.assertEquals(key2.first, 4)
            Assert.assertEquals(key2.second, null)

            //아무 것도 없을때 테스트
            val key3 = factor.getPrevKeyAndNextKey(
                currentPage = 1,
                totalItemSize = 0,
                toIndex = toIndex
            )
            Assert.assertEquals(key3.first, null)
            Assert.assertEquals(key3.second, null)
        }
    }

    @Test
    fun getPrevKeyAndNextKey_DB_데이터_없을때() {
        runBlocking {
            val maxPage = 5
            val pageSize = 20

            //아무 것도 없을때 테스트
            val key1 = factor.getPrevKeyAndNextKey(
                currentPage = 1,
                totalItemSize = 0,
                toIndex = pageSize
            )
            Assert.assertEquals(key1.first, null)
            Assert.assertEquals(key1.second, null)

            //마지막 페이지 일때 테스트
            val key2 = factor.getPrevKeyAndNextKey(
                currentPage = maxPage,
                totalItemSize = 100,
                toIndex = 100
            )
            Assert.assertEquals(key2.first, 4)
            Assert.assertEquals(key2.second, null)
        }
    }

    @Test
    fun getIndexFromAndTo() {
        runBlocking {
            val maxPage = 5
            val maxPageSize = 20
            val totalItemSize = maxPage * maxPageSize

            //첫 Index 계산 테스트
            val index1 = factor.getIndexFromAndTo(
                1,
                maxPageSize,
                totalItemSize
            )
            Assert.assertEquals(index1.first, 0)
            Assert.assertEquals(index1.second, 20)

            //마지막 Index 계산 테스트
            val index2 = factor.getIndexFromAndTo(
                maxPage,
                maxPageSize,
                totalItemSize
            )
            Assert.assertEquals(index2.first, 80)
            Assert.assertEquals(index2.second, 100)
        }
    }

}