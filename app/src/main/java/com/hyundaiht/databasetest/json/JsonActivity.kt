package com.hyundaiht.databasetest.json

import android.annotation.SuppressLint
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
import com.hyundaiht.databasetest.TitleAndButton
import com.hyundaiht.databasetest.createRandomPush
import com.hyundaiht.databasetest.ui.MyDatabase
import com.hyundaiht.databasetest.ui.PushEntity
import com.hyundaiht.databasetest.ui.theme.DataBaseTestTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JsonActivity : ComponentActivity() {
    private val tag = javaClass.simpleName
    private lateinit var db: MyDatabase
    private val ioCoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewModelProvider.Factory 사용하여 ViewModel 주입
        db = MyDatabase.getInstance(this@JsonActivity)

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
                            title = "insertCanonical 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    val item = """{ "a": 1, "b": { "x": 10 } }"""
                                    db.jsonTextDao().insertCanonical(item)
                                }
                            }
                        )

                        TitleAndButton(
                            title = "selectPayload 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    val item = db.jsonTextDao().selectPayload(1)
                                    Log.d("JsonActivity", "selectPayload item = $item")
                                }
                            }
                        )

                        TitleAndButton(
                            title = "extractBX 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    val item = db.jsonTextDao().extractBX(1)
                                    Log.d("JsonActivity", "extractBX item = $item")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
