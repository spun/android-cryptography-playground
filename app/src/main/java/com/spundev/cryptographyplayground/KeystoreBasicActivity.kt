package com.spundev.cryptographyplayground

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.AlgorithmParameterSpec


class KeystoreBasicActivity : AppCompatActivity() {

    private lateinit var signatureResult: ByteArray

    companion object {
        // key alias for the keystore entry
        const val keyAlias = "keystoreBasicAlias"

        // save state result
        private const val STATE_SIGNATURE_RESULT = "signatureResult"

        fun start(context: Context) {
            val starter = Intent(context, KeystoreBasicActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keystore_basic)

        // Input EditText
        val inputText: EditText = findViewById(R.id.inputText)
        // Signature result TextView
        val signatureTextView: TextView = findViewById(R.id.signatureTextView)

        // Retrieve previously calculated signature
        signatureResult = if (savedInstanceState != null) {
            savedInstanceState.getByteArray(STATE_SIGNATURE_RESULT)!!
        } else {
            ByteArray(0)
        }
        // Show the current signature content
        signatureTextView.text = signatureResult.contentToString()

        // Check if the keystore has a key with the alias
        if (!doesKeyAliasExists()) {
            // if we don't find a key with the alias, create new
            generateNewKey()
        }

        // Click events
        // Sign button
        val signButton: Button = findViewById(R.id.signButton)
        signButton.setOnClickListener {
            // Retrieve message from the EditText view
            val message = inputText.text.toString()
            // obtain signature
            val privateKey = getKeyFromKeystore()
            signatureResult = sign(privateKey, message)
            // Show result
            signatureTextView.text = signatureResult.contentToString()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save the current signature result
        outState.putByteArray(STATE_SIGNATURE_RESULT, signatureResult)
        super.onSaveInstanceState(outState)
    }

    private fun doesKeyAliasExists(): Boolean {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        return ks.containsAlias(keyAlias)
    }

    private fun generateNewKey() {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )

        val parameterSpec: AlgorithmParameterSpec =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                ).run {
                    setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    build()
                }
            } else {
                KeyPairGeneratorSpec.Builder(this).run {
                    setAlias(keyAlias) // used to retrieve the key
                    build()
                }
            }

        kpg.initialize(parameterSpec)
        kpg.generateKeyPair()
    }

    private fun getKeyFromKeystore(): PrivateKey {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val entry: KeyStore.Entry = ks.getEntry(keyAlias, null)
        if (entry !is KeyStore.PrivateKeyEntry) {
            throw Exception("Not an instance of a PrivateKeyEntry")
        } else {
            return entry.privateKey
        }
    }

    private fun sign(privateKey: PrivateKey, message: String): ByteArray {
        return Signature.getInstance("SHA256withECDSA").run {
            initSign(privateKey)
            update(message.toByteArray())
            sign()
        }
    }
}
