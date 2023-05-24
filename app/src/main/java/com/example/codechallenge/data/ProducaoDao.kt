package com.example.codechallenge.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface ProducaoDao {


    @Upsert()
    fun upsert(producaoDiaria: ProducaoDiaria)

    @Query("SELECT totAnimal FROM producaodiaria WHERE data = :selectedData")
    fun somaTotAnimais(selectedData: String): Int

    @Query("SELECT primeiraOrdenha FROM producaodiaria WHERE data = :selectedData")
    fun primOrdenha(selectedData: String):Float

    @Query("SELECT segOrdenha FROM producaodiaria WHERE data = :selectedData")
    fun segOrdenha(selectedData: String):Float

    @Query("SELECT totLitrosDia FROM producaodiaria WHERE data = :selectedData")
    fun totLitros(selectedData: String):Float

    @Query("SELECT media FROM producaodiaria WHERE data = :selectedData")
    fun mediaLitros(selectedData: String):Float

    @Query("SELECT primeiraOrdenha,segOrdenha, data FROM ProducaoDiaria ORDER BY data DESC LIMIT 10")
    fun ordenhasRecentes():List<OrdenhasRecentes>





}