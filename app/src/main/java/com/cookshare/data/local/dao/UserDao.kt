package com.cookshare.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cookshare.data.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE uid = :userId")
    fun getUserById(userId: String): LiveData<User?>

    @Query("SELECT * FROM users WHERE uid = :userId")
    suspend fun getUserByIdSync(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE uid = :userId")
    suspend fun deleteUser(userId: String)
}