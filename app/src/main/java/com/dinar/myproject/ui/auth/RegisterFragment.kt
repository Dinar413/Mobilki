package com.dinar.myproject.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dinar.myproject.R
import com.dinar.myproject.data.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etLogin = view.findViewById<EditText>(R.id.etRegLogin)
        val etPass = view.findViewById<EditText>(R.id.etRegPassword)
        val spRole = view.findViewById<Spinner>(R.id.spRole)
        val btnReg = view.findViewById<Button>(R.id.btnRegister)

        val roles = listOf("ADMIN", "MANAGER")
        spRole.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles)

        btnReg.setOnClickListener {
            val login = etLogin.text.toString()
            val pass = etPass.text.toString()
            val role = roles[spRole.selectedItemPosition]

            viewLifecycleOwner.lifecycleScope.launch {
                val res = withContext(Dispatchers.IO) {
                    ServiceLocator.authRepo.register(login, pass, role)
                }

                res.onSuccess { userId ->
                    withContext(Dispatchers.IO) {
                        ServiceLocator.session.setUserId(userId)
                    }
                    findNavController().navigate(R.id.action_register_to_home)

                }.onFailure { e ->
                    Toast.makeText(requireContext(), e.message ?: "Ошибка", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
