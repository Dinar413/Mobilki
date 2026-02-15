package com.dinar.myproject.data

import android.content.Context
import com.dinar.myproject.data.repo.AuthRepository
import com.dinar.myproject.data.repo.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ServiceLocator {
    lateinit var db: AppDatabase
        private set

    lateinit var session: SessionStore
        private set

    lateinit var authRepo: AuthRepository
        private set

    lateinit var financeRepo: FinanceRepository
        private set

    fun init(context: Context) {
        db = AppDatabase.get(context)
        session = SessionStore(context)
        authRepo = AuthRepository(db)
        financeRepo = FinanceRepository(db)

        CoroutineScope(Dispatchers.IO).launch {
            seedCategoriesIfNeeded(db)
        }
    }
}
