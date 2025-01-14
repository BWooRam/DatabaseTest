package com.hyundaiht.databasetest.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.hyundaiht.databasetest.ui.MyDatabase
import com.hyundaiht.databasetest.ui.UserDao

class PagingViewModel(private val db: MyDatabase) : ViewModel() {
    private val userDao = db.userDao()

    /**
     * cachedIn은 페이징 데이터의 중간 상태를 공유 메모리에 저장합니다.
     * 동일한 Flow를 여러 번 수집해도, 새로운 데이터 로드 없이 캐싱된 데이터를 제공합니다.
     * 스코프 생명주기:
     *
     * 지정된 CoroutineScope의 생명주기 동안 캐시가 유지됩니다.
     * 스코프가 취소되거나 종료되면 캐시도 함께 정리됩니다.
     */
    @OptIn(ExperimentalPagingApi::class)
    val userPagingData = Pager(
        config = PagingConfig(
            pageSize = 100, // 페이지 크기
            enablePlaceholders = false // 자리 표시 여부
        ),remoteMediator = UserRemoteMediator(db, userDao, FakeApiService())
    ) {
        userDao.pagingSource()
    }.flow.cachedIn(viewModelScope) // ViewModel 범위 내에서 캐싱


    /**
     * 테스트용 UserPagingSource
     */
    @OptIn(ExperimentalPagingApi::class)
    val customUserPagingData = Pager(
        config = PagingConfig(
            pageSize = 100, // 페이지 크기
            initialLoadSize = 100,
            enablePlaceholders = false // 자리 표시 여부
        )
    ) {
        UserPagingSource(userDao)
    }.flow.cachedIn(viewModelScope) // ViewModel 범위 내에서 캐싱
}