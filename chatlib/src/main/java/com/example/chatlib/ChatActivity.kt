package com.example.chatlib

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatlib.adapter.ChatAdapter
import com.example.chatlib.adapter.ChatMessage
import okhttp3.*
import okio.ByteString

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var adapter: ChatAdapter
    private lateinit var webSocket: WebSocket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.recyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        adapter = ChatAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        initWebSocket()

        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotBlank()) {
                webSocket.send(message)
                adapter.addMessage(ChatMessage(message, true)) // отправитель
                messageInput.setText("")
            }
        }
    }

    private fun initWebSocket() {
        val client = OkHttpClient()
        val request = Request.Builder().url("wss://ws.ifelse.io").build()


        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Соединение установлено")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Получено текстовое сообщение: $text")
                runOnUiThread {
                    if (text == "203") {
                        adapter.addMessage(ChatMessage("Системное сообщение: код 203", false))
                    } else {
                        adapter.addMessage(ChatMessage(text, false)) // получатель
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                val hex = bytes.hex()
                Log.d("WebSocket", "Получено бинарное сообщение: $hex")
                runOnUiThread {
                    if (hex == "cb") {
                        adapter.addMessage(ChatMessage("Системное сообщение: код 203 (байты)", false))
                    } else {
                        adapter.addMessage(ChatMessage("Получено необработанное бинарное сообщение", false))
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("WebSocket", "Соединение закрывается: $code $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Соединение закрыто: $code $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Ошибка WebSocket соединения", t)
                runOnUiThread {
                    adapter.addMessage(ChatMessage("Ошибка подключения: ${t.message}", false))
                }
            }
        })
    }


    override fun onDestroy() {
        webSocket.close(1000, null)
        super.onDestroy()
    }
}