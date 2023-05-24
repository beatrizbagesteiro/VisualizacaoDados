package com.example.codechallenge

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.codechallenge.data.ControleDao
import com.example.codechallenge.data.ControleLeiteiro
import com.example.codechallenge.data.OrdenhasRecentes
import com.example.codechallenge.data.ProducaoDao
import com.example.codechallenge.data.ProducaoDiaria
import com.example.codechallenge.data.Top10Ordenhas
import com.example.codechallenge.presentation.CodeChallangeViewModel
import com.github.mikephil.charting.charts.BarChart
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)

class CodeChallangeViewModelTest {

    private val producaoDao:ProducaoDao = mockk()

    private val controleDao: ControleDao = mockk()

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private val underTest:CodeChallangeViewModel by lazy {
        CodeChallangeViewModel(controleDao,producaoDao)
    }

    @Test
    fun atualizarControleLeiteiro(){
        val controleLeiteiro = ControleLeiteiro(
            id = "1a2b3c4d",
            microchip = 1234567890,
            numAnimal = 10,
            nome = "Animal1",
            dataParto = "01/01/2022",
            baia = 5,
            primOrdenha = 12.5f,
            segOrdenha = 10.8f,
            total = 23.3f,
            dataControle = "01/05/2022",
            del = 0
        )

        every { controleDao.upsert(controleLeiteiro) } just Runs

        underTest.upsertControle(controleLeiteiro)

        verify { controleDao.upsert(controleLeiteiro) }
    }

    @Test
    fun atualizarProducaoDiaria(){
        val producaoDiaria = ProducaoDiaria(
            id = "1a2b3c4d",
            totAnimal = 10,
            primeiraOrdenha = 12.5f,
            segOrdenha = 10.8f,
            totLitrosDia = 23.3f,
            media = 15.5f,
            data = "01/05/2022"
        )
        every { producaoDao.upsert(producaoDiaria) } just Runs

        underTest.upsertProdDiaria(producaoDiaria)

        verify { producaoDao.upsert(producaoDiaria) }
    }

    @Test
    fun cardViewControleGeralTest() {
        every { controleDao.getTotalLitros() } returns 10.0f
        every { controleDao.getSomaPrimOrdenha() } returns 5.0f
        every { controleDao.getSomaSegOrdenha() } returns 7.0f
        every { controleDao.getMediaGeralOrdenhas() } returns 6.0f
        every { controleDao.getDel() } returns 3

        underTest.cardViewControleGeral()

        verify { controleDao.getTotalLitros() }
        verify { controleDao.getSomaPrimOrdenha() }
        verify { controleDao.getSomaSegOrdenha() }
        verify { controleDao.getMediaGeralOrdenhas() }
        verify { controleDao.getDel() }


        underTest.cardViewControle.observeForever { dadosCardViewGeral ->
            assert(dadosCardViewGeral != null)
            assert(dadosCardViewGeral!!.size == 1)
            assert(dadosCardViewGeral[0].totalLitros == 10.0f)
            assert(dadosCardViewGeral[0].somaPrimOrdenha == 5.0f)
            assert(dadosCardViewGeral[0].somaSegOrdenha == 7.0f)
            assert(dadosCardViewGeral[0].mediaGeralOrdenhas == 6.0f)
            assert(dadosCardViewGeral[0].del == 3)
        }
    }

    @Test
    fun fetchData(){
        val selectedData = "24/05/2023"
        every { producaoDao.somaTotAnimais(selectedData) } returns 10
        every { producaoDao.primOrdenha(selectedData) } returns 14.0f
        every { producaoDao.segOrdenha(selectedData) } returns 10.0f
        every { producaoDao.totLitros(selectedData) } returns 24.0f
        every { producaoDao.mediaLitros(selectedData) } returns 12.0f

        underTest.fetchData(selectedData)

        verify { producaoDao.somaTotAnimais(selectedData) }
        verify { producaoDao.primOrdenha(selectedData) }
        verify { producaoDao.segOrdenha(selectedData) }
        verify { producaoDao.totLitros(selectedData) }
        verify { producaoDao.mediaLitros(selectedData) }

        underTest.fetchData.observeForever{ dadosCardViewData ->
            assert(dadosCardViewData != null)
            assert(dadosCardViewData!!.size == 1)
            assert(dadosCardViewData[0].somaTotAnimal == 10)
            assert(dadosCardViewData[0].primOrdenha == 14.0f)
            assert(dadosCardViewData[0].segOrdenha == 10.0f)
            assert(dadosCardViewData[0].totLitros == 24.0f)
            assert(dadosCardViewData[0].mediaLitros == 12.0f)
        }
    }

    @Test
    fun generateBarChart(){
                val top10Ordenhas = listOf(
           Top10Ordenhas(10, 12.0f),
           Top10Ordenhas(5,6.0f),
           Top10Ordenhas(12, 15.0f)
        )

        every { controleDao.top10() } returns top10Ordenhas

        val resultado = controleDao.top10()

        assertEquals(top10Ordenhas,resultado)


    }

    @Test
    fun generateBarChartRecentes(){
        val ondenhasRecentes = listOf<OrdenhasRecentes>(
            OrdenhasRecentes(primeiraOrdenha = 10.0f, segOrdenha = 12.0f, data = "24/05/2023"),
            OrdenhasRecentes(primeiraOrdenha = 11.0f, segOrdenha = 13.0f, data = "25/05/2023")
        )

        every { producaoDao.ordenhasRecentes() } returns ondenhasRecentes

        val resultado = producaoDao.ordenhasRecentes()

        assertEquals(ondenhasRecentes,resultado)

    }




}






