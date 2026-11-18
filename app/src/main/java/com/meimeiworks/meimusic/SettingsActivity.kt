package com.meimeiworks.meimusic

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("mei_id_prefs", Context.MODE_PRIVATE)

        // Создаем интерфейс прямо в коде (не нужны лишние XML-файлы)
        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        scroll.addView(layout)

        val tvStatus = TextView(this).apply {
            textSize = 16f
            setPadding(0, 0, 0, 24)
        }
        layout.addView(tvStatus)

        // Блок входа в MEI ID
        val etNick = EditText(this).apply { hint = "Введите никнейм для MEI ID" }
        val btnLogin = Button(this).apply { text = "Войти в MEI ID (Офлайн)" }
        layout.addView(etNick)
        layout.addView(btnLogin)

        // Блок ввода ключей (PRO, Лицензия 99₽, Поддержал проект, Создатель)
        val etKey = EditText(this).apply { hint = "Введите ключ (PRO / Лицензия / Донат)" }
        val btnActivate = Button(this).apply { text = "Активировать ключ" }
        layout.addView(etKey)
        layout.addView(btnActivate)

        // Выбор тем (7 базовых + Зимняя + Жидкое стекло)
        val themes = listOf(
            "Мей Классик", "Мей Небо", "Мей Закат", "Мей Мята",
            "Мей Лаванда", "Мей Графит", "Мей Персик",
            "Зимняя тема Мей Мей", "Жидкое стекло"
        )

        for (themeName in themes) {
            val btnTheme = Button(this).apply {
                text = "Тема: $themeName"
                setOnClickListener {
                    val isLoggedIn = prefs.getBoolean("is_logged_in", false)
                    val isLicensed = prefs.getBoolean("is_licensed", false)

                    if (themeName == "Зимняя тема Мей Мей" && !isLoggedIn) {
                        Toast.makeText(this@SettingsActivity, "Нужен вход в MEI ID!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (themeName == "Жидкое стекло" && !isLicensed) {
                        Toast.makeText(this@SettingsActivity, "Тема «Жидкое стекло» доступна по Лицензии (99 ₽)!", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    prefs.edit().putString("selected_theme", themeName).apply()
                    Toast.makeText(this@SettingsActivity, "Выбрана тема: $themeName", Toast.LENGTH_SHORT).show()
                }
            }
            layout.addView(btnTheme)
        }

        fun updateInfo() {
            val nick = prefs.getString("nickname", "Гость")
            val id = prefs.getString("mei_id", "Нет ID")
            val isLogged = prefs.getBoolean("is_logged_in", false)
            tvStatus.text = if (isLogged) "Профиль: $nick ($id)" else "Вы не вошли в MEI ID"
        }

        btnLogin.setOnClickListener {
            val nick = etNick.text.toString().trim()
            if (nick.isEmpty()) {
                Toast.makeText(this, "Введите ник!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newId = "MEI-${Random.nextInt(1000, 9999)}"
            prefs.edit().apply {
                putBoolean("is_logged_in", true)
                putString("nickname", nick)
                putString("mei_id", newId)
                apply()
            }
            updateInfo()
            Toast.makeText(this, "Добро пожаловать! Зимняя тема открыта.", Toast.LENGTH_SHORT).show()
        }

        btnActivate.setOnClickListener {
            val code = etKey.text.toString().trim().uppercase()
            val idDigits = (prefs.getString("mei_id", "") ?: "").filter { it.isDigit() }.toIntOrNull() ?: 0

            when {
                code == "MEIMEI-CREATOR-2026" -> {
                    prefs.edit().putBoolean("is_creator", true).putBoolean("is_licensed", true).putBoolean("is_pro", true).apply()
                    Toast.makeText(this, "Значок «Создатель» и все темы активированы!", Toast.LENGTH_LONG).show()
                }
                idDigits > 0 && code == "LIC-${(idDigits * 4) + 512}-MW" -> {
                    prefs.edit().putBoolean("is_licensed", true).putBoolean("is_pro", true).apply()
                    Toast.makeText(this, "Лицензия (99 ₽) активирована! Открыто «Жидкое стекло».", Toast.LENGTH_LONG).show()
                }
                idDigits > 0 && code == "PRO-${(idDigits * 2) + 137}-MW" -> {
                    prefs.edit().putBoolean("is_pro", true).apply()
                    Toast.makeText(this, "Подписка PRO (29 ₽) активирована!", Toast.LENGTH_LONG).show()
                }
                idDigits > 0 && code == "SUP-${(idDigits * 3) + 251}-MW" -> {
                    prefs.edit().putBoolean("is_supporter", true).apply()
                    Toast.makeText(this, "Значок «Поддержал проект» активирован!", Toast.LENGTH_LONG).show()
                }
                else -> Toast.makeText(this, "Неверный ключ", Toast.LENGTH_SHORT).show()
            }
            etKey.text.clear()
        }

        updateInfo()
        setContentView(scroll)
    }
}
