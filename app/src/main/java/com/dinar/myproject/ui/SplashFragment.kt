package com.dinar.myproject.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dinar.myproject.R
import com.dinar.myproject.data.ServiceLocator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashFragment : Fragment(R.layout.fragment_splash) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val userId = ServiceLocator.session.userIdFlow.first()
            if (userId > 0L) findNavController().navigate(R.id.action_splash_to_home)
            else findNavController().navigate(R.id.action_splash_to_login)

        }
    }
}
