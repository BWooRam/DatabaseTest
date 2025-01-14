package com.hyundaiht.databasetest.paging

class PagingFactor {
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
}