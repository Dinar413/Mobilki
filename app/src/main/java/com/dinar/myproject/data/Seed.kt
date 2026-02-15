package com.dinar.myproject.data

import com.dinar.myproject.data.entities.Category

suspend fun seedCategoriesIfNeeded(db: AppDatabase) {
    if (db.categoryDao().count() > 0) return

    db.categoryDao().insert(Category(name = "Аренда", isIncome = false))
    db.categoryDao().insert(Category(name = "Закупка товара", isIncome = false))
    db.categoryDao().insert(Category(name = "Зарплата", isIncome = false))
    db.categoryDao().insert(Category(name = "Транспорт", isIncome = false))
    db.categoryDao().insert(Category(name = "Реклама", isIncome = false))

    db.categoryDao().insert(Category(name = "Продажи", isIncome = true))
    db.categoryDao().insert(Category(name = "Услуги", isIncome = true))
    db.categoryDao().insert(Category(name = "Инвестиции", isIncome = true))
}
