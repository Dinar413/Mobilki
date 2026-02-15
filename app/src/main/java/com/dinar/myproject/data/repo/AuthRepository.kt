package com.dinar.myproject.data.repo

import com.dinar.myproject.data.AppDatabase
import com.dinar.myproject.data.entities.User
import com.dinar.myproject.utils.PasswordHasher

class AuthRepository(private val db: AppDatabase) {

    suspend fun register(login: String, password: String, role: String): Result<Long> {
        val cleanLogin = login.trim()
        if (cleanLogin.length < 3) return Result.failure(Exception("Логин должен быть минимум 3 символа"))
        if (password.length < 4) return Result.failure(Exception("Пароль должен быть минимум 4 символа"))

        val exists = db.userDao().findByLogin(cleanLogin)
        if (exists != null) return Result.failure(Exception("Логин уже занят"))

        val id = db.userDao().insert(
            User(
                login = cleanLogin,
                passwordHash = PasswordHasher.sha256(password),
                role = role
            )
        )
        return Result.success(id)
    }

    suspend fun login(login: String, password: String): Result<User> {
        val user = db.userDao().findByLogin(login.trim())
            ?: return Result.failure(Exception("Пользователь не найден"))

        val hash = PasswordHasher.sha256(password)
        if (user.passwordHash != hash) {
            return Result.failure(Exception("Неверный пароль"))
        }
        return Result.success(user)
    }
}
