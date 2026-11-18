package com.meimeiworks.meimusic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val tvAccountStatus = findViewById<TextView>(R.id.tvAccountStatus)
        val btnOpenMeiId = findViewById<Button>(R.id.btnOpenMeiId)
        val btnOpenSupport = findViewById<Button>(R.id.btnOpenSupport)

        val btnWinterTheme = findViewById<Button>(R.id.btnWinterTheme)
        val btnProColors = findViewById<Button>(R.id.btnProColors)
        val btnCustomTheme = findViewById<Button>(R.id.btnCustomTheme)
        val btnCustomCovers = findViewById<Button>(R.id.btnCustomCovers)

        // Переходы в MEI ID и Поддержку проекта
        btnOpenMeiId.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnOpenSupport.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        // Настройка 7 базовых тем с Мей Мей (доступны всем бесплатно)
        val baseThemeButtons = listOf(
            R.id.btnTheme1 to "Мей Классик",
            R.id.btnTheme2 to "Мей Небо",
            R.id.btnTheme3 to "Мей Закат",
            R.id.btnTheme4 to "Мей Мята",
            R.id.btnTheme5 to "Мей Лаванда",
            R.id.btnTheme6 to "Мей Графит",
            R.id.btnTheme7 to "Мей Персик"
        )

        for ((btnId, themeName) in baseThemeButtons) {
            findViewById<Button>(btnId)?.setOnClickListener {
                applyTheme(themeName)
            }
        }

        // Зимняя тема (разблокируется после входа в MEI ID)
        btnWinterTheme.setOnClickListener {
            val prefs = getSharedPreferences("mei_id_prefs", Context.MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("is_logged_in", false)

            if (isLoggedIn) {
                applyTheme("Зимняя тема Мей Мей")
            } else {
                Toast.makeText(this, "Войдите в MEI ID, чтобы открыть Зимнюю тему!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        // PRO-функции (открываются по подписке за 29 руб/мес)
        btnProColors.setOnClickListener { checkProAndRun("Новые PRO-цвета с Мей Мей") }
        btnCustomTheme.setOnClickListener { checkProAndRun("Настраиваемая тема") }
        btnCustomCovers.setOnClickListener { checkProAndRun("Выбор своих обложек") }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val prefs = getSharedPreferences("mei_id_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val isPro = prefs.getBoolean("is_pro", false)
        val isSupporter = prefs.getBoolean("is_supporter", false)

        val tvAccountStatus = findViewById<TextView>(R.id.tvAccountStatus)
        val btnWinterTheme = findViewById<Button>(R.id.btnWinterTheme)

        if (isLoggedIn) {
            val nick = prefs.getString("nickname", "Пользователь")
            val id = prefs.getString("mei_id", "")
            val badges = mutableListOf<String>()
            if (isPro) badges.add("PRO")
            if (isSupporter) badges.add("Поддержал проект")

            val statusLine = if (badges.isNotEmpty()) " [${badges.joinToString(", ")}]" else ""
            tvAccountStatus.text = "MEI ID: $nick ($id)$statusLine"
            btnWinterTheme.text = "Зимняя тема Мей Мей (Открыта)"
        } else {
            tvAccountStatus.text = "MEI ID: Не выполнен вход"
            btnWinterTheme.text = "Зимняя тема Мей Мей (Нужен MEI ID)"
        }
    }

    private fun checkProAndRun(featureName: String) {
        val prefs = getSharedPreferences("mei_id_prefs", Context.MODE_PRIVATE)
        val isPro = prefs.getBoolean("is_pro", false)

        if (isPro) {
            Toast.makeText(this, "Открыто: $featureName", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Функция «$featureName» доступна в Meimusic PRO (29 ₽/мес)", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, SupportActivity::class.java))
        }
    }

    private fun applyTheme(themeName: String) {
        val prefs = getSharedPreferences("mei_id_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_theme", themeName).apply()
        Toast.makeText(this, "Применена тема: $themeName", Toast.LENGTH_SHORT).show()
    }
}
