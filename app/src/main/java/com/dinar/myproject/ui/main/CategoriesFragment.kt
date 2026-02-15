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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesFragment : Fragment(R.layout.fragment_categories) {

    private var currentCats: List<Category> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rgType = view.findViewById<RadioGroup>(R.id.rgCatType)
        val etName = view.findViewById<EditText>(R.id.etCatName)
        val btnAdd = view.findViewById<Button>(R.id.btnAddCat)
        val lvCats = view.findViewById<ListView>(R.id.lvCats)

        fun isIncome(): Boolean = rgType.checkedRadioButtonId == R.id.rbCatIncome

        fun renderList() {
            val names = currentCats.map { " ${it.name}" }
            lvCats.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
        }

        suspend fun refresh() {
            currentCats = withContext(Dispatchers.IO) {
                ServiceLocator.financeRepo.categories(isIncome())
            }
            renderList()
        }

        viewLifecycleOwner.lifecycleScope.launch { refresh() }

        rgType.setOnCheckedChangeListener { _, _ ->
            viewLifecycleOwner.lifecycleScope.launch { refresh() }
        }

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Введите название", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    ServiceLocator.financeRepo.addCategory(name, isIncome())
                }
                etName.setText("")
                Toast.makeText(requireContext(), "Категория добавлена", Toast.LENGTH_SHORT).show()
                refresh()
            }
        }

        lvCats.setOnItemClickListener { _, _, position, _ ->
            if (position !in currentCats.indices) return@setOnItemClickListener
            val cat = currentCats[position]

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Удалить категорию?")
                .setMessage("Удалить: «${cat.name}» ?")
                .setPositiveButton("Удалить") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        val ok = withContext(Dispatchers.IO) {
                            runCatching {
                                ServiceLocator.financeRepo.deleteCategory(cat.id)
                            }.isSuccess
                        }

                        if (ok) {
                            Toast.makeText(requireContext(), "Удалено", Toast.LENGTH_SHORT).show()
                            refresh()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Не удалось удалить (возможно, категория уже используется в операциях)",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }
}
