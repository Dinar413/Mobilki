package com.dinar.myproject.ui.main

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dinar.myproject.R
import com.dinar.myproject.data.ServiceLocator
import com.dinar.myproject.data.entities.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTxFragment : Fragment(R.layout.fragment_add_tx) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rgType = view.findViewById<RadioGroup>(R.id.rgType)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val spCategory = view.findViewById<Spinner>(R.id.spCategory)
        val etNote = view.findViewById<EditText>(R.id.etNote)
        val btnSave = view.findViewById<Button>(R.id.btnSaveTx)

        fun isIncome(): Boolean = rgType.checkedRadioButtonId == R.id.rbIncome

        suspend fun loadCategories() {
            val cats = withContext(Dispatchers.IO) {
                ServiceLocator.financeRepo.categories(isIncome())
            }
            val names = cats.map { it.name }
            spCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
            spCategory.tag = cats
        }

        rgType.setOnCheckedChangeListener { _, _ ->
            viewLifecycleOwner.lifecycleScope.launch { loadCategories() }
        }

        viewLifecycleOwner.lifecycleScope.launch { loadCategories() }

        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString().trim().replace(",", ".")
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(requireContext(), "Введите сумму", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cats = spCategory.tag as? List<Category>
            if (cats.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Нет категорий", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val categoryId = cats[spCategory.selectedItemPosition].id
            val note = etNote.text.toString()

            viewLifecycleOwner.lifecycleScope.launch {
                val userId = ServiceLocator.session.userIdFlow.first()
                withContext(Dispatchers.IO) {
                    ServiceLocator.financeRepo.addTx(
                        userId = userId,
                        amount = amount,
                        isIncome = isIncome(),
                        categoryId = categoryId,
                        note = note
                    )
                }
                Toast.makeText(requireContext(), "Сохранено", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}
