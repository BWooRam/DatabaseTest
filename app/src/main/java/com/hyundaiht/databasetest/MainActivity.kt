package com.hyundaiht.databasetest

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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.hyundaiht.databasetest.ui.ExampleEntity
import com.hyundaiht.databasetest.ui.MyDatabase
import com.hyundaiht.databasetest.ui.NewMyDatabase
import com.hyundaiht.databasetest.ui.PushEntity
import com.hyundaiht.databasetest.ui.UserEntity
import com.hyundaiht.databasetest.ui.theme.DataBaseTestTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val tag = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                            title = "DB 생성 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "DB 생성",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                val db = MyDatabase.getInstance(this@MainActivity)
                                Log.d(tag, "DB 생성 결과 db = $db")
                                CoroutineScope(Dispatchers.Default).launch {
                                    runCatching {
                                        db.exampleDao().allList()
                                    }.onSuccess {
                                        Log.d(tag, "DB 생성 테스트 onSuccess data = $it")
                                    }.onFailure {
                                        Log.d(tag, "DB 생성 테스트 onFailure error = $it")
                                    }
                                }
                            }
                        )

                        TitleAndButton(
                            title = "DB Random insert 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "DB insert 실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                val db = MyDatabase.getInstance(this@MainActivity)
                                CoroutineScope(Dispatchers.Default).launch {
                                    runCatching {
                                        db.exampleDao().insert(createRandomEntity())
                                    }.onSuccess {
                                        Log.d(tag, "DB Random insert 테스트 onSuccess")
                                    }.onFailure {
                                        Log.d(tag, "DB Random insert 테스트 onFailure error = $it")
                                    }
                                }
                            }
                        )

                        TitleAndButton(
                            title = "DB 데이터 수동 이전 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "DB 이전 실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                val db = MyDatabase.getInstance(this@MainActivity)
                                Log.d(tag, "DB 수동 이전 결과 db = $db")
                                CoroutineScope(Dispatchers.Default).launch {
                                    runCatching {
//                                        db.exampleDao().insert(createRandomEntity())
//                                        db.exampleDao().insert(createRandomEntity())
//                                        db.exampleDao().insert(createRandomEntity())
//                                        db.userDao().insert(createRandomUser())
//                                        db.userDao().insert(createRandomUser())
//                                        db.userDao().insert(createRandomUser())
                                        db.exampleDao().allList()
                                    }.onSuccess {
                                        Log.d(tag, "DB 데이터 이전 테스트 onSuccess data = $it")
                                    }.onFailure {
                                        Log.d(tag, "DB 데이터 이전 테스트 onFailure error = $it")
                                    }
                                }
                            }
                        )

                        TitleAndButton(
                            title = "DB 데이터 자동 이전 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "DB 이전 실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                val db = NewMyDatabase.getInstance(this@MainActivity)
                                Log.d(tag, "DB 자동 이전 결과 db = $db")
                                CoroutineScope(Dispatchers.Default).launch {
                                    runCatching {
//                                        db.exampleDao().insert(createRandomEntity())
//                                        db.exampleDao().insert(createRandomEntity())
//                                        db.exampleDao().insert(createRandomEntity())
//                                        db.userDao().insert(createRandomUser())
//                                        db.userDao().insert(createRandomUser())
//                                        db.userDao().insert(createRandomUser())
                                        db.exampleDao().allList()
                                    }.onSuccess {
                                        Log.d(tag, "DB 데이터 이전 테스트 onSuccess data = $it")
                                    }.onFailure {
                                        Log.d(tag, "DB 데이터 이전 테스트 onFailure error = $it")
                                    }
                                }
                            }
                        )

                        TitleAndButton(
                            title = "이전 DB 데이터 삭제 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "이전 DB 삭제 실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                val isDelete = deleteDatabase("ASDFASDF.db")
                                Log.d(tag, "DB 삭제 결과 isDelete = $isDelete")
                            }
                        )
                    }
                }
            }
        }
    }
}

fun createRandomEntity(): ExampleEntity {
    val randomId = Random.nextInt(0, 10000)
    val randomString = randomId.toString()
    return ExampleEntity(id = randomId, randomString)
}

fun createRandomUser(): UserEntity {
    val randomId = Random.nextInt(0, 10000)
    val randomString = randomId.toString()
    val randomAge = Random.nextInt(0, 99)
    val randomGender = Random.nextBoolean()
    return UserEntity(
        id = randomId,
        name = randomString,
        age = randomAge,
        gender = randomGender
    )
}

fun createRandomPush(): PushEntity {
    val randomId = Random.nextInt(0, 10000)
    val randomString = randomId.toString()
    val randomAge = Random.nextInt(0, 99)
    val randomGender = Random.nextBoolean()
    return PushEntity(
        uuid = randomId,
        name = randomString,
        age = randomAge,
        gender = randomGender
    )
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
