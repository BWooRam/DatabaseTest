package com.hyundaiht.databasetest.ui

import android.content.Context
import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.io.FileNotFoundException

@Entity(tableName = "example_table")
data class ExampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val data: String,
    val createdAt: Long = System.currentTimeMillis() // 생성 시간 기록
)

/*@Entity(tableName = "user2")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val age: Int,
    val gender: Boolean
)*/

@Dao
interface ExampleDao {
    @Query("DELETE FROM example_table WHERE :currentTime - createdAt > :expiryTime")
    suspend fun deleteExpiredData(currentTime: Long, expiryTime: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exampleEntity: ExampleEntity)

    @Query("SELECT * FROM example_table")
    suspend fun allList(): List<ExampleEntity>
}

/*@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user1Entity: UserEntity)

    @Query("SELECT * FROM user2")
    suspend fun allList(): List<UserEntity>
}*/

@Database(entities = [ExampleEntity::class], version = 1)
abstract class MyDatabase : RoomDatabase() {
    abstract fun exampleDao(): ExampleDao

    companion object {
        fun getInstance(context: Context): MyDatabase {
            return synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, MyDatabase::class.java, "example_database.db"
                )
//                    .createFromFile(getDBfile(context, "example_database.db"))
//                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
//                    .enableMultiInstanceInvalidation()
//                    .fallbackToDestructiveMigration()
                    .build()
                instance
            }
        }
    }
}

/**
 * 자동 autoMigrations 관련 테스트 진행
 * # 요구 사항
 * - build gradle에 스키마 관련 코드 필요
 * - app > schemas에 각 version에 관련된 schema가 정의 필요
 */
@Database(
    entities = [
        ExampleEntity::class,
//        UserEntity::class,
    ],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, AutoMigration_1_2::class),
        AutoMigration(from = 2, to = 3, AutoMigration_2_3::class),
        AutoMigration(from = 3, to = 4, AutoMigration_3_4::class),
        AutoMigration(from = 4, to = 5, AutoMigration_4_5::class),
    ]
)
abstract class NewMyDatabase : RoomDatabase() {
    abstract fun exampleDao(): ExampleDao
//    abstract fun userDao(): UserDao

    companion object {
        fun getInstance(context: Context): NewMyDatabase {
            return synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, NewMyDatabase::class.java, "example_database.db"
                )
//                    .createFromFile(getDBfile(context, "example_database.db"))
//                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
//                    .enableMultiInstanceInvalidation()
//                    .fallbackToDestructiveMigration()
                    .build()
                instance
            }
        }
    }
}

private fun getDBfile(context: Context, dbName: String): File {
//            val dbFile = context.getDatabasePath(dbName)
    val dbFile = File("/data/data/com.hyundaiht.databasetest/files", dbName).apply {
        setReadable(true)
        setWritable(true)
    }
    if (!dbFile.exists()) {
        throw FileNotFoundException("Database file not found at: ${dbFile.absolutePath}")
    }
    Log.d("MyDatabase", "getDBfile file = $dbFile")
    return dbFile
}


@RenameTable(fromTableName = "user1", toTableName = "user2")
class AutoMigration_1_2 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
//        db.execSQL("UPDATE WorkSpec SET `last_enqueue_time` = -1 WHERE `last_enqueue_time` = 0")
    }
}

@RenameColumn(tableName = "user2", fromColumnName = "fullName", toColumnName = "name")
class AutoMigration_2_3 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
//        db.execSQL("UPDATE WorkSpec SET `last_enqueue_time` = -1 WHERE `last_enqueue_time` = 0")
    }
}

@DeleteColumn(tableName = "user2", columnName = "middleName")
class AutoMigration_3_4 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
//        db.execSQL("UPDATE WorkSpec SET `last_enqueue_time` = -1 WHERE `last_enqueue_time` = 0")
    }
}

@DeleteTable(tableName = "user2")
class AutoMigration_4_5 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
//        db.execSQL("UPDATE WorkSpec SET `last_enqueue_time` = -1 WHERE `last_enqueue_time` = 0")
    }
}