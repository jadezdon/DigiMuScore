package com.zhouppei.digimuscore.data.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(obj: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg obj: T)

    @Update
    suspend fun update(obj: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAll(vararg obj: T)

    @Delete
    suspend fun delete(obj: T)
}