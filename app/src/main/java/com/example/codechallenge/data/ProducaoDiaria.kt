package com.example.codechallenge.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class ProducaoDiaria(
    @PrimaryKey
    val id:String,
    val totAnimal:Int,
    val primeiraOrdenha:Float,
    val segOrdenha:Float,
    val totLitrosDia:Float,
    val media:Float,
    val data: String
)
