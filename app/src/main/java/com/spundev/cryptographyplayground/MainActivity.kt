package com.spundev.cryptographyplayground

import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Basic
        // Cipher activity
        val cipherButton: Button = findViewById(R.id.cipherActivityButton)
        cipherButton.setOnClickListener {
            CipherActivity.start(this)
        }
        // Message digest activity
        val digestButton: Button = findViewById(R.id.digestActivityButton)
        digestButton.setOnClickListener {
            DigestMessageActivity.start(this)
        }
        // Signature activity
        val signatureButton: Button = findViewById(R.id.signatureActivityButton)
        signatureButton.setOnClickListener {
            SignatureActivity.start(this)
        }
        // MAC activity
        val macButton: Button = findViewById(R.id.macActivityButton)
        macButton.setOnClickListener {
            MacActivity.start(this)
        }

        // Keystore
        // Basic
        val keystoreBasicButton: Button = findViewById(R.id.keystoreBasicActivityButton)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keystoreBasicButton.isEnabled = true
            keystoreBasicButton.setOnClickListener {
                KeystoreBasicActivity.start(this)
            }
        }
        // Fingerprint
        val keystoreFingerprintButton: Button = findViewById(R.id.keystoreFingerprintActivityButton)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keystoreFingerprintButton.isEnabled = true
            keystoreFingerprintButton.setOnClickListener {
                KeystoreFingerprintActivity.start(this)
            }
        }
    }
}
