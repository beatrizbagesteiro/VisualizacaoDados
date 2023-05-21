package com.example.codechallenge.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class ControleLeiteiro(
    @PrimaryKey
    val id:Int,
    val microchip:Int,
    val numAnimal:Int,
    val nome:String,
    val dataParto:String,
    val baia:Int,
    val primOrdenha:Float,
    val segOrdenha:Float,
    val total:Float,
    val dataControle:String,
    val del:Int
)
