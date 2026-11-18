package com.meimeiworks.meimusic

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var themeIndex = 0

    // 7 цветовых вариаций: [Цвет фона, Цвет Мей Мей и кнопок, Название темы]
    private val themes = listOf(
        Triple("#1A1A2E", "#E94560", "Тема 1: Ночной барс"),
        Triple("#0F3460", "#00D2FC", "Тема 2: Ледяной пик"),
        Triple("#222831", "#FFD369", "Тема 3: Золотой рассвет"),
        Triple("#1B262C", "#3282B8", "Тема 4: Глубокий океан"),
        Triple("#2D132C", "#EE4540", "Тема 5: Рубиновый закат"),
        Triple("#1E2022", "#A3F7BF", "Тема 6: Мятный бриз"),
        Triple("#2C2C54", "#FF793F", "Тема 7: Огненный ирбис")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val titleText = TextView(this).apply {
            text = "MEIMUSIC"
            textSize = 32f
            gravity = Gravity.CENTER
        }

        val versionText = TextView(this).apply {
            text = "Версия Alpha 0.0.1 (Android 6–9)"
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 40)
        }

        val mascotBox = TextView(this).apply {
            text = "Мей Мей готова к музыке!"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(32, 48, 32, 48)
        }

        val themeLabel = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 24)
        }

        val switchThemeBtn = Button(this).apply {
            text = "Сменить тему Мей Мей (1/7)"
            textSize = 16f
        }

        fun applyTheme() {
            val (bgHex, meiHex, name) = themes[themeIndex]
            val bgColor = Color.parseColor(bgHex)
            val meiColor = Color.parseColor(meiHex)

            rootLayout.setBackgroundColor(bgColor)
            titleText.setTextColor(meiColor)
            versionText.setTextColor(Color.LTGRAY)
            mascotBox.setBackgroundColor(meiColor)
            mascotBox.setTextColor(bgColor)
            themeLabel.text = name
            themeLabel.setTextColor(Color.WHITE)
            switchThemeBtn.setBackgroundColor(meiColor)
            switchThemeBtn.setTextColor(bgColor)
            switchThemeBtn.text = "Сменить тему Мей Мей (${themeIndex + 1}/7)"
        }

        switchThemeBtn.setOnClickListener {
            themeIndex = (themeIndex + 1) % themes.size
            applyTheme()
        }

        applyTheme()

        rootLayout.addView(titleText)
        rootLayout.addView(versionText)
        rootLayout.addView(mascotBox)
        rootLayout.addView(themeLabel)
        rootLayout.addView(switchThemeBtn)

        setContentView(rootLayout)
    }
}
