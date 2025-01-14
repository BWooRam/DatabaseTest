package com.hyundaiht.databasetest.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.hyundaiht.databasetest.createRandomUser
import com.hyundaiht.databasetest.ui.MyDatabase
import com.hyundaiht.databasetest.ui.UserDao
import com.hyundaiht.databasetest.ui.UserEntity
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * ApiService
 *
 */
interface ApiService {
    suspend fun getUsers(page: Int, pageSize: Int): List<UserEntity>
}

/**
 * FakeApiService
 *
 */
class FakeApiService : ApiService {
    private var count = AtomicInteger(0)
    override suspend fun getUsers(page: Int, pageSize: Int): List<UserEntity> {
        Log.d("UserRemoteMediator", "getUsers page = $page, pageSize = $pageSize")
        val temp = mutableListOf<UserEntity>()
        for (index in 0 until pageSize) {
            val index = count.incrementAndGet()
            temp.add(UserEntity(id = index, name = index.toString(), age = index, gender = false))
        }
        return temp
    }
}

/**
 * UserRemoteMediator
 *
 * @property db
 * @property apiService
 */
@OptIn(ExperimentalPagingApi::class)
class UserRemoteMediator(
    private val db: MyDatabase,
    private val userDao: UserDao,
    private val apiService: ApiService // 서버에서 데이터를 가져오는 API 서비스
) : RemoteMediator<Int, UserEntity>() {
    private val tag = javaClass.simpleName

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, UserEntity>
    ): MediatorResult {
        return try {
            Log.d("UserRemoteMediator", "load loadType = $loadType, state = $state")
            when (loadType) {
                LoadType.REFRESH -> return MediatorResult.Success(endOfPaginationReached = false)
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.pages.lastOrNull() ?: return MediatorResult.Error(
                        NullPointerException("PagingState.pages is null")
                    )
                    val nextKey = if (lastItem.prevKey == null && lastItem.nextKey == null) {
                        1
                    } else if (lastItem.prevKey != null && lastItem.nextKey == null) {
                        lastItem.prevKey!! + 2
                    } else {
                        lastItem.nextKey!!
                    }
                    // API에서 데이터를 가져옵니다.
                    val response = apiService.getUsers(nextKey, state.config.pageSize)
                    Log.d(tag, "load anchorPosition = ${state.anchorPosition}, nextKey = $nextKey, response = $response")

                    // 데이터를 DB에 삽입
                    db.withTransaction {
                        userDao.insertAll(response)
                    }
                    // 더 이상 가져올 데이터가 없으면 true, 있으면 false
                    MediatorResult.Success(endOfPaginationReached = response.isEmpty())
                }
            }

        } catch (exception: Exception) {
            // 예외 처리
            MediatorResult.Error(exception)
        }
    }

        /*private var pageSize = 0

        override suspend fun load(
            loadType: LoadType, state: PagingState<Int, UserEntity>
        ): MediatorResult {
            Log.d(tag, "load loadType = $loadType, state = $state")
            pageSize = state.config.pageSize
            return when (loadType) {
                LoadType.REFRESH -> refresh()
                LoadType.PREPEND -> loadAfter()
                LoadType.APPEND -> loadBefore()
            }
        }

        private suspend fun refresh(): MediatorResult {
            Log.d(tag, "refresh")
            try {
                val list = apiService.getUsers(0, pageSize)
                if (list.isNotEmpty()) {
                    db.withTransaction {
                        userDao.deleteAllList()
                        userDao.insertAll(list)
                    }
                }
            } catch (e: Exception) {
                return MediatorResult.Error(e)
            }
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        private suspend fun loadBefore(): MediatorResult {
            Log.d(tag, "loadBefore")
            *//*var earliestUser: UserEntity? = null
            db.withTransaction {
                earliestUser = userDao.getEarliestPost()
            }

            val earliestDate = earliestUser?.date ?: WPDateConverter.currentDate()
            Log.d(tag, "earliestDate = $earliestDate")*//*

            try {
                val list = apiService.getUsers(0, pageSize)
                if (list.isNotEmpty()) {
                    db.withTransaction {
                        userDao.insertAll(list)
                    }
                }
                return MediatorResult.Success(endOfPaginationReached = list.isEmpty())
            } catch (e: Exception) {
                return MediatorResult.Error(e)
            }

        }

        private suspend fun loadAfter(): MediatorResult {
            Log.d(tag, "loadAfter")
            *//*var latestUser: UserEntity? = null
            db.withTransaction {
                latestUser = userDao.getLatestPost()
            }

            if (latestUser == null) {
                return MediatorResult.Error(IllegalStateException())
            }

            val latestDate = latestUser!!.date*//*
            try {
                val list = apiService.getUsers(0, pageSize)
                if (list.isNotEmpty()) {
                    db.withTransaction {
                        userDao.insertAll(list)
                    }
                }
                return MediatorResult.Success(endOfPaginationReached = list.isEmpty())
            } catch (e: Exception) {
                return MediatorResult.Error(e)
            }
        }*/
}