package com.example.codechallenge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.codechallenge.data.AppDataBase
import com.example.codechallenge.data.ControleLeiteiro
import com.example.codechallenge.data.ProducaoDiaria
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange

class MainActivity : AppCompatActivity() {

    lateinit var db:AppDataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java, "database-codechallenge"
        ).build()

        val daoControle = db.controleDao()
        val daoDiario = db.producaoDao()


        val httpTransport: HttpTransport = NetHttpTransport()
        val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()


        val credentials = GoogleCredential.fromStream(resources.openRawResource(R.raw.codechallenge_387317_38977e37a5b0))
            .createScoped(listOf("https://www.googleapis.com/auth/spreadsheets.readonly"))

        val sheetsService = Sheets.Builder(httpTransport, jsonFactory, credentials).build()
        val spreadsheetId = "1u722U5dFJ-jfUWScqteceHHbydtBxpxdTXWrL7wa14k"




        val runnable = Runnable{
            val producaoDiariaData = lerDadosProducaoDiaria(sheetsService, spreadsheetId, 1575878870)
            val producaoDiariaList = converterDadosParaProducaoDiaria(producaoDiariaData)

            val controleLeiteiroData = lerDadosControleLeiteiro(sheetsService, spreadsheetId, 777965068)
            val controleLeiteiroList = converterDadosParaControleLeiteiro(controleLeiteiroData)
            adicionarDadosNoBancoDeDados(producaoDiariaList, controleLeiteiroList)
        }

        val thread =Thread(runnable)
        thread.start()

    }

    fun lerDadosProducaoDiaria(sheetsService: Sheets, spreadsheetId: String, sheetId: Int): List<List<Any>> {
        val range = "$sheetId!A:G" // Defina o intervalo de células que deseja ler
        val response: ValueRange = sheetsService.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute()

        return response.getValues() ?: emptyList()
    }

    fun lerDadosControleLeiteiro(sheetsService: Sheets, spreadsheetId: String, sheetId: Int): List<List<Any>> {
        val range = "$sheetId!A:K" // Defina o intervalo de células que deseja ler
        val response: ValueRange = sheetsService.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute()

        return response.getValues() ?: emptyList()
    }

    fun converterDadosParaProducaoDiaria(data: List<List<Any>>): List<ProducaoDiaria> {
        val producaoDiariaList = mutableListOf<ProducaoDiaria>()
        for (row in data) {
            val id = row[0].toString().toInt()
            val totAnimal = row[1].toString().toInt()
            val primeiraOrdenha = row[2].toString().toFloat()
            val segundaOrdenha = row[3].toString().toFloat()
            val totLitrosDia = row[4].toString().toFloat()
            val media = row[5].toString().toFloat()
            val data = row[6].toString()
            val producaoDiaria = ProducaoDiaria(id,totAnimal, primeiraOrdenha,segundaOrdenha,totLitrosDia,media,data)
            producaoDiariaList.add(producaoDiaria)
        }
        return producaoDiariaList
    }
    fun converterDadosParaControleLeiteiro(data: List<List<Any>>): List<ControleLeiteiro> {
        val controleLeiteiroList = mutableListOf<ControleLeiteiro>()
        for (row in data) {
            val controleLeiteiro = ControleLeiteiro(
                id = row[0].toString().toInt(),
                microchip = row[1].toString().toInt(),
                numAnimal = row[2].toString().toInt(),
                nome = row[3].toString(),
                dataParto = row[4].toString(),
                baia = row[5].toString().toInt(),
                primOrdenha = row[6].toString().toFloat(),
                segOrdenha = row[7].toString().toFloat(),
                total = row[8].toString().toFloat(),
                dataControle = row[9].toString(),
                del = row[10].toString().toInt()
            )
            controleLeiteiroList.add(controleLeiteiro)
        }
        return controleLeiteiroList
    }


    fun adicionarDadosNoBancoDeDados(producaoDiariaList: List<ProducaoDiaria>, controleLeiteiroList: List<ControleLeiteiro>) {

        val producaoDiariaDao = db.producaoDao()
        val controleLeiteiroDao = db.controleDao()

        for (ProducaoDiaria in producaoDiariaList){
            producaoDiariaDao.insertProd(ProducaoDiaria)
        }

        for (ControleLeiteiro in controleLeiteiroList){
            controleLeiteiroDao.insertControle(ControleLeiteiro)
        }

    }
}