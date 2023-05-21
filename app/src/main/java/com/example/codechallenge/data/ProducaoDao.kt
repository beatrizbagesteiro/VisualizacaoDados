package com.example.codechallenge.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface ProducaoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProd(producaoDiaria: ProducaoDiaria)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateProd(producaoDiaria: ProducaoDiaria)

}