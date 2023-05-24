package com.example.codechallenge.presentation

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.codechallenge.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var barChartMelhores: BarChart
    private lateinit var barChartOrdenhasRec: BarChart
    private lateinit var selectCardData:CardView
    private lateinit var selectDataTextView:TextView

    lateinit var diaTotalAnimaisTexView:TextView
    lateinit var diaPrimOrdenhaTextView:TextView
    lateinit var diaSegOrdenhaTextView:TextView
    lateinit var diaTotLitrosTextView:TextView
    lateinit var diaMediaTextView:TextView

    private var selectedData:String = ""


    val viewModel:CodeChallangeViewModel by lazy {
        CodeChallangeViewModel.create(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        viewModel.readControleLeiteiro(this@MainActivity)
        viewModel.readProdDiaria(this@MainActivity)

        viewModel.dadosPlanilhaControleLeiteiro.observe(this@MainActivity) { controleLeiteiroList ->
            controleLeiteiroList.forEach {
                viewModel.atualizarControleLeiteiro(it)
            }

            controleLeiteiroList.forEach{
                viewModel.insertControleLeiteiro(it)
            }
        }
        viewModel.dadosPlanilhaProdDiaria.observe(this@MainActivity){ producaoDiariaList ->

            producaoDiariaList.forEach{
                viewModel.insertProdDiaria(it)
            }
            producaoDiariaList.forEach{
                viewModel.atualizarProducaoDiaria(it)
            }

        }

        //Atualização CardView ControleLeiteiro
        val geralTotLitros = findViewById<TextView>(R.id.geral_totLitros_tv)
        val geralPrimOrdenha = findViewById<TextView>(R.id.geral_mediaPrim_tv)
        val geraLSegOrdenha = findViewById<TextView>(R.id.geral_mediaSeg_tv)
        val geralMedia = findViewById<TextView>(R.id.geral_media_tv)
        val geralDel = findViewById<TextView>(R.id.geral_del_tv)

        viewModel.cardViewControleGeral()
        viewModel.cardViewControle.observe(this) { dadosCardView ->

            if (dadosCardView.isNotEmpty()) {
                val cardViewGeral = dadosCardView[0]

                geralTotLitros.text = cardViewGeral.totalLitros.toString()
                geralPrimOrdenha.text = cardViewGeral.somaPrimOrdenha.toString()
                geraLSegOrdenha.text = cardViewGeral.somaSegOrdenha.toString()
                geralMedia.text = cardViewGeral.mediaGeralOrdenhas.toString()
                geralDel.text = cardViewGeral.del.toString()
            }
        }


        //Grafico de barra Controle Geral
        barChartMelhores = findViewById(R.id.barChartMelhores)

        viewModel.generateBarChartData()
        viewModel.barChartData.observe(this) { barData ->
            barChartMelhores.xAxis.position = XAxis.XAxisPosition.BOTTOM
            barChartMelhores.xAxis.setDrawGridLines(false)
            barChartMelhores.axisRight.isEnabled = false
            barChartMelhores.setFitBars(true)
            barChartMelhores.data = barData
            barChartMelhores.description.isEnabled = false
            barChartMelhores.xAxis.labelCount = barData.entryCount
            barChartMelhores.animateY(2000)
            barChartMelhores.invalidate()
        }



        //CardView Controle p/ Dia

         diaTotalAnimaisTexView = findViewById(R.id.dia_totAnimais_tv)
         diaPrimOrdenhaTextView = findViewById(R.id.dia_primOrdenha_tv)
         diaSegOrdenhaTextView = findViewById(R.id.dia_segOrdenha_tv)
         diaTotLitrosTextView = findViewById(R.id.dia_totLitros_tv)
         diaMediaTextView = findViewById(R.id.dia_media_tv)

        //Seleção Data

         selectCardData = findViewById(R.id.selectDataCard)
         selectDataTextView = findViewById(R.id.selectData_tv)

        val calendarBox = Calendar.getInstance()
        val dateBox = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
            calendarBox.set(Calendar.YEAR, year)
            calendarBox.set(Calendar.MONTH, month)
            calendarBox.set(Calendar.DAY_OF_MONTH, day)
            updateText(calendarBox)
        }

        selectCardData.setOnClickListener {
            DatePickerDialog(
                this,
                dateBox,
                calendarBox.get(Calendar.YEAR),
                calendarBox.get(Calendar.MONTH),
                calendarBox.get(Calendar.DAY_OF_MONTH)

            ).show()

        }

        viewModel.fetchData.observe(this@MainActivity){
            if (it.isNotEmpty()){
                val cardViewSelectedData = it[0]
                diaTotalAnimaisTexView.text = cardViewSelectedData.somaTotAnimal.toString()
                diaPrimOrdenhaTextView.text = cardViewSelectedData.primOrdenha.toString()
                diaSegOrdenhaTextView.text = cardViewSelectedData.segOrdenha.toString()
                diaTotLitrosTextView.text = cardViewSelectedData.totLitros.toString()
                diaMediaTextView.text = cardViewSelectedData.mediaLitros.toString()
            }

        }

        //Grafico de barra OrdenhasRecentes

        barChartOrdenhasRec = findViewById(R.id.barChartDiaria)
        viewModel.generateBarChartRecentes(barChartOrdenhasRec)
        viewModel.barChartDataRecentes.observe(this@MainActivity){barData->
            barChartOrdenhasRec.description.isEnabled=false
            barChartOrdenhasRec.xAxis.position = XAxis.XAxisPosition.BOTTOM
            barChartOrdenhasRec.xAxis.setDrawGridLines(false)
            barChartOrdenhasRec.axisRight.isEnabled = false
            barChartOrdenhasRec.data = barData
            barChartOrdenhasRec.animateY(2000)
            barChartOrdenhasRec.invalidate()
        }

    }

    private fun updateText(calendar: Calendar) {
            val dateFormat = "dd/MM/yyyy"
            val simple = SimpleDateFormat(dateFormat, Locale.getDefault())
            selectDataTextView.text = simple.format(calendar.time)
            selectDataTextView.setTextColor(Color.BLACK)
            selectedData = simple.format(calendar.time)
            viewModel.fetchData(selectedData)
    }


}




