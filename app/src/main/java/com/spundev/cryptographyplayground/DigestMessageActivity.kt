package com.spundev.cryptographyplayground

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.nio.charset.Charset
import java.security.MessageDigest


class DigestMessageActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val starter = Intent(context, DigestMessageActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_digest_message)

        // Input EditText
        val inputText: EditText = findViewById(R.id.inputText)

        // Digest message button
        val digestMessageButton: Button = findViewById(R.id.digestMessageButton)
        digestMessageButton.setOnClickListener {
            // Digest message and show result
            showResult(digestMessage(inputText.text.toString()))
        }
    }

    private fun digestMessage(message: String): ByteArray {
        // Convert message to byte array
        val originalMessage = message.toByteArray(Charset.defaultCharset())
        // SHA-256 of the original message
        return MessageDigest.getInstance("SHA-256").run {
            digest(originalMessage)
        }
    }

    private fun showResult(text: ByteArray) {
        // Result text to String
        val resultText = text.joinToString("") { String.format("%02x", it) }

        // Show result in TextView
        val resultTextView: TextView = findViewById(R.id.resultTextView)
        resultTextView.text = resultText
    }
}
