package com.hyundaiht.databasetest.ui

import android.content.Context
import android.util.Log
import androidx.paging.PagingSource
import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.io.FileNotFoundException

@Entity(tableName = "example_table")
data class ExampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val data: String,
    val createdAt: Long = System.currentTimeMillis() // 생성 시간 기록
)

@Entity(
    tableName = "userEntity",
    indices = [Index(value = ["id"])]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val age: Int,
    val gender: Boolean
)

@Fts4
@Entity(tableName = "pushEntity")
data class PushEntity(
    @ColumnInfo(name = "uuid") val uuid: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "age") val age: Int,
    @ColumnInfo(name = "gender") val gender: Boolean
)

@Dao
interface ExampleDao {
    @Query("DELETE FROM example_table WHERE :currentTime - createdAt > :expiryTime")
    suspend fun deleteExpiredData(currentTime: Long, expiryTime: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exampleEntity: ExampleEntity)

    @Query("SELECT * FROM example_table")
    suspend fun allList(): List<ExampleEntity>
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user1Entity: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<UserEntity>)

    @Query("DELETE FROM userEntity")
    suspend fun deleteAllList()

    @Query("SELECT COUNT(*) FROM userEntity")
    suspend fun getItemCount(): Int

    @Query("SELECT COUNT(*) FROM userEntity LIMIT :limit OFFSET :offset")
    suspend fun getItemCount(limit: Int, offset: Int): Int

    @Query("SELECT * FROM userEntity ORDER BY id ASC")
    suspend fun allList(): List<UserEntity>

    @Query("SELECT * FROM userEntity ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun allList(limit: Int, offset: Int): List<UserEntity>

    @Query("SELECT * FROM userEntity")
    fun pagingSource(): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM userEntity WHERE name = :name")
    fun searchUsersName(name: String): List<UserEntity>

    @Query("SELECT * FROM userEntity WHERE id = :id")
    fun searchUsersId(id: String): List<UserEntity>
}

@Dao
interface PushDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pushEntity: PushEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pushList: List<PushEntity>)

    @Query("DELETE FROM pushEntity")
    suspend fun deleteAllList()

    @Query("SELECT COUNT(*) FROM pushEntity")
    suspend fun getItemCount(): Int

    @Query("SELECT * FROM pushEntity ORDER BY uuid ASC")
    suspend fun allList(): List<PushEntity>

    @Query("SELECT * FROM pushEntity ORDER BY uuid ASC LIMIT :limit OFFSET :offset")
    suspend fun allList(limit: Int, offset: Int): List<PushEntity>

    @Query("SELECT * FROM pushEntity")
    fun pagingSource(): PagingSource<Int, PushEntity>

    @Query("SELECT * FROM pushEntity WHERE name = :name")
    fun searchPush(name: String): List<PushEntity>

    @Query("SELECT * FROM pushEntity WHERE name MATCH :name||'*'")
    fun searchLikePush(name: String): List<PushEntity>

    @Query("SELECT * FROM pushEntity WHERE name MATCH :name")
    fun searchMatchPush(name: String): List<PushEntity>
}

/**
 * 수동 이전 테스트 : addMigrations
 * # 요구 사항 : 없음
 */
@Database(
    entities = [
        ExampleEntity::class,
        UserEntity::class,
        PushEntity::class,
    ],
    version = 4
)
abstract class MyDatabase : RoomDatabase() {
    abstract fun exampleDao(): ExampleDao
    abstract fun userDao(): UserDao
    abstract fun pushDao(): PushDao

    companion object {
        fun getInstance(context: Context): MyDatabase {
            return synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, MyDatabase::class.java, "my_database.db"
                )
//                    .createFromFile(getDBfile(context, "example_database.db"))
//                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
//                    .enableMultiInstanceInvalidation()
//                    .fallbackToDestructiveMigration()
                    .addMigrations(Migration(1, 2) { db ->
                        db.execSQL("ALTER TABLE user2 ADD COLUMN `middleName` TEXT")
                    }).addMigrations(Migration(2, 3) { db ->
                        //DROP Column이 존재하지 않아서 이런 식으로 진행해야합니다.
                        db.execSQL("CREATE TABLE user_new(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, age INTEGER NOT NULL, gender INTEGER NOT NULL)")
                        db.execSQL("INSERT INTO user_new(id, name, age, gender) SELECT id, name, age, gender FROM user2")
                        db.execSQL("DROP TABLE user2")
                        db.execSQL("ALTER TABLE user_new RENAME TO user2")
                    }).addMigrations(Migration(3, 4) { db ->
                        db.execSQL("ALTER TABLE user2 RENAME TO user")
                    })/*.addMigrations(Migration(4, 5) { db ->

                    })*/
                    .build()
                instance
            }
        }
    }
}

/**
 * 자동 이전 테스트 : autoMigrations
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

fun getDBfile(context: Context, dbName: String): File {
//            val dbFile = context.getDatabasePath(dbName)
    val dbFile = File(context.filesDir.absolutePath, dbName).apply {
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