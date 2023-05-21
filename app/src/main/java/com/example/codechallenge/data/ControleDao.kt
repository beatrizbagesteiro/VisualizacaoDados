package com.example.codechallenge.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface ControleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertControle (controleLeiteiro: ControleLeiteiro)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateControle (controleLeiteiro: ControleLeiteiro)
}