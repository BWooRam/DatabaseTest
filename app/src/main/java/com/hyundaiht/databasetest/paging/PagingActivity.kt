package com.hyundaiht.databasetest.paging

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.hyundaiht.databasetest.createRandomUser
import com.hyundaiht.databasetest.ui.MyDatabase
import com.hyundaiht.databasetest.ui.UserEntity
import com.hyundaiht.databasetest.ui.theme.DataBaseTestTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PagingActivity : ComponentActivity() {
    private val tag = javaClass.simpleName
    private lateinit var viewModel: PagingViewModel
    private lateinit var db: MyDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewModelProvider.Factory 사용하여 ViewModel 주입
        db = MyDatabase.getInstance(this@PagingActivity)

        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PagingViewModel(db.userDao()) as T
            }
        }

        viewModel = ViewModelProvider(this, viewModelFactory)[PagingViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            DataBaseTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {

                        TitleAndButton(
                            title = "DB Random User Insert 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "Insert 실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    for (index in 0 until 1000)
                                        db.userDao().insert(createRandomUser())
                                }
                            }
                        )

                        TitleAndButton(
                            title = "DB searchUsers 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "executeSearchUser 실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    executeSearchUserName("5013731")
                                    executeSearchUserName("5014199")
                                    executeSearchUserName("8840711")
                                    executeSearchUserName("8840902")

                                    executeSearchUserId("5013731")
                                    executeSearchUserId("5014199")
                                    executeSearchUserId("8840711")
                                    executeSearchUserId("8840902")
                                }
                            }
                        )

                        UserScreen(viewModel)
                    }
                }
            }
        }
    }

    @Composable
    fun UserScreen(viewModel: PagingViewModel) {
        val userPagingData = viewModel.customUserPagingData.collectAsLazyPagingItems()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(userPagingData.itemCount) { index ->
                val item = userPagingData[index]
                item?.let {
                    UserItem(user = it)
                }
            }

            // 로딩 상태 처리
            userPagingData.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item { Text("Loading...") }
                    }

                    loadState.append is LoadState.Loading -> {
                        item { Text("Loading more...") }
                    }

                    loadState.append is LoadState.Error -> {
                        item { Text("Error loading more items") }
                    }
                }
            }
        }
    }

    private fun executeSearchUserId(id: String) {
        val beforeTime = System.currentTimeMillis()
        val search = db.userDao().searchUsersId(id)
        val afterTime = System.currentTimeMillis()
        Log.d(tag, "executeSearchUserId search = $search, time = ${afterTime - beforeTime}")
    }

    private fun executeSearchUserName(name: String) {
        val beforeTime = System.currentTimeMillis()
        val search = db.userDao().searchUsersName(name)
        val afterTime = System.currentTimeMillis()
        Log.d(tag, "executeSearchUser search = $search, time = ${afterTime - beforeTime}")
    }

    @Composable
    fun UserItem(user: UserEntity) {
        Text(text = "User: ${user.name}")
    }

    @Composable
    fun TitleAndButton(
        title: String,
        titleModifier: Modifier = Modifier,
        buttonName: String,
        buttonModifier: Modifier = Modifier,
        clickEvent: () -> Unit
    ) {
        Text(
            text = title,
            modifier = titleModifier
        )
        Button(
            onClick = clickEvent,
            modifier = buttonModifier,
            content = { Text(text = buttonName) }
        )
    }
}
