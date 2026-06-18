package com.eatwhat.data.sync

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-GCM 加解密管理器
 */
object CryptoManager {

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_TAG_LENGTH = 128
    private const val IV_SIZE = 12
    private const val SALT_SIZE = 16
    private const val PBKDF2_ITERATIONS = 100000

    /**
     * 加密后的数据结构
     */
    data class EncryptedData(
        val ciphertext: ByteArray,
        val iv: ByteArray,
        val salt: ByteArray
    ) {
        /**
         * 将加密数据序列化为字节数组
         * 格式: [salt(16)] [iv(12)] [ciphertext]
         */
        fun toBytes(): ByteArray {
            return salt + iv + ciphertext
        }

        companion object {
            /**
             * 从字节数组反序列化加密数据
             */
            fun fromBytes(bytes: ByteArray): EncryptedData {
                require(bytes.size > SALT_SIZE + IV_SIZE) { "Invalid encrypted data" }
                val salt = bytes.copyOfRange(0, SALT_SIZE)
                val iv = bytes.copyOfRange(SALT_SIZE, SALT_SIZE + IV_SIZE)
                val ciphertext = bytes.copyOfRange(SALT_SIZE + IV_SIZE, bytes.size)
                return EncryptedData(ciphertext, iv, salt)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as EncryptedData
            return ciphertext.contentEquals(other.ciphertext) &&
                    iv.contentEquals(other.iv) &&
                    salt.contentEquals(other.salt)
        }

        override fun hashCode(): Int {
            var result = ciphertext.contentHashCode()
            result = 31 * result + iv.contentHashCode()
            result = 31 * result + salt.contentHashCode()
            return result
        }
    }

    /**
     * 使用密码加密数据
     * @param data 原始数据
     * @param password 加密密码
     * @return 加密后的数据（含 IV 和 Salt）
     */
    fun encrypt(data: ByteArray, password: String): EncryptedData {
        val salt = generateSalt()
        val key = deriveKey(password, salt)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val ciphertext = cipher.doFinal(data)

        return EncryptedData(ciphertext, iv, salt)
    }

    /**
     * 使用密码解密数据
     * @param encryptedData 加密数据
     * @param password 解密密码
     * @return 解密后的数据
     * @throws DecryptionException 密码错误或数据损坏
     */
    fun decrypt(encryptedData: EncryptedData, password: String): ByteArray {
        return try {
            val key = deriveKey(password, encryptedData.salt)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, encryptedData.iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            cipher.doFinal(encryptedData.ciphertext)
        } catch (e: Exception) {
            throw DecryptionException("解密失败：密码错误或数据损坏", e)
        }
    }

    /**
     * 使用 PBKDF2 从密码派生密钥
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

    /**
     * 生成随机盐值
     */
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_SIZE)
        SecureRandom().nextBytes(salt)
        return salt
    }
}

/**
 * 解密异常
 */
class DecryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)
