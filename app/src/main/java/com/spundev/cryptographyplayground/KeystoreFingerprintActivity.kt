package com.spundev.cryptographyplayground

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.google.android.material.snackbar.Snackbar
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.AlgorithmParameterSpec
import java.util.concurrent.Executor

class KeystoreFingerprintActivity : AppCompatActivity() {

    private lateinit var signatureResult: ByteArray

    companion object {
        private const val TAG = "KeystoreFingerprint"

        // key alias for the keystore entry
        const val keyAlias = "keystoreFingerprintAlias"

        // save state result
        private const val STATE_SIGNATURE_RESULT = "signatureResult"

        fun start(context: Context) {
            val starter = Intent(context, KeystoreFingerprintActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keystore_fingerprint)

        // Main ConstraintLayout
        val constraintLayout: ConstraintLayout = findViewById(R.id.constraintLayout)

        // Check if the user hasn't set up a fingerprint or lock screen
        val mKeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!mKeyguardManager.isKeyguardSecure) {
            // Show a message that the user hasn't set up a fingerprint or lock screen.
            Snackbar
                .make(constraintLayout, "No fingerprint or lock screen set up.", Snackbar.LENGTH_INDEFINITE)
                .setAction("Go to settings") {
                    startFingerprintEnrollment()
                }
                .show()
            return
        }

        // Check if no fingerprints are registered
        val fingerprintManager = FingerprintManagerCompat.from(this)
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            Snackbar
                .make(constraintLayout, "No fingerprints registered.", Snackbar.LENGTH_INDEFINITE)
                .setAction("Go to settings") {
                    startFingerprintEnrollment()
                }
                .show()
            return
        }

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
            // get key
            val privateKey = getKeyFromKeystore()
            // initialize signature with key
            val signatureCryptoObject = initSignature(privateKey)

            // Show authentication prompt
            openBiometricPrompt(signatureCryptoObject) { authorizedSignatureObject ->
                // calculate signature
                signatureResult = sign(authorizedSignatureObject, message)
                // Show result
                signatureTextView.text = signatureResult.contentToString()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save the current signature result
        outState.putByteArray(STATE_SIGNATURE_RESULT, signatureResult)
        super.onSaveInstanceState(outState)
    }

    private fun startFingerprintEnrollment() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Intent(Settings.ACTION_FINGERPRINT_ENROLL)
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        startActivityForResult(intent, 0)
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
                    // Require the user to authenticate with a fingerprint to authorize
                    // every use of the private key
                    setUserAuthenticationRequired(true)
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

    private fun initSignature(privateKey: PrivateKey): Signature {
        return Signature.getInstance("SHA256withECDSA").apply {
            initSign(privateKey)
        }
    }

    private fun openBiometricPrompt(signatureCryptoObject: Signature, callback: (Signature) -> Unit) {

        val executor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) mainExecutor else MainThreadExecutor()

        // Prepare prompt
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Test Title")
            .setSubtitle("Test Subtitle")
            .setDescription("Test description")
            .setNegativeButtonText("Cancel")
            .build()


        BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.e(TAG, "AuthenticationError: ($errorCode) $errString")
                Toast.makeText(this@KeystoreFingerprintActivity, errString, Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // get cryptoObject
                val resultCryptoObject = result.cryptoObject
                if (resultCryptoObject != null) {
                    // get signature from cryptoObject
                    val resultSignatureObject = resultCryptoObject.signature
                    if (resultSignatureObject != null) {
                        callback(resultSignatureObject)
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.e(TAG, "onAuthenticationFailed")
            }
        }).authenticate(promptInfo, BiometricPrompt.CryptoObject(signatureCryptoObject))

    }

    private fun sign(signature: Signature, message: String): ByteArray {
        return signature.run {
            update(message.toByteArray())
            sign()
        }
    }

    class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}
