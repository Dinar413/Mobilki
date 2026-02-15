package com.dinar.myproject.ui.main

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dinar.myproject.R
import com.dinar.myproject.data.ServiceLocator
import com.dinar.myproject.data.currency.CurrencyConverter
import com.dinar.myproject.data.currency.MoneyFormatter
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class AnalyticsFragment : Fragment(R.layout.fragment_analytics) {

    private val dfBtn = SimpleDateFormat("dd MMM", Locale("ru"))
    private val dfX = SimpleDateFormat("dd.MM", Locale.getDefault())

    private var startMillis: Long = 0L
    private var endMillis: Long = 0L

    private var incomeSelected: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnPick = view.findViewById<Button>(R.id.btnPickRange)
        val tgType = view.findViewById<MaterialButtonToggleGroup>(R.id.tgType)
        val tvAvgLabel = view.findViewById<TextView>(R.id.tvAvgLabel)
        val tvSumLabel = view.findViewById<TextView>(R.id.tvSumLabel)
        val tvAvg = view.findViewById<TextView>(R.id.tvAvgValue)
        val tvSum = view.findViewById<TextView>(R.id.tvSumValue)
        val chart = view.findViewById<LineChart>(R.id.chart)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        val green = ContextCompat.getColor(requireContext(), R.color.brand_green)
        val red = ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
        val grid = ContextCompat.getColor(requireContext(), R.color.divider_dark)
        val axisText = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)

        val now = System.currentTimeMillis()
        endMillis = now
        startMillis = now - 13L * 24L * 60L * 60L * 1000L

        fun updateBtnText() {
            btnPick.text = "${dfBtn.format(Date(startMillis))} - ${dfBtn.format(Date(endMillis))}"
        }

        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.legend.textColor = axisText
        chart.setNoDataText("")
        chart.setExtraOffsets(8f, 8f, 12f, 10f)


        chart.axisRight.isEnabled = false

        chart.axisLeft.apply {
            textColor = axisText
            setDrawGridLines(true)
            gridColor = grid
            axisMinimum = 0f
        }

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            textColor = axisText
            granularity = 1f
            setAvoidFirstLastClipping(true)
        }

        fun setupChart(labels: List<String>, incomeValues: List<Double>, expenseValues: List<Double>, currency: String) {
            val incomeEntries = incomeValues.mapIndexed { i, v -> Entry(i.toFloat(), v.toFloat()) }
            val expenseEntries = expenseValues.mapIndexed { i, v -> Entry(i.toFloat(), v.toFloat()) }

            val dsIncome = LineDataSet(incomeEntries, getString(R.string.income)).apply {
                setDrawValues(false)
                setDrawCircles(true)
                circleRadius = 3.2f
                lineWidth = 2.2f
                color = green
                setCircleColor(green)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            val dsExpense = LineDataSet(expenseEntries, getString(R.string.expense)).apply {
                setDrawValues(false)
                setDrawCircles(true)
                circleRadius = 3.2f
                lineWidth = 2.2f
                color = red
                setCircleColor(red)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dsIncome, dsExpense)

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val i = value.toInt()
                    return if (i in labels.indices) labels[i] else ""
                }
            }

            chart.axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return MoneyFormatter.format(value.toDouble())
                }
            }

            chart.invalidate()

            val allZero = incomeValues.all { it == 0.0 } && expenseValues.all { it == 0.0 }
            tvEmpty.visibility = if (allZero) View.VISIBLE else View.GONE
            chart.visibility = if (allZero) View.INVISIBLE else View.VISIBLE
        }

        tgType.check(R.id.btnExpense)
        incomeSelected = false

        updateBtnText()

        viewLifecycleOwner.lifecycleScope.launch {
            val userId = ServiceLocator.session.userIdFlow.first()

            suspend fun refresh() {
                val currency = ServiceLocator.session.currencyFlow.first()

                val ops = withContext(Dispatchers.IO) {
                    ServiceLocator.financeRepo.listTxWithCategory(userId)
                }.filter { it.dateMillis in startMillis..endMillis }

                val zone = ZoneId.systemDefault()
                val byDayIncome = ops.filter { it.isIncome }.groupBy {
                    Instant.ofEpochMilli(it.dateMillis).atZone(zone).toLocalDate()
                }.mapValues { (_, list) -> list.sumOf { it.amount } }

                val byDayExpense = ops.filter { !it.isIncome }.groupBy {
                    Instant.ofEpochMilli(it.dateMillis).atZone(zone).toLocalDate()
                }.mapValues { (_, list) -> list.sumOf { it.amount } }

                val startDay = Instant.ofEpochMilli(startMillis).atZone(zone).toLocalDate()
                val endDay = Instant.ofEpochMilli(endMillis).atZone(zone).toLocalDate()

                val days = generateSequence(startDay) { d ->
                    if (d.isBefore(endDay)) d.plusDays(1) else null
                }.toList()

                val labels = days.map { d ->
                    val date = Date(d.atStartOfDay(zone).toInstant().toEpochMilli())
                    dfX.format(date)
                }

                val incomeRub = days.map { d -> byDayIncome[d] ?: 0.0 }
                val expenseRub = days.map { d -> byDayExpense[d] ?: 0.0 }

                val chosenRub = if (incomeSelected) incomeRub else expenseRub
                val sumRub = chosenRub.sum()
                val avgRub = if (chosenRub.isEmpty()) 0.0 else sumRub / chosenRub.size

                val sum = CurrencyConverter.fromRub(sumRub, currency)
                val avg = CurrencyConverter.fromRub(avgRub, currency)

                tvSum.text = "${MoneyFormatter.format(sum)} ${CurrencyConverter.symbol(currency)}"
                tvAvg.text = "${MoneyFormatter.format(avg)} ${CurrencyConverter.symbol(currency)}"

                tvAvgLabel.text = if (incomeSelected)
                    getString(R.string.avg_per_day)
                else
                    getString(R.string.avg_per_day)

                tvSumLabel.text = getString(R.string.total_for_period)

                val incomeValues = incomeRub.map { CurrencyConverter.fromRub(it, currency) }
                val expenseValues = expenseRub.map { CurrencyConverter.fromRub(it, currency) }

                setupChart(labels, incomeValues, expenseValues, currency)
            }

            refresh()

            btnPick.setOnClickListener {
                val picker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText(getString(R.string.pick_period))
                    .build()

                picker.addOnPositiveButtonClickListener { range ->
                    val start = range.first ?: return@addOnPositiveButtonClickListener
                    val end = range.second ?: return@addOnPositiveButtonClickListener
                    startMillis = start
                    endMillis = end + 24L * 60L * 60L * 1000L - 1L
                    updateBtnText()
                    viewLifecycleOwner.lifecycleScope.launch { refresh() }
                }

                picker.show(parentFragmentManager, "range")
            }

            tgType.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (!isChecked) return@addOnButtonCheckedListener
                incomeSelected = checkedId == R.id.btnIncome
                viewLifecycleOwner.lifecycleScope.launch { refresh() }
            }
        }
    }
}
