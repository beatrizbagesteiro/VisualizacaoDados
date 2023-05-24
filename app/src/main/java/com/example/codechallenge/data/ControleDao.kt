package com.example.codechallenge.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface ControleDao {

    @Upsert
    fun upsert(controleLeiteiro: ControleLeiteiro)

    @Query("SELECT del FROM ControleLeiteiro")
    fun getDel():Int
    @Query("SELECT numAnimal,total FROM ControleLeiteiro ORDER BY total DESC LIMIT 10")
    fun top10():List<Top10Ordenhas>

    @Query("SELECT SUM(total) FROM ControleLeiteiro")
    fun getTotalLitros(): Float

    @Query("SELECT SUM(primOrdenha) FROM ControleLeiteiro")
    fun getSomaPrimOrdenha(): Float

    @Query("SELECT SUM(segOrdenha) FROM ControleLeiteiro")
    fun getSomaSegOrdenha(): Float

    @Query("SELECT AVG(total) FROM ControleLeiteiro")
    fun getMediaGeralOrdenhas(): Float
}