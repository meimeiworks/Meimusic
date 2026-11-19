package com.meimeiworks.meimusic

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // Современный обработчик запроса разрешений (для Android 9–14)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val audioGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.READ_MEDIA_AUDIO] == true
            } else {
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
            }

            if (audioGranted) {
                // Разрешение на музыку получено — можно загружать треки
                loadMusicLibrary()
            } else {
                Toast.makeText(
                    this,
                    "Для работы плеера нужен доступ к аудиофайлам",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Инициализируем скрытый локальный MeiID (нигде не выводится на экран)
        val localMeiId = getOrCreateHiddenMeiId()

        // 2. Проверяем и запрашиваем права на музыку под нужную версию Android (9–14)
        checkAndRequestPermissions()

        // 3. Здесь подключаются твои кнопки плеера, настройки и переключение 7 тем Мэй Мэй
        setupPlayerUI()
    }

    /**
     * Скрытый локальный ID. Хранится только в закрытой памяти приложения (MODE_PRIVATE)
     * и нигде не отображается в интерфейсе.
     */
    private fun getOrCreateHiddenMeiId(): Long {
        val prefs = getSharedPreferences("mei_internal_secure_prefs", Context.MODE_PRIVATE)
        var savedId = prefs.getLong("hidden_local_mei_id", -1L)

        if (savedId == -1L) {
            // Устанавливаем твой личный закрытый ID при первом запуске
            savedId = 5927346L
            prefs.edit().putLong("hidden_local_mei_id", savedId).apply()
        }
        return savedId
    }

    /**
     * Запрос разрешений строго под диапазон Android 9 – 14
     */
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 и 14 (API 33–34)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 9 – 12 (API 28–32)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            loadMusicLibrary()
        }
    }

    private fun loadMusicLibrary() {
        // TODO: Сюда перенеси свой код сканирования и загрузки треков из предыдущей версии
    }

    private fun setupPlayerUI() {
        // TODO: Сюда перенеси привязку кнопок (Play/Pause, Next, Prev, Настройки и 7 тем Мэй Мэй)
    }
}
