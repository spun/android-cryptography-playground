package com.spundev.cryptographyplayground

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CipherActivity : AppCompatActivity() {

    private lateinit var secretKey: SecretKey
    private lateinit var iv: ByteArray

    companion object {
        // save state keys
        private const val STATE_SECRET_KEY = "secretKey"
        private const val STATE_IV = "initializationVector"

        fun start(context: Context) {
            val starter = Intent(context, CipherActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cipher)

        // Input EditText
        val inputText: EditText = findViewById(R.id.inputText)
        // Initial vector TextView
        val initialVectorTextView: TextView = findViewById(R.id.initialVectorTextView)

        // Retrieve previous secret key and iv
        if (savedInstanceState != null) {
            secretKey = savedInstanceState.getSerializable(STATE_SECRET_KEY) as SecretKey
            iv = savedInstanceState.getByteArray(STATE_IV)!!
        } else {
            secretKey = generateKey()
            iv = ByteArray(0)
        }
        // Show the initialization vector content
        initialVectorTextView.text = iv.contentToString()

        // Click events
        // Encrypt button
        val encryptButton: Button = findViewById(R.id.encryptButton)
        encryptButton.setOnClickListener {
            // Retrieve original message from the EditText view
            val originalMessage = inputText.text.toString()
            // Encrypt using our previously generated secret key
            val (cipherText, initialVector) = encrypt(secretKey, originalMessage)
            // Save the initial vector for decryption
            iv = initialVector
            // Show the result as Base64
            inputText.setText(Base64.encodeToString(cipherText, Base64.DEFAULT))
            // Show the initialization vector content
            initialVectorTextView.text = iv.contentToString()
        }
        // Decrypt button
        val decryptButton: Button = findViewById(R.id.decryptButton)
        decryptButton.setOnClickListener {
            if (::iv.isInitialized) {
                // Retrieve the original cipher text from the EditText view
                val cipherText = Base64.decode(inputText.text.toString(), Base64.DEFAULT)
                // Decrypt using the secret key and the initial vector
                val retrievedText = decrypt(secretKey, iv, cipherText)
                // Show the result as Base64
                inputText.setText(String(retrievedText, Charset.defaultCharset()))
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save the secret key and the iv
        outState.putSerializable(STATE_SECRET_KEY, secretKey)
        outState.putByteArray(STATE_IV, iv)
        super.onSaveInstanceState(outState)
    }

    private fun generateKey(): SecretKey {
        // Generate key
        val keygen = KeyGenerator.getInstance("AES").apply {
            init(256)
        }
        return keygen.generateKey()
    }

    private fun encrypt(secretKey: SecretKey, message: String): Pair<ByteArray, ByteArray> {
        // message to byte array
        val originalMessage = message.toByteArray()
        // encrypt message
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, secretKey)
        }
        val cipherMessage = cipher.doFinal(originalMessage)
        val iv = cipher.iv

        return Pair(cipherMessage, iv)
    }

    private fun decrypt(secretKey: SecretKey, iv: ByteArray, cipherMessage: ByteArray): ByteArray {
        // decrypt message
        val spec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, secretKey, spec)
        }
        return try {
            cipher.doFinal(cipherMessage)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to decrypt with the given values", Toast.LENGTH_SHORT).show()
            ByteArray(0)
        }
    }
}
