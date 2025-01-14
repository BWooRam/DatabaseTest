package com.hyundaiht.databasetest.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hyundaiht.databasetest.ui.UserDao
import com.hyundaiht.databasetest.ui.UserEntity

class UserPagingSource(
    private val dao: UserDao,
) : PagingSource<Int, UserEntity>() {
    private val tag = javaClass.simpleName
    private val factor = PagingFactor()

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, UserEntity> {
        try {
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            val totalItemSize = dao.getItemCount()
            Log.d(tag,"currentPage = $currentPage, pageSize = $pageSize, totalItemSize = $totalItemSize")

            //fromIndex, toIndex 계산
            val index = factor.getIndexFromAndTo(currentPage, pageSize, totalItemSize)
            //prevKey, nextKey 계산
            val key = factor.getPrevKeyAndNextKey(currentPage, totalItemSize, index.second)
            Log.d(tag,"index = $index, key = $key")

            val data = dao.allList(pageSize, index.first)
            Log.d(tag,"data size = ${data.size}, list = $data")

            return if(data.isEmpty()) LoadResult.Invalid() else LoadResult.Page(
                data = data,
                prevKey = key.first, // Only paging forward.
                nextKey = key.second
            )
        } catch (e: Exception) {
            Log.d(tag, "Exception e = $e")
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, UserEntity>): Int? {
        val anchorPosition = state.anchorPosition
        Log.d(tag, "getRefreshKey anchorPosition = $anchorPosition")
        return state.anchorPosition?.let { position ->
            val anchorPage = state.closestPageToPosition(position)
            Log.d(tag, "getRefreshKey prevKey = ${anchorPage?.prevKey}, nextKey = ${anchorPage?.nextKey}")
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}