package com.meimeiworks.meimusic

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnOpenSettings = findViewById<Button>(R.id.btnOpenSettings)
        val btnMeiMeiAction = findViewById<Button>(R.id.btnMeiMeiAction)
        val etTrackUrl = findViewById<EditText>(R.id.etTrackUrl)
        val btnDownloadTrack = findViewById<Button>(R.id.btnDownloadTrack)

        // Переход в настройки и управление MEI ID
        btnOpenSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Кнопка скачивания трека по ссылке
        btnDownloadTrack.setOnClickListener {
            val url = etTrackUrl.text.toString().trim()
            if (url.isEmpty() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
                Toast.makeText(this, "Введите корректную ссылку (http/https)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
                Toast.makeText(this, "Разрешите доступ к памяти и нажмите Скачать снова", Toast.LENGTH_LONG).show()
            } else {
                startMusicDownload(url)
                etTrackUrl.text.clear()
            }
        }

        // Главная кнопка с Мей Мей
        btnMeiMeiAction.setOnClickListener {
            Toast.makeText(this, "Мей Мей готова к воспроизведению музыки!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startMusicDownload(url: String) {
        try {
            val fileName = "Meimusic_${System.currentTimeMillis()}.mp3"
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle("Загрузка в Meimusic")
                setDescription(fileName)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, fileName)
            }

            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(this, "Загрузка началась! Проверьте шторку уведомлений.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при запуске загрузки", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateMainScreen()
    }

    private fun updateMainScreen() {
        val rootView = findViewById<View>(R.id.mainRoot)
        val prefs = getSharedPreferences("mei_id_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val selectedTheme = prefs.getString("selected_theme", "Мей Классик") ?: "Мей Классик"

        // Применяем цвет выбранной темы (включая Жидкое стекло)
        applySafeTheme(rootView, selectedTheme)

        findViewById<TextView>(R.id.tvCurrentThemeLabel).text = "Тема: $selectedTheme"
        val tvHeader = findViewById<TextView>(R.id.tvMainHeader)

        if (isLoggedIn) {
            val nick = prefs.getString("nickname", "Пользователь")
            val isCreator = prefs.getBoolean("is_creator", false)
            val isLicensed = prefs.getBoolean("is_licensed", false)
            val isPro = prefs.getBoolean("is_pro", false)
            val isSupporter = prefs.getBoolean("is_supporter", false)

            val badges = mutableListOf<String>()
            if (isCreator) badges.add("Создатель")
            if (isLicensed) badges.add("Лицензия")
            if (isPro) badges.add("PRO")
            if (isSupporter) badges.add("Поддержал проект")

            val badgeStr = if (badges.isNotEmpty()) " (${badges.joinToString(" • ")})" else ""
            tvHeader.text = "$nick$badgeStr"
        } else {
            tvHeader.text = "Meimusic • Войдите в MEI ID"
        }
    }

    private fun applySafeTheme(rootView: View, themeName: String) {
        if (themeName == "Жидкое стекло") {
            val glassGradient = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.parseColor("#E1F5FE"),
                    Color.parseColor("#B3E5FC"),
                    Color.parseColor("#F3E5F5")
                )
            )
            rootView.background = glassGradient
            return
        }

        val hex = when (themeName) {
            "Мей Небо" -> "#E3F2FD"
            "Мей Закат" -> "#FFEBEE"
            "Мей Мята" -> "#E8F5E9"
            "Мей Лаванда" -> "#F3E5F5"
            "Мей Графит" -> "#424242"
            "Мей Персик" -> "#FFF3E0"
            "Зимняя тема Мей Мей" -> "#B3E5FC"
            else -> "#F5F5F7"
        }
        rootView.setBackgroundColor(Color.parseColor(hex))
    }
}
