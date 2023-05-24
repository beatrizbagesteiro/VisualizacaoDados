package com.example.codechallenge

import android.app.Application
import androidx.room.Room
import com.example.codechallenge.data.AppDataBase

class CodeChallangeApplication:Application() {

    private lateinit var dataBase: AppDataBase

    override fun onCreate() {
        super.onCreate()

        dataBase = Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java, "database-codechallange"
        ).build()


    }

    fun getAppDataBase(): AppDataBase {
        return dataBase
    }
}