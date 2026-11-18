package com.meimeiworks.meimusic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnOpenSettings = findViewById<Button>(R.id.btnOpenSettings)
        val btnMeiMeiAction = findViewById<Button>(R.id.btnMeiMeiAction)

        // Переход в настройки и управление MEI ID
        btnOpenSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Главная кнопка с Мей Мей
        btnMeiMeiAction.setOnClickListener {
            Toast.makeText(this, "Мей Мей готова к воспроизведению музыки!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateMainScreen()
    }

    private fun updateMainScreen() {
        // 1. Красим фон главного экрана в выбранную тему
        val rootView = findViewById<View>(R.id.mainRoot)
        ThemeManager.applyThemeToView(this, rootView)

        // 2. Обновляем шапку профиля и название текущей темы
        val prefs = getSharedPreferences("mei_id_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val selectedTheme = prefs.getString("selected_theme", "Мей Классик")

        findViewById<TextView>(R.id.tvCurrentThemeLabel).text = "Тема: $selectedTheme"
        val tvHeader = findViewById<TextView>(R.id.tvMainHeader)

        if (isLoggedIn) {
            val nick = prefs.getString("nickname", "Пользователь")
            val isCreator = prefs.getBoolean("is_creator", false)
            val isPro = prefs.getBoolean("is_pro", false)
            val isSupporter = prefs.getBoolean("is_supporter", false)

            val badges = mutableListOf<String>()
            if (isCreator) badges.add("Создатель")
            if (isPro) badges.add("PRO")
            if (isSupporter) badges.add("Поддержал проект")

            val badgeStr = if (badges.isNotEmpty()) " (${badges.joinToString(" • ")})" else ""
            tvHeader.text = "$nick$badgeStr"
        } else {
            tvHeader.text = "Meimusic • Войдите в MEI ID"
        }
    }
}
