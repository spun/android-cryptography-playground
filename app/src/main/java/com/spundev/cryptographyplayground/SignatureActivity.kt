package com.spundev.cryptographyplayground

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyProperties
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.security.*

class SignatureActivity : AppCompatActivity() {

    private lateinit var keyPair: KeyPair
    private lateinit var signature: ByteArray

    companion object {
        // save state keys
        private const val STATE_KEY_PAIR = "keyPair"
        private const val STATE_SIGNATURE = "signature"

        fun start(context: Context) {
            val starter = Intent(context, SignatureActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature)

        // Input EditText
        val inputText: EditText = findViewById(R.id.inputText)
        // Signature result TextView
        val signatureTextView: TextView = findViewById(R.id.signatureTextView)
        // Signature result TextView
        val verifyTextView: TextView = findViewById(R.id.verifyTextView)

        // Retrieve previous key pair
        if (savedInstanceState != null) {
            keyPair = savedInstanceState.getSerializable(STATE_KEY_PAIR) as KeyPair
            signature = savedInstanceState.getByteArray(STATE_SIGNATURE)!!
        } else {
            keyPair = generateKey()
            signature = ByteArray(0)
        }
        // Show the current signature content
        signatureTextView.text = signature.contentToString()

        // Click events
        // Sign button
        val signButton: Button = findViewById(R.id.signButton)
        signButton.setOnClickListener {
            // Retrieve message from the EditText view
            val message = inputText.text.toString()
            // obtain signature
            signature = sign(keyPair.private, message)
            // Show result
            signatureTextView.text = signature.contentToString()
        }
        // Verify button
        val verifyButton: Button = findViewById(R.id.verifyButton)
        verifyButton.setOnClickListener {
            // Retrieve message from the EditText view
            val message = inputText.text.toString()
            // verify with signature
            val isValid = verify(keyPair.public, message, signature)
            // Show result
            verifyTextView.text = isValid.toString()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save the key pair and the current signature
        outState.putSerializable(STATE_KEY_PAIR, keyPair)
        outState.putByteArray(STATE_SIGNATURE, signature)
        super.onSaveInstanceState(outState)
    }

    private fun generateKey(): KeyPair {
        // Generate key pair
        val keygen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC)
        } else {
            KeyPairGenerator.getInstance("EC")
        }
        return keygen.generateKeyPair()
    }

    private fun sign(privateKey: PrivateKey, message: String): ByteArray {
        return Signature.getInstance("SHA256withECDSA").run {
            initSign(privateKey)
            update(message.toByteArray())
            sign()
        }
    }

    private fun verify(publicKey: PublicKey, message: String, signature: ByteArray): Boolean {
        return Signature.getInstance("SHA256withECDSA").run {
            initVerify(publicKey)
            update(message.toByteArray())
            verify(signature)
        }
    }
}
