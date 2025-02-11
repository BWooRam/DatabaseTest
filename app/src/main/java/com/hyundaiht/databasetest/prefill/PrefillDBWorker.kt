package com.hyundaiht.databasetest.prefill

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.hyundaiht.databasetest.ui.MyDatabase
import com.hyundaiht.databasetest.ui.UserEntity
import com.hyundaiht.databasetest.ui.getDBfile

/**
 * 정책 : 실패시 되돌리기, 실패한 데이터 따로 저장후 재시도, 무시하기
 *
 * @constructor
 * TODO
 *
 * @param appContext
 * @param workerParams
 */
class PrefillDBWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val tag = javaClass.simpleName

    override suspend fun doWork(): Result {
        val context = this@PrefillDBWorker.applicationContext

        val dbFile = kotlin.runCatching {
            getDBfile(context, "my_database.db")
        }.onFailure {
            Log.d(tag, "getDBfile() onFailure error = $it")
        }.onSuccess {
            Log.d(tag, "onSuccess file = $it")
        }.getOrNull()

        if(dbFile == null || !dbFile.exists())
            return Result.failure(workDataOf("reason" to "getDBfile is Null")
        )

        val oldDb =
            SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

        //기존 데이터베이스에서 데이터 가져오기
        val cursor = oldDb.rawQuery("SELECT * FROM user", null)
        val userList = mutableListOf<UserEntity>()
        val failList = arrayListOf<Throwable>()
        oldDb.use {
            cursor.use {
                while (cursor.moveToNext()) {
                    kotlin.runCatching {
                        val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                        val age = cursor.getInt(cursor.getColumnIndexOrThrow("age"))
                        val gender = cursor.getInt(cursor.getColumnIndexOrThrow("gender"))
                        userList.add(UserEntity(id, name, age, gender >= 0))
                    }.onFailure {
                        failList.add(it)
                    }
                }
            }
        }

        if (failList.isNotEmpty()) {
            Log.d(tag, "doWork() failList = $failList")
            return Result.failure(workDataOf("reason" to failList))
        }

        //Room을 초기화
        val roomDb = MyDatabase.getInstance(context)
        roomDb.runInTransaction {
            roomDb.userDao().insertAll(userList)
        }

        return Result.success()
    }
}