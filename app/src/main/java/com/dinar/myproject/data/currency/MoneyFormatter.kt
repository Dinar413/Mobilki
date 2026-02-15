package com.dinar.myproject.data.currency

import java.util.Locale

object MoneyFormatter {
    fun format(amount: Double): String =
        String.format(Locale.US, "%.2f", amount)
}
