package com.meimeiworks.meimusic

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
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
import java.io.File

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var trackList: List<File> = emptyList()
    private var currentTrackIndex = 0
    private var isPlaying = false

    private lateinit var tvNowPlaying: TextView
    private lateinit var btnPlayPause: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Защита от молчаливого вылета при запуске
        try {
            setContentView(R.layout.activity_main)

            val btnOpenSettings = findViewById<Button>(R.id.btnOpenSettings)
            val etTrackUrl = findViewById<EditText>(R.id.etTrackUrl)
            val btnDownloadTrack = findViewById<Button>(R.id.btnDownloadTrack)

            btnPlayPause = findViewById(R.id.btnMeiMeiAction)
            tvNowPlaying = findViewById(R.id.tvCurrentThemeLabel)

            // Открытие экрана настроек, тем и MEI ID
            btnOpenSettings.setOnClickListener {
                startActivity(Intent(this, SettingsActivity::class.java))
            }

            // Скачивание MP3 по прямой ссылке
            btnDownloadTrack.setOnClickListener {
                val url = etTrackUrl.text.toString().trim()
                if (url.isEmpty() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
                    Toast.makeText(this, "Введите ссылку (http/https)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (checkStoragePermission()) {
                    startMusicDownload(url)
                    etTrackUrl.text.clear()
                }
            }

            // Кнопка Пауза / Воспроизведение (долгое нажатие переключает трек)
            btnPlayPause.setOnClickListener {
                if (isPlaying) {
                    pauseTrack()
                } else if (mediaPlayer != null) {
                    resumeTrack()
                } else {
                    loadAndPlayTracks(0)
                }
            }

            btnPlayPause.setOnLongClickListener {
                if (trackList.isNotEmpty()) {
                    val nextIndex = (currentTrackIndex + 1) % trackList.size
                    loadAndPlayTracks(nextIndex)
                }
                true
            }
        } catch (e: Exception) {
            val errorView = TextView(this).apply {
                text = "Ошибка запуска интерфейса:\n${e.message}"
                setPadding(40, 40, 40, 40)
            }
            setContentView(errorView)
        }
    }

    private fun checkStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            )
            Toast.makeText(this, "Разрешите доступ к памяти и нажмите еще раз", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun loadAndPlayTracks(index: Int) {
        if (!checkStoragePermission()) return

        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val files = musicDir?.listFiles { file -> file.extension.equals("mp3", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

        if (files.isEmpty()) {
            Toast.makeText(this, "В папке Music нет MP3. Скачайте трек по ссылке выше!", Toast.LENGTH_LONG).show()
            return
        }

        trackList = files
        currentTrackIndex = index.coerceIn(0, trackList.size - 1)
        val track = trackList[currentTrackIndex]

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(track.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    val next = (currentTrackIndex + 1) % trackList.size
                    loadAndPlayTracks(next)
                }
            }
            isPlaying = true
            btnPlayPause.text = "⏸ Пауза (Удерж. — След.)"
            tvNowPlaying.text = "Играет (${currentTrackIndex + 1}/${trackList.size}): ${track.nameWithoutExtension}"
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка чтения файла: ${track.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pauseTrack() {
        mediaPlayer?.pause()
        isPlaying = false
        btnPlayPause.text = "▶ Продолжить"
    }

    private fun resumeTrack() {
        mediaPlayer?.start()
        isPlaying = true
        btnPlayPause.text = "⏸ Пауза (Удерж. — След.)"
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
        try {
            updateUIAndTheme()
        } catch (e: Exception) {
            // Игнорируем ошибку обновления UI, если экран еще не загружен
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun updateUIAndTheme() {
        val rootView = findViewById<View>(R.id.mainRoot) ?: return
        val prefs = getSharedPreferences("mei_id_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val selectedTheme = prefs.getString("selected_theme", "Мей Классик") ?: "Мей Классик"

        applyThemeColor(rootView, selectedTheme)

        if (!isPlaying && mediaPlayer == null && ::tvNowPlaying.isInitialized) {
            tvNowPlaying.text = "Тема: $selectedTheme"
        }

        val tvHeader = findViewById<TextView>(R.id.tvMainHeader) ?: return
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

    private fun applyThemeColor(rootView: View, themeName: String) {
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
