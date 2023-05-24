package com.example.codechallenge.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ControleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertControle (controleLeiteiro: ControleLeiteiro)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(controleLeiteiro: ControleLeiteiro)
    @Query("SELECT del FROM ControleLeiteiro")
    fun getDel():Int
    @Query("SELECT numAnimal, total from ControleLeiteiro order by total desc limit 10")
    fun top10():List<Top10Ordenhas>

    @Query("SELECT total FROM ControleLeiteiro")
    fun getTotalLitros(): Float

    @Query("SELECT SUM(primOrdenha) FROM ControleLeiteiro")
    fun getSomaPrimOrdenha(): Float

    @Query("SELECT SUM(segOrdenha) FROM ControleLeiteiro")
    fun getSomaSegOrdenha(): Float

    @Query("SELECT AVG(total) FROM ControleLeiteiro")
    fun getMediaGeralOrdenhas(): Float
}