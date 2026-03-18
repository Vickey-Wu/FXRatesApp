package com.example.fxratesapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fxratesapp.data.AlertEntity
import com.example.fxratesapp.data.RateRepository
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

class MainViewModel : ViewModel() {
    private val repo = RateRepository()

    private val _latestText = MutableLiveData<String>("Latest: -")
    val latestText: LiveData<String> = _latestText

    private val _lineEntries = MutableLiveData<List<Entry>>(emptyList())
    val lineEntries: LiveData<List<Entry>> = _lineEntries

    private val _candleEntries = MutableLiveData<List<CandleEntry>>(emptyList())
    val candleEntries: LiveData<List<CandleEntry>> = _candleEntries

    private val fmt = DateTimeFormatter.ISO_DATE

    fun loadLatest(base: String, target: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val latest = repo.getLatest(base, listOf(target))
            val rate = latest.rates[target] ?: 0.0
            val text = String.format("Latest: %s -> %s = %.6f (date %s)", base, target, rate, latest.date)
            withContext(Dispatchers.Main) { _latestText.value = text }
        }
    }

    fun loadLineSeries(base: String, target: String, days: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val end = LocalDate.now()
            val start = end.minusDays(days)
            val ts = repo.getTimeSeries(base, listOf(target), start, end)
            val entries = ts.rates.toSortedMap().mapIndexed { idx, entry ->
                val rate = entry.value[target] ?: 0.0
                Entry(idx.toFloat(), rate.toFloat())
            }
            withContext(Dispatchers.Main) { _lineEntries.value = entries }
        }
    }

    fun loadDailyK(base: String, target: String, days: Long = 30) {
        viewModelScope.launch(Dispatchers.IO) {
            val end = LocalDate.now()
            val start = end.minusDays(days)
            val ts = repo.getTimeSeries(base, listOf(target), start, end)
            val sorted = ts.rates.toSortedMap()
            val entries = mutableListOf<CandleEntry>()
            var prevClose: Double? = null
            var idx = 0
            for ((_, map) in sorted) {
                val close = map[target] ?: continue
                val open = prevClose ?: close
                val high = max(open, close)
                val low = min(open, close)
                entries.add(CandleEntry(idx.toFloat(), high.toFloat(), low.toFloat(), open.toFloat(), close.toFloat()))
                prevClose = close
                idx++
            }
            withContext(Dispatchers.Main) { _candleEntries.value = entries }
        }
    }

    fun loadMonthlyK(base: String, target: String, months: Long = 12) {
        viewModelScope.launch(Dispatchers.IO) {
            val end = LocalDate.now()
            val start = end.minusMonths(months)
            val ts = repo.getTimeSeries(base, listOf(target), start, end)
            val byMonth = ts.rates.toSortedMap().entries.groupBy { LocalDate.parse(it.key, fmt).withDayOfMonth(1) }
            val entries = mutableListOf<CandleEntry>()
            var idx = 0
            for ((_, list) in byMonth.toSortedMap()) {
                val sorted = list.sortedBy { it.key }
                val first = sorted.first().value[target] ?: continue
                val last = sorted.last().value[target] ?: continue
                val highs = sorted.mapNotNull { it.value[target] }
                val high = highs.maxOrNull() ?: last
                val low = highs.minOrNull() ?: last
                entries.add(CandleEntry(idx.toFloat(), high.toFloat(), low.toFloat(), first.toFloat(), last.toFloat()))
                idx++
            }
            withContext(Dispatchers.Main) { _candleEntries.value = entries }
        }
    }
}
