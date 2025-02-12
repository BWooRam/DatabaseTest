package com.hyundaiht.databasetest.prefill

import android.database.sqlite.SQLiteDatabase
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
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.withTransaction
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.hyundaiht.databasetest.TitleAndButton
import com.hyundaiht.databasetest.createRandomUser
import com.hyundaiht.databasetest.ui.MyDatabase
import com.hyundaiht.databasetest.ui.UserEntity
import com.hyundaiht.databasetest.ui.getDBfile
import com.hyundaiht.databasetest.ui.theme.DataBaseTestTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrefillActivity : ComponentActivity() {
    private val tag = javaClass.simpleName
    private lateinit var db: MyDatabase
    private lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewModelProvider.Factory 사용하여 ViewModel 주입
        db = MyDatabase.getInstance(this@PrefillActivity)
        workManager = WorkManager.getInstance(this@PrefillActivity)

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
                                    val userList = arrayListOf<UserEntity>()
                                    for (index in 0 until 5000) {
                                        val randomUser = createRandomUser()
                                        userList.add(randomUser)
                                    }

                                    db.userDao().insertAll(userList)
                                }
                            }
                        )

                        TitleAndButton(
                            title = "DB runInTransaction 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val beforeMaxCount = db.userDao().getItemCount()
                                    Log.d(tag, "runInTransaction beforeMaxCount = $beforeMaxCount")

                                    kotlin.runCatching {
                                        db.withTransaction {
                                            db.userDao().insert(createRandomUser())
                                            db.userDao().insert(createRandomUser())
                                            db.userDao().insert(createRandomUser())
                                            throw Throwable()
                                        }
                                    }.onFailure {
                                        Log.d(tag, "runInTransaction onFailure error = $it")
                                    }

                                    val afterMaxCount = db.userDao().getItemCount()
                                    Log.d(tag, "runInTransaction afterMaxCount = $afterMaxCount")
                                }
                            }
                        )

                        TitleAndButton(
                            title = "DB PrefillDBWorker 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    workManager.cancelAllWork()
                                    workManager.pruneWork()

                                    val request = OneTimeWorkRequest
                                        .Builder(PrefillDBWorker::class.java)
                                        .addTag("PrefillDBWork").build()

                                    val result = workManager.enqueue(request)
                                    Log.d(tag, "PrefillDBWorker result = ${result.result}")

                                    withContext(CoroutineScope(Dispatchers.Main).coroutineContext) {
                                        workManager
                                            .getWorkInfosByTagLiveData("PrefillDBWork")
                                            .observe(this@PrefillActivity) {
                                                for (workInfo in it) {
                                                    Log.d(tag, "state = ${workInfo.state}")
                                                    Log.d(tag, "outputData = ${workInfo.outputData}")
                                                }
                                            }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
