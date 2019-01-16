package com.spundev.cryptographyplayground

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

class MacActivity : AppCompatActivity() {

    private lateinit var secretKey: SecretKey

    companion object {
        // save state keys
        private const val STATE_SECRET_KEY = "secretKey"

        fun start(context: Context) {
            val starter = Intent(context, MacActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mac)

        // Input EditText
        val inputText: EditText = findViewById(R.id.inputText)
        // Result TextView
        val resultTextView: TextView = findViewById(R.id.resultTextView)

        // Retrieve previous secret key and iv
        secretKey = if (savedInstanceState != null) {
            savedInstanceState.getSerializable(STATE_SECRET_KEY) as SecretKey
        } else {
            generateKey()
        }

        // Click event
        val hmacButton: Button = findViewById(R.id.mac_button)
        hmacButton.setOnClickListener {
            // Retrieve message from the EditText view
            val message = inputText.text.toString()
            // hmac
            val result = hmac(secretKey, message).joinToString("") { value -> String.format("%02x", value) }
            // Show result
            resultTextView.text = result
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save the secret key and the iv
        outState.putSerializable(STATE_SECRET_KEY, secretKey)
        super.onSaveInstanceState(outState)
    }

    private fun generateKey(): SecretKey {
        // Generate key
        val keygen = KeyGenerator.getInstance("HmacSHA256")
        return keygen.generateKey()
    }

    private fun hmac(secretKey: SecretKey, message: String): ByteArray {
        return Mac.getInstance("HmacSHA256").run {
            init(secretKey)
            doFinal(message.toByteArray())
        }
    }
}
