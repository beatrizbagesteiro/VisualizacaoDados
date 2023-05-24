package com.example.codechallenge.presentation



import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codechallenge.R
import com.example.codechallenge.CodeChallangeApplication
import com.example.codechallenge.data.ControleDao
import com.example.codechallenge.data.ControleLeiteiro
import com.example.codechallenge.data.DadosCardViewGeral
import com.example.codechallenge.data.DadosCardViewSelectedData
import com.example.codechallenge.data.OrdenhasRecentes

import com.example.codechallenge.data.ProducaoDao
import com.example.codechallenge.data.ProducaoDiaria
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale


class CodeChallangeViewModel(
    private val controleDao: ControleDao,
    private val producaoDao: ProducaoDao,
    private val dispatcher:CoroutineDispatcher = Dispatchers.IO):ViewModel() {

    val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
    val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)

    //LiveData Controle Leiteiro
    private val _dadosPlanilhaControleLeiteiro = MutableLiveData<List<ControleLeiteiro>>()
    val dadosPlanilhaControleLeiteiro: LiveData<List<ControleLeiteiro>>
        get() = _dadosPlanilhaControleLeiteiro

    //LiveData Produção Daria
    private val _dadosPlanilhaProdDiaria = MutableLiveData<List<ProducaoDiaria>>()
    val dadosPlanilhaProdDiaria: LiveData<List<ProducaoDiaria>>
        get() = _dadosPlanilhaProdDiaria


    fun insertControleLeiteiro(controleLeiteiro: ControleLeiteiro){
        viewModelScope.launch (dispatcher)  {
            controleDao.insertControle(controleLeiteiro)
        }
    }


    fun atualizarControleLeiteiro(controleLeiteiro: ControleLeiteiro) {
        viewModelScope.launch(dispatcher) {
            controleDao.update(controleLeiteiro)
        }
    }

    fun insertProdDiaria(producaoDiaria: ProducaoDiaria){
        viewModelScope.launch(dispatcher){
            producaoDao.insertProd(producaoDiaria)
        }
    }
    fun atualizarProducaoDiaria(producaoDiaria: ProducaoDiaria) {
        viewModelScope.launch(dispatcher) {
            producaoDao.update(producaoDiaria)
        }
    }

    //Atualização CardView ControleLeiteiro
    private val _dadosCardViewControleGeral = MutableLiveData<List<DadosCardViewGeral>>()
    val cardViewControle: LiveData<List<DadosCardViewGeral>>
        get() = _dadosCardViewControleGeral

    fun cardViewControleGeral() {
        viewModelScope.launch(dispatcher) {
            val totalLitros = controleDao.getTotalLitros()
            val somaPrimOrdenha = controleDao.getSomaPrimOrdenha()
            val somaSegOrdenha = controleDao.getSomaSegOrdenha()
            val mediaGeralOrdenhas = controleDao.getMediaGeralOrdenhas()
            val getDel = controleDao.getDel()

            val cardViewGeral = DadosCardViewGeral(
                totalLitros = totalLitros,
                somaPrimOrdenha = somaPrimOrdenha,
                somaSegOrdenha = somaSegOrdenha,
                mediaGeralOrdenhas = mediaGeralOrdenhas,
                del = getDel
            )
            _dadosCardViewControleGeral.postValue(listOf(cardViewGeral))
        }
    }

    private val _barChartData = MutableLiveData<BarData>()
    val barChartData: LiveData<BarData> = _barChartData

    fun generateBarChartData(barChartMelhores:BarChart) {
        viewModelScope.launch(dispatcher) {
            val top10Data = controleDao.top10()

            withContext(Dispatchers.Main) {
                val entries = top10Data.mapIndexed { index, data ->
                    BarEntry(index.toFloat(), data.total)
                }

                val numerosAnimais = top10Data.map { it.numAnimal }

                val xAxisLabels = numerosAnimais.map { it.toString() }.toTypedArray()
                val xAxisFormatter = IndexAxisValueFormatter(xAxisLabels)

                val xAxis = barChartMelhores.xAxis
                xAxis.valueFormatter = xAxisFormatter


                val dataSet = BarDataSet(entries, "Litros")
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS, 255)
                dataSet.valueTextColor = Color.BLACK

                val chartData = BarData(dataSet)
                _barChartData.postValue(chartData)
            }
        }
    }

    private val _fetchData = MutableLiveData<List<DadosCardViewSelectedData>>()
    val fetchData:LiveData<List<DadosCardViewSelectedData>> = _fetchData

    fun fetchData (selectedData:String){
        viewModelScope.launch(dispatcher) {
            val somaTotAnimal = producaoDao.somaTotAnimais(selectedData)
            val primOrdenha = producaoDao.primOrdenha(selectedData)
            val segOrdenha = producaoDao.segOrdenha(selectedData)
            val totLitros = producaoDao.totLitros(selectedData)
            val mediaLitros = producaoDao.mediaLitros(selectedData)

            val cardViewSelectedData = DadosCardViewSelectedData(
                somaTotAnimal = somaTotAnimal,
                primOrdenha = primOrdenha,
                segOrdenha = segOrdenha,
                totLitros = totLitros,
                mediaLitros = mediaLitros
            )
            _fetchData.postValue(listOf(cardViewSelectedData))
        }
    }

    private val _barChartDataRecentes = MutableLiveData<BarData>()
    val barChartDataRecentes: LiveData<BarData> = _barChartDataRecentes

    fun generateBarChartRecentes(barChartOrdenhasRec:BarChart){
        viewModelScope.launch (dispatcher){
            val dados = producaoDao.ordenhasRecentes().map { ordenha ->
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = dateFormat.parse(ordenha.data)
                val formattedDate = SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
                OrdenhasRecentes(ordenha.primeiraOrdenha, ordenha.segOrdenha, formattedDate)
            }.sortedByDescending { it.data }

            withContext(Dispatchers.Main){
                val primeiraOrdenhaEntries = ArrayList<BarEntry>()
                val segundaOrdenhaEntries = ArrayList<BarEntry>()
                val labels = ArrayList<String>()


                dados.forEachIndexed { index, dado ->
                    val primeiraOrdenha = BarEntry(index.toFloat(), dado.primeiraOrdenha)
                    val segundaOrdenha = BarEntry(index.toFloat(), dado.segOrdenha)
                    val label = dado.data

                    primeiraOrdenhaEntries.add(primeiraOrdenha)
                    segundaOrdenhaEntries.add(segundaOrdenha)
                    labels.add(label)


                }

                val primeiraOrdenhaDataSet = BarDataSet(primeiraOrdenhaEntries, "Primeira Ordenha")
                val segundaOrdenhaDataSet = BarDataSet(segundaOrdenhaEntries, "Segunda Ordenha")
                val barData = BarData(primeiraOrdenhaDataSet, segundaOrdenhaDataSet)

                val xAxis = barChartOrdenhasRec.xAxis
                xAxis.valueFormatter = object : IndexAxisValueFormatter(labels) {
                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < labels.size) {
                            labels[index]
                        } else {
                            ""
                        }
                    }
                }
                barChartOrdenhasRec.xAxis.labelCount = labels.size
                primeiraOrdenhaDataSet.color = Color.parseColor("#FFA500")
                segundaOrdenhaDataSet.color = Color.parseColor("#3BB6FB")

                _barChartDataRecentes.postValue(barData)
            }
        }
    }


    fun readControleLeiteiro(context: Context) {
        viewModelScope.launch {
            val httpTransport = NetHttpTransport()

            val credentials = withContext(dispatcher) {
                GoogleCredential.fromStream(context.resources.openRawResource(R.raw.codechallenge_387317_38977e37a5b0))
                    .createScoped(SCOPES)
            }

            val service = Sheets.Builder(httpTransport, JSON_FACTORY, credentials)
                .build()

            val spreadsheetId = "1xJwTy6NRdC-RWDuBqEwANgtkToIMLamFfnYNbYf_gI0"
            val range = "Controle Leiteiro!A1:K"

            val response = withContext(dispatcher) {
                service.spreadsheets().values()[spreadsheetId, range]
                    .execute()
            }

            val values = response.getValues()

            val controleLeiteiroList = converterDadosParaControleLeiteiro(values)
            withContext(dispatcher) {
                addBancoDadosControle(controleLeiteiroList.value!!)
                _dadosPlanilhaControleLeiteiro.postValue(controleLeiteiroList.value)
            }
        }
    }

    fun converterDadosParaControleLeiteiro(data: List<List<Any>>): MutableLiveData<List<ControleLeiteiro>> {
        val controleLeiteiroList = MutableLiveData<List<ControleLeiteiro>>()
        val header = data[0]
        val tempList = mutableListOf<ControleLeiteiro>()

        for (i in 1 until data.size) {
            val row = data[i]
            if (row.size == header.size) {
                val controleLeiteiro = ControleLeiteiro(
                    id = row[0].toString(),
                    microchip = row[1].toString().toLong(),
                    numAnimal = row[2].toString().toInt(),
                    nome = row[3].toString(),
                    dataParto = row[4].toString(),
                    baia = row[5].toString().toInt(),
                    primOrdenha = row[6].toString().replace(",", ".").toFloat(),
                    segOrdenha = row[7].toString().replace(",", ".").toFloat(),
                    total = row[8].toString().replace(",", ".").toFloat(),
                    dataControle = row[9].toString(),
                    del = row[10].toString().toInt()
                )
                tempList.add(controleLeiteiro)
            }
        }
        controleLeiteiroList.value = tempList
        return controleLeiteiroList
    }

    fun addBancoDadosControle(controleLeiteiroList: List<ControleLeiteiro>) {
        controleLeiteiroList.forEach { controleLeiteiro ->
            controleDao.insertControle(controleLeiteiro)
        }
    }



    fun readProdDiaria (context: Context) {
        viewModelScope.launch {

            val httpTransport = NetHttpTransport()

            val credentials =
                GoogleCredential.fromStream(context.resources.openRawResource(R.raw.codechallenge_387317_38977e37a5b0))
                    .createScoped(SCOPES)

            val service = Sheets.Builder(httpTransport, JSON_FACTORY, credentials)
                .build()

            val spreadsheetId = "1xJwTy6NRdC-RWDuBqEwANgtkToIMLamFfnYNbYf_gI0"
            val range = "ProdDiaria!A1:G"

            val response = withContext(dispatcher) {
                service.spreadsheets().values()[spreadsheetId, range]
                    .execute()
            }

            val values = response.getValues()
            val producaoDiariaList= converterDadosParaProducaoDiaria(values)

            withContext(dispatcher) {
                addBancoDadosProdDiaria(producaoDiariaList.value!!)
                _dadosPlanilhaProdDiaria.postValue(producaoDiariaList.value)
            }

        }
    }

    fun converterDadosParaProducaoDiaria(data: List<List<Any>>): MutableLiveData<List<ProducaoDiaria>>{
        val producaoDiariaList = MutableLiveData<List<ProducaoDiaria>>()
        val header = data[0]
        val tempList = mutableListOf<ProducaoDiaria>()

        for (i in 1 until data.size) {
            val row = data[i]
            if (row.size == header.size){
                val producaoDiaria = ProducaoDiaria(
                    id = row[0].toString(),
                    totAnimal = row[1].toString().toInt(),
                    primeiraOrdenha = row[2].toString().replace(",", ".").toFloat(),
                    segOrdenha = row[3].toString().replace(",", ".").toFloat(),
                    totLitrosDia = row[4].toString().replace(",", ".").toFloat(),
                    media =  row[5].toString().replace(",", ".").toFloat(),
                    data = row[6].toString()
                )
                tempList.add(producaoDiaria)
            }
        }
        producaoDiariaList.value = tempList
        return producaoDiariaList
    }

    fun addBancoDadosProdDiaria(producaoDiariaList: List<ProducaoDiaria>?) {
        viewModelScope.launch(dispatcher) {
            producaoDiariaList?.forEach{producaoDiariaList ->
                    producaoDao.insertProd(producaoDiariaList)

            }
        }
    }



    companion object{
        fun create(application: Application):CodeChallangeViewModel{
            val dataBaseInstace = (application as CodeChallangeApplication).getAppDataBase()
            val controleLeiteiro = dataBaseInstace.controleDao()
            val producaoDiaria = dataBaseInstace.producaoDao()
            return CodeChallangeViewModel(controleLeiteiro,producaoDiaria)
        }
    }


}
