package com.example.fxratesapp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fxratesapp.data.AlertEntity
import com.example.fxratesapp.data.AppDatabase
import com.example.fxratesapp.databinding.ActivityMainBinding
import com.example.fxratesapp.ui.AlertsAdapter
import com.example.fxratesapp.ui.MainViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val currencies = listOf("HKD", "CNY", "USD")
    private var lastMode: Mode = Mode.LINE_7D

    enum class Mode { LINE_24H, LINE_7D, DAILY_K, MONTHLY_K }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupCharts()
        setupAlertsList()

        viewModel.latestText.observe(this) { binding.currentRateText.text = it }
        viewModel.lineEntries.observe(this) { entries ->
            val dataSet = LineDataSet(entries, "Rate")
            dataSet.setDrawCircles(false)
            dataSet.lineWidth = 2f
            binding.lineChart.data = LineData(dataSet)
            binding.lineChart.invalidate()
        }
        viewModel.candleEntries.observe(this) { entries ->
            val dataSet = CandleDataSet(entries, "K")
            dataSet.shadowWidth = 1f
            dataSet.decreasingColor = getColor(android.R.color.holo_red_dark)
            dataSet.increasingColor = getColor(android.R.color.holo_green_dark)
            dataSet.neutralColor = getColor(android.R.color.darker_gray)
            dataSet.setDrawValues(false)
            binding.candleChart.data = CandleData(dataSet)
            binding.candleChart.invalidate()
        }

        binding.btn24h.setOnClickListener { lastMode = Mode.LINE_24H; loadLine(days = 1) }
        binding.btn7d.setOnClickListener { lastMode = Mode.LINE_7D; loadLine(days = 7) }
        binding.btnDailyK.setOnClickListener { lastMode = Mode.DAILY_K; loadDailyK() }
        binding.btnMonthlyK.setOnClickListener { lastMode = Mode.MONTHLY_K; loadMonthlyK() }

        binding.btnSaveAlert.setOnClickListener { saveAlert() }

        loadLine(days = 7)
        loadLatest()
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBase.adapter = adapter
        binding.spinnerTarget.adapter = adapter
        binding.spinnerOperator.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(">=", "<="))

        binding.spinnerBase.setSelection(0)
        binding.spinnerTarget.setSelection(1)
        binding.spinnerOperator.setSelection(0)

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshCurrentMode()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.spinnerBase.onItemSelectedListener = listener
        binding.spinnerTarget.onItemSelectedListener = listener
    }

    private fun setupCharts() {
        binding.lineChart.description.isEnabled = false
        binding.lineChart.axisRight.isEnabled = false
        binding.lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        binding.candleChart.description.isEnabled = false
        binding.candleChart.axisRight.isEnabled = false
        binding.candleChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
    }

    private fun setupAlertsList() {
        val dao = AppDatabase.get(this).alertDao()
        val adapter = AlertsAdapter { alert ->
            lifecycleScope.launch(Dispatchers.IO) { dao.delete(alert) }
        }
        binding.alertsRecycler.layoutManager = LinearLayoutManager(this)
        binding.alertsRecycler.adapter = adapter

        dao.getAll().observe(this) { list -> adapter.submit(list) }
    }

    private fun refreshCurrentMode() {
        when (lastMode) {
            Mode.LINE_24H -> loadLine(days = 1)
            Mode.LINE_7D -> loadLine(days = 7)
            Mode.DAILY_K -> loadDailyK()
            Mode.MONTHLY_K -> loadMonthlyK()
        }
        loadLatest()
    }

    private fun loadLatest() {
        val base = binding.spinnerBase.selectedItem.toString()
        val target = binding.spinnerTarget.selectedItem.toString()
        if (base == target) {
            binding.currentRateText.text = "Latest: base and target must differ"
            return
        }
        viewModel.loadLatest(base, target)
    }

    private fun loadLine(days: Long) {
        binding.candleChart.visibility = View.GONE
        binding.lineChart.visibility = View.VISIBLE
        val base = binding.spinnerBase.selectedItem.toString()
        val target = binding.spinnerTarget.selectedItem.toString()
        if (base == target) return
        viewModel.loadLineSeries(base, target, days)
        loadLatest()
    }

    private fun loadDailyK() {
        binding.lineChart.visibility = View.GONE
        binding.candleChart.visibility = View.VISIBLE
        val base = binding.spinnerBase.selectedItem.toString()
        val target = binding.spinnerTarget.selectedItem.toString()
        if (base == target) return
        viewModel.loadDailyK(base, target)
        loadLatest()
    }

    private fun loadMonthlyK() {
        binding.lineChart.visibility = View.GONE
        binding.candleChart.visibility = View.VISIBLE
        val base = binding.spinnerBase.selectedItem.toString()
        val target = binding.spinnerTarget.selectedItem.toString()
        if (base == target) return
        viewModel.loadMonthlyK(base, target)
        loadLatest()
    }

    private fun saveAlert() {
        val base = binding.spinnerBase.selectedItem.toString()
        val target = binding.spinnerTarget.selectedItem.toString()
        val operator = binding.spinnerOperator.selectedItem.toString()
        val amount = binding.editAmount.text.toString().toDoubleOrNull()
        val threshold = binding.editThreshold.text.toString().toDoubleOrNull()

        if (base == target) {
            Toast.makeText(this, "Base and target must differ", Toast.LENGTH_SHORT).show()
            return
        }
        if (amount == null || threshold == null) {
            Toast.makeText(this, "Enter valid amount and threshold", Toast.LENGTH_SHORT).show()
            return
        }

        val alert = AlertEntity(
            base = base,
            target = target,
            amount = amount,
            operator = operator,
            threshold = threshold,
            enabled = true
        )

        val dao = AppDatabase.get(this).alertDao()
        lifecycleScope.launch(Dispatchers.IO) {
            dao.insert(alert)
        }

        Toast.makeText(this, "Alert saved", Toast.LENGTH_SHORT).show()
    }
}
