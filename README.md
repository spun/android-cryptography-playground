
# Android cryptography playground

Basic examples of cryptographic operations in Android using the [recommended algorithms](https://developer.android.com/guide/topics/security/cryptography#choose-algorithm)

<img src="screenshots/screenshot_main_0.png" width="200" style="max-width:100%;">

---

## Basic operations

### Cipher [`[CipherActivity.kt]`](app/src/main/java/com/spundev/cryptographyplayground/CipherActivity.kt)

Encrypts and decrypts a message using `AES/GCM/NoPadding`.

<img src="screenshots/screenshot_cipher_0.png" width="200" style="max-width:100%;">

<img src="screenshots/screenshot_cipher_1.png" width="200" style="max-width:100%;">

### MessageDigest [`[DigestMessageActivity.kt]`](app/src/main/java/com/spundev/cryptographyplayground/DigestMessageActivity.kt)

`SHA-256` of the given message.

<img src="screenshots/screenshot_digest_0.png" width="200" style="max-width:100%;">

### Mac [`[MacActivity.kt]`](app/src/main/java/com/spundev/cryptographyplayground/MacActivity.kt)

`HMACSHA256` of the message.

<img src="screenshots/screenshot_mac_0.png" width="200" style="max-width:100%;">

### Signature [`[SignatureActivity.kt]`](app/src/main/java/com/spundev/cryptographyplayground/SignatureActivity.kt)

Obtains the signature  of the given message using `SHA256withECDSA` and verifies the content.

<img src="screenshots/screenshot_signature_0.png" width="200" style="max-width:100%;">

<img src="screenshots/screenshot_signature_1.png" width="200" style="max-width:100%;">

---

## Android Keystore

### Basic [`[KeystoreBasicActivity.kt]`](app/src/main/java/com/spundev/cryptographyplayground/KeystoreBasicActivity.kt)

Obtains the signature of the message using a key entry from the keystore.

### With authentication [`[KeystoreFingerprintActivity.kt]`](app/src/main/java/com/spundev/cryptographyplayground/KeystoreFingerprintActivity.kt)

Obtains the signature of the message using a key entry from the keystore. However, unlike the basic example, in this case the key can only be used if the user has been authenticated. This authentication is done using the fingerprint scanner and the [biometrics library from androidx](https://developer.android.com/reference/androidx/biometrics/package-summary).

<img src="screenshots/screenshot_ks_fingerprint_0.png" width="200" style="max-width:100%;">

<img src="screenshots/screenshot_ks_fingerprint_1.png" width="200" style="max-width:100%;">
