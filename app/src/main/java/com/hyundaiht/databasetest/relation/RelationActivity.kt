package com.hyundaiht.databasetest.relation

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
import com.hyundaiht.databasetest.TitleAndButton
import com.hyundaiht.databasetest.ui.theme.DataBaseTestTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class RelationActivity : ComponentActivity() {
    private val tag = javaClass.simpleName
    private lateinit var db: RelationDatabase
    private val ioCoroutineScope = CoroutineScope(Dispatchers.IO)
    private val user = createRandomUser()
    private val group = createRandomGroup()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = RelationDatabase.getInstance(this@RelationActivity)

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
                            title = "DB Random Default Insert 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    db.userRelationDao().insertUser(user)
                                    db.userRelationDao().insertGroup(group)
                                    db.userRelationDao()
                                        .insertReservation(createRandomReservation(user.userId))
                                    db.userRelationDao().insertUserGroupCrossRef(
                                        UserGroupCrossRef(
                                            userId = user.userId,
                                            groupId = group.groupId
                                        )
                                    )

                                    //Push 5개 추가
                                    for (index in 0 until 5) {
                                        db.userRelationDao()
                                            .insertPush(createRandomPushList(user.userId))
                                    }
                                }
                            }
                        )

                        TitleAndButton(
                            title = "DB getAllList 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val list = db.userRelationDao().getUserWithInfo(user.userId)
                                    Log.d(tag, "getUserWithInfo list = $list")
                                }
                            }
                        )

                        TitleAndButton(
                            title = "DB getUserWithGroups 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val list =
                                        db.userRelationDao().getUserWithGroups(userId = user.userId)
                                    Log.d(tag, "getUserWithGroups list = $list")
                                }
                            }
                        )

                        TitleAndButton(
                            title = "DB getGroupWithUsers 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val list = db.userRelationDao()
                                        .getGroupWithUsers(groupId = group.groupId)
                                    Log.d(tag, "getGroupWithUsers list = $list")
                                }
                            }
                        )

                        TitleAndButton(
                            title = "DB getUserGroupPushInfo 테스트",
                            titleModifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            buttonName = "실행",
                            buttonModifier = Modifier
                                .wrapContentSize(),
                            clickEvent = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val list = db.userRelationDao().getUserGroupPushInfo()
                                    Log.d(tag, "getUserGroupPushInfo list = $list")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun createRandomUser(): User {
        val randomId = Random.nextLong(0, 10000)
        val randomString = randomId.toString()
        val randomAge = Random.nextInt(0, 99)
        return User(
            userId = randomId,
            name = "name$randomString",
            age = randomAge
        )
    }

    private fun createRandomReservation(userId: Long): Reservation {
        val randomId = Random.nextLong(0, 10000)
        val randomString = randomId.toString()
        return Reservation(
            reservationId = randomId,
            date = "date$randomString",
            location = "location$randomString",
            userOwnerId = userId,
        )
    }

    private fun createRandomGroup(): Group {
        val randomId = Random.nextLong(0, 10000)
        val randomString = randomId.toString()
        return Group(
            groupId = randomId,
            groupName = randomString,
        )
    }

    private fun createRandomPushList(userid: Long): Push {
        val randomId = Random.nextLong(0, 10000)
        val randomString = randomId.toString()

        return Push(
            userOwnerId = userid,
            pushId = randomId,
            message = "message$randomString",
            timestamp = randomId
        )
    }
}
