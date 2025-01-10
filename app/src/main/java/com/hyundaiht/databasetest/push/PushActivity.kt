package com.hyundaiht.databasetest.push

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hyundaiht.databasetest.ui.MyDatabase
import com.hyundaiht.databasetest.ui.PushEntity
import com.hyundaiht.databasetest.ui.theme.DataBaseTestTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PushActivity : ComponentActivity() {
    private val tag = javaClass.simpleName
    private lateinit var viewModel: PushViewModel
    private lateinit var db: MyDatabase
    private val ioCoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewModelProvider.Factory 사용하여 ViewModel 주입
        db = MyDatabase.getInstance(this@PushActivity)

        /*CoroutineScope(Dispatchers.Default).launch {
            for(index in 0 until 100)
            db.pushDao().insert(createRandomPush())
        }*/

        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PushViewModel(db.pushDao()) as T
            }
        }

        viewModel = ViewModelProvider(this, viewModelFactory)[PushViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            DataBaseTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        var query by remember { mutableStateOf("") }
                        TextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                        PushScreen(query)
                    }
                }
            }
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    fun PushScreen(query: String) {
        val userPagingData = remember { mutableStateOf<List<PushEntity>?>(null) }
        ioCoroutineScope.launch {
            if(query.isEmpty())
                return@launch

            userPagingData.value = db.pushDao().searchPush(query)
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(userPagingData.value?.size ?: 0) { index ->
                val item = userPagingData.value!![index]

                PushItem(push = item)
            }
        }
    }

    @Composable
    fun PushItem(push: PushEntity) {
        Text(text = "Push: ${push.name}")
    }
}
