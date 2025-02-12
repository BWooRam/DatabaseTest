package com.hyundaiht.databasetest.relation

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "reservation",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userOwnerId"])],
    primaryKeys = ["reservationId"]
)
data class Reservation(
    val reservationId: Long,
    val date: String,
    val location: String,
    val userOwnerId: Long  // User와 연결되는 외래 키
)

@Entity(tableName = "group")
data class Group(
    @PrimaryKey(autoGenerate = true) val groupId: Long = 0,
    val groupName: String
)

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val name: String,
    val age: Int
)

@Entity(
    tableName = "push",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userOwnerId"],
            onDelete = ForeignKey.CASCADE // 부모 삭제 시 같이 삭제됨
        )
    ],
    indices = [Index(value = ["userOwnerId"])]
)
data class Push(
    @PrimaryKey(autoGenerate = true) val pushId: Long = 0,
    val message: String,
    val timestamp: Long,
    val userOwnerId: Long  // User와 연결되는 외래 키
)

/**
 * 다대다 관계를 위해서 Entity 재정의
 *
 * @property userId
 * @property groupId
 */
@Entity(
    tableName = "user_group_cross_ref",
    primaryKeys = ["userId", "groupId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Group::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["groupId"])]
)
data class UserGroupCrossRef(
    val userId: Long,
    val groupId: Long
)

/**
 * 일대일, 일대다 관계 데이터 클래스
 *
 * @property user
 * @property pushes
 * @property reservation
 */
data class UserWithInfo(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userOwnerId"
    )
    val pushes: List<Push>,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userOwnerId"
    )
    val reservation: Reservation?
)


/**
 * 다대다 관계 데이터 클래스
 *
 * @property user
 * @property groups
 */
data class UserWithGroups(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "groupId",
        associateBy = Junction(UserGroupCrossRef::class)
    )
    val groups: List<Group>
)

/**
 * 다대다 관계 데이터 클래스
 *
 * @property group
 * @property users
 */
data class GroupWithUsers(
    @Embedded val group: Group,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "userId",
        associateBy = Junction(UserGroupCrossRef::class)
    )
    val users: List<User>
)

@Dao
interface UserRelationDao {
    @Insert
    fun insertUser(user: User): Long

    @Insert
    fun insertPush(push: Push)

    @Insert
    fun insertReservation(reservation: Reservation)

    @Insert
    fun insertGroup(group: Group): Long

    @Insert
    fun insertUserGroupCrossRef(crossRef: UserGroupCrossRef)

    @Transaction
    @Query("SELECT * FROM user WHERE userId = :userId")
    fun getUserWithInfo(userId: Long): UserWithInfo

    @Transaction
    @Query("SELECT * FROM user WHERE userId = :userId")
    fun getUserWithGroups(userId: Long): UserWithGroups

    @Transaction
    @Query("SELECT * FROM `group` WHERE groupId = :groupId")
    fun getGroupWithUsers(groupId: Long): GroupWithUsers
}

@Database(
    entities = [
        Reservation::class,
        User::class,
        Push::class,
        Group::class,
        UserGroupCrossRef::class
    ],
    version = 1
)
abstract class RelationDatabase : RoomDatabase() {
    abstract fun userRelationDao(): UserRelationDao

    companion object {
        fun getInstance(context: Context): RelationDatabase {
            return synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, RelationDatabase::class.java, "relation_database.db"
                ).build()

                instance
            }
        }
    }
}