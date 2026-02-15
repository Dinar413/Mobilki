package com.dinar.myproject.ui.main

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dinar.myproject.R
import com.dinar.myproject.data.ServiceLocator
import com.dinar.myproject.data.currency.CurrencyConverter
import com.dinar.myproject.data.currency.MoneyFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: HomeOpsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvBalance = view.findViewById<TextView>(R.id.tvHomeBalance)
        val tvIncome = view.findViewById<TextView>(R.id.tvHomeIncome)
        val tvExpense = view.findViewById<TextView>(R.id.tvHomeExpense)

        val btnAdd = view.findViewById<Button>(R.id.btnHomeAdd)
        val btnAll = view.findViewById<Button>(R.id.btnHomeAllOps)
        val btnCats = view.findViewById<Button>(R.id.btnHomeCategories)

        val rv = view.findViewById<RecyclerView>(R.id.rvHomeOps)

        adapter = HomeOpsAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnAdd.setOnClickListener { findNavController().navigate(R.id.addTxFragment) }
        btnAll.setOnClickListener { findNavController().navigate(R.id.txListFragment) }
        btnCats.setOnClickListener { findNavController().navigate(R.id.categoriesFragment) }

        viewLifecycleOwner.lifecycleScope.launch {
            val userId = ServiceLocator.session.userIdFlow.first()
            val currency = ServiceLocator.session.currencyFlow.first()

            val role = withContext(Dispatchers.IO) { ServiceLocator.financeRepo.getUserRole(userId) }
            btnCats.isVisible = role == "ADMIN"

            val (balRub, incRub, expRub) = withContext(Dispatchers.IO) {
                ServiceLocator.financeRepo.totals(userId)
            }

            val bal = CurrencyConverter.fromRub(balRub, currency)
            val inc = CurrencyConverter.fromRub(incRub, currency)
            val exp = CurrencyConverter.fromRub(expRub, currency)

            tvBalance.text = "Баланс: ${MoneyFormatter.format(bal)} ${CurrencyConverter.symbol(currency)}"
            tvIncome.text = "Доходы: ${MoneyFormatter.format(inc)} ${CurrencyConverter.symbol(currency)}"
            tvExpense.text = "Расходы: ${MoneyFormatter.format(exp)} ${CurrencyConverter.symbol(currency)}"

            val lastOps = withContext(Dispatchers.IO) {
                ServiceLocator.financeRepo.listTxWithCategory(userId)
            }.take(10)

            val df = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

            val uiList = lastOps.map { op ->
                val date = df.format(Date(op.dateMillis))
                val note = op.note?.trim().orEmpty()
                val subtitle = if (note.isBlank()) date else "$date • $note"

                val converted = CurrencyConverter.fromRub(op.amount, currency)
                val sign = if (op.isIncome) "+ " else "- "
                val amountText = "$sign${MoneyFormatter.format(converted)} ${CurrencyConverter.symbol(currency)}"

                HomeOpUi(
                    title = op.categoryName,
                    subtitle = subtitle,
                    amountText = amountText,
                    isIncome = op.isIncome
                )
            }

            adapter.submit(uiList)
        }
    }
}
