package com.example.chatlibraryproject

import android.os.Bundle
import android.widget.Button  // Добавьте этот импорт
import androidx.appcompat.app.AppCompatActivity
import com.example.chatlib.ChatLauncher

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        // Теперь вызов ChatLauncher после отображения UI
        val startChatButton: Button = findViewById(R.id.startChatButton)
        startChatButton.setOnClickListener {
            ChatLauncher.start(this)  // Запуск чата при нажатии кнопки
        }
    }
}
