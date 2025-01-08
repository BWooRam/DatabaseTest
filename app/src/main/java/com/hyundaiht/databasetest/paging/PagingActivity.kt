package com.hyundaiht.databasetest.paging

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewModelProvider.Factory 사용하여 ViewModel 주입
        val db = MyDatabase.getInstance(this@PagingActivity)

        /*CoroutineScope(Dispatchers.Default).launch {
            for(index in 0 until 100)
            db.userDao().insert(createRandomUser())
        }*/

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

    @Composable
    fun UserItem(user: UserEntity) {
        Text(text = "User: ${user.name}")
    }
}
