package com.dinar.myproject.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dinar.myproject.R
import com.dinar.myproject.data.ServiceLocator
import com.dinar.myproject.data.currency.CurrencyConverter
import com.dinar.myproject.data.currency.MoneyFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TxListFragment : Fragment(R.layout.fragment_tx_list) {

    private enum class Sort { ALL, NEW_FIRST, OLD_FIRST }
    private enum class Filter { ALL, INCOME, EXPENSE }

    private var sort: Sort = Sort.ALL
    private var filter: Filter = Filter.ALL

    private lateinit var adapter: TxListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvTxList)
        val actSort = view.findViewById<MaterialAutoCompleteTextView>(R.id.actSort)
        val actFilter = view.findViewById<MaterialAutoCompleteTextView>(R.id.actFilter)

        adapter = TxListAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val sortOptions = listOf(
            getString(R.string.sort_all),
            getString(R.string.sort_new_first),
            getString(R.string.sort_old_first)
        )
        actSort.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, sortOptions))
        actSort.setText(sortOptions[0], false)

        actSort.setOnItemClickListener { _, _, position, _ ->
            sort = when (position) {
                0 -> Sort.ALL
                1 -> Sort.NEW_FIRST
                else -> Sort.OLD_FIRST
            }
            refresh()
        }

        val filterOptions = listOf(
            getString(R.string.filter_all),
            getString(R.string.filter_income),
            getString(R.string.filter_expense)
        )
        actFilter.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, filterOptions))
        actFilter.setText(filterOptions[0], false) // "Все"

        actFilter.setOnItemClickListener { _, _, position, _ ->
            filter = when (position) {
                0 -> Filter.ALL
                1 -> Filter.INCOME
                else -> Filter.EXPENSE
            }
            refresh()
        }

        val swipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return

                val item = adapter.getItemAt(pos)

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.delete))
                    .setMessage(getString(R.string.confirm_delete_one))
                    .setNegativeButton(getString(R.string.no)) { _, _ ->
                        adapter.notifyItemChanged(pos)
                    }
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                ServiceLocator.financeRepo.deleteTx(item.id)
                            }
                            refresh()
                        }
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(pos)
                    }
                    .show()
            }
        }
        ItemTouchHelper(swipe).attachToRecyclerView(rv)

        refresh()
    }

    private fun refresh() {
        viewLifecycleOwner.lifecycleScope.launch {
            val userId = ServiceLocator.session.userIdFlow.first()
            val currency = ServiceLocator.session.currencyFlow.first()

            val ops = withContext(Dispatchers.IO) {
                ServiceLocator.financeRepo.listTxWithCategory(userId)
            }

            // Filter
            val filtered = when (filter) {
                Filter.ALL -> ops
                Filter.INCOME -> ops.filter { it.isIncome }
                Filter.EXPENSE -> ops.filter { !it.isIncome }
            }

            // Sort
            val sorted = when (sort) {
                Sort.ALL -> filtered
                Sort.NEW_FIRST -> filtered.sortedByDescending { it.dateMillis }
                Sort.OLD_FIRST -> filtered.sortedBy { it.dateMillis }
            }

            val df = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

            val ui = sorted.map { op ->
                val date = df.format(Date(op.dateMillis))
                val note = op.note?.trim().orEmpty()
                val subtitle = if (note.isBlank()) date else "$date • $note"

                val converted = CurrencyConverter.fromRub(op.amount, currency)
                val sign = if (op.isIncome) "+ " else "- "
                val amountText = "$sign${MoneyFormatter.format(converted)} ${CurrencyConverter.symbol(currency)}"

                TxOpUi(
                    id = op.id,
                    title = op.categoryName,
                    subtitle = subtitle,
                    amountText = amountText,
                    isIncome = op.isIncome
                )
            }

            adapter.submit(ui)
        }
    }
}
