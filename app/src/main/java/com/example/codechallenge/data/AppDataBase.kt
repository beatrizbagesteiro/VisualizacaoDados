package com.example.codechallenge.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProducaoDiaria::class, ControleLeiteiro::class], version = 1)
abstract class AppDataBase:RoomDatabase() {
    abstract fun controleDao(): ControleDao
    abstract fun producaoDao(): ProducaoDao
}