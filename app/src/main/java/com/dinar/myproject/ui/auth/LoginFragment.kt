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

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etLogin = view.findViewById<EditText>(R.id.etLogin)
        val etPass = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvToRegister = view.findViewById<TextView>(R.id.tvToRegister)

        tvToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        btnLogin.setOnClickListener {
            val login = etLogin.text.toString()
            val pass = etPass.text.toString()

            viewLifecycleOwner.lifecycleScope.launch {
                val res = withContext(Dispatchers.IO) {
                    ServiceLocator.authRepo.login(login, pass)
                }

                res.onSuccess { user ->
                    withContext(Dispatchers.IO) {
                        ServiceLocator.session.setUserId(user.id)
                    }
                    findNavController().navigate(R.id.action_login_to_home)

                }.onFailure { e ->
                    Toast.makeText(requireContext(), e.message ?: "Ошибка", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
