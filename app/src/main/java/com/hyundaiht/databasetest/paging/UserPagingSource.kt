package com.hyundaiht.databasetest.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hyundaiht.databasetest.ui.UserDao
import com.hyundaiht.databasetest.ui.UserEntity

class UserPagingSource(
    private val dao: UserDao,
) : PagingSource<Int, UserEntity>() {
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, UserEntity> {
        try {
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            val totalItemSize = dao.getItemCount()
            Log.d("UserPagingSource","currentPage = $currentPage, pageSize = $pageSize, totalItemSize = $totalItemSize")

            //fromIndex, toIndex 계산
            val index = getIndexFromAndTo(currentPage, pageSize, totalItemSize)
            //prevKey, nextKey 계산
            val key = getPrevKeyAndNextKey(currentPage, totalItemSize, index.second)
            Log.d("UserPagingSource","index = $index, key = $key")

            val data = dao.allList(pageSize, index.first)
            Log.d("UserPagingSource","data size = ${data.size}, list = $data")

            return LoadResult.Page(
                data = data,
                prevKey = key.first, // Only paging forward.
                nextKey = key.second
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    /**
     * getPrevKeyAndNextKey
     *
     * @param currentPage
     * @param toIndex
     * @param totalItemSize
     * @return Pair(prevKey, nextKey)
     */
    fun getPrevKeyAndNextKey(currentPage: Int, totalItemSize: Int, toIndex: Int): Pair<Int?, Int?> {
        val prevKey = if (currentPage <= 1) null else currentPage - 1
        val nextKey = if (toIndex < totalItemSize) currentPage + 1 else null
        return prevKey to nextKey
    }

    /**
     * getIndexFromAndTo
     *
     * @param currentPage
     * @param pageSize
     * @param totalItemSize
     * @return Pair(fromIndex, toIndex)
     */
    fun getIndexFromAndTo(currentPage: Int, pageSize: Int, totalItemSize: Int): Pair<Int, Int> {
        val page = if (currentPage < 1) 1 else currentPage
        val fromIndex = (page - 1) * pageSize
        val toIndex = (fromIndex + pageSize).coerceAtMost(totalItemSize)
        return fromIndex to toIndex
    }

    override fun getRefreshKey(state: PagingState<Int, UserEntity>): Int? {
        // Try to find the page key of the closest page to anchorPosition from
        // either the prevKey or the nextKey; you need to handle nullability
        // here.
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey are null -> anchorPage is the
        //    initial page, so return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}