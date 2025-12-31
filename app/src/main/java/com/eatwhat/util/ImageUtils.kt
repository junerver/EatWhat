package com.eatwhat.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream

/**
 * Utility class for image processing
 * Handles image compression to WebP format and Base64 encoding
 * Designed for storing recipe images in database with future export/import support
 */
object ImageUtils {

    /**
     * Maximum image dimension (width or height) after resizing
     * Keeps images reasonable for storage while maintaining quality
     */
    private const val MAX_IMAGE_DIMENSION = 800

    /**
     * WebP compression quality (0-100)
     * 80 provides good balance between quality and file size
     */
    private const val WEBP_QUALITY = 80

    /**
     * Maximum Base64 string length (approximately 500KB of image data)
     * Helps prevent database bloat
     */
    private const val MAX_BASE64_LENGTH = 700_000

    /**
     * Result of image processing
     */
    sealed class ImageProcessingResult {
        data class Success(val base64: String) : ImageProcessingResult()
        data class Error(val message: String) : ImageProcessingResult()
    }

    /**
     * Process an image from URI to Base64 encoded WebP string
     *
     * @param context Android context
     * @param uri Image URI (from gallery or camera)
     * @return ImageProcessingResult with Base64 string or error message
     */
    fun processImageToBase64(context: Context, uri: Uri): ImageProcessingResult {
        return try {
            // Load bitmap from URI
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ImageProcessingResult.Error("无法读取图片")

            // Read EXIF orientation
            val exifInputStream = context.contentResolver.openInputStream(uri)
            val orientation = try {
                exifInputStream?.let {
                    val exif = ExifInterface(it)
                    exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (e: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            } finally {
                exifInputStream?.close()
            }

            // Decode bitmap with options
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate sample size for efficient loading
            val sampleSize = calculateSampleSize(options.outWidth, options.outHeight)

            // Load sampled bitmap
            val sampledInputStream = context.contentResolver.openInputStream(uri)
                ?: return ImageProcessingResult.Error("无法读取图片")

            val sampledOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val sampledBitmap = BitmapFactory.decodeStream(sampledInputStream, null, sampledOptions)
            sampledInputStream.close()

            if (sampledBitmap == null) {
                return ImageProcessingResult.Error("无法解码图片")
            }

            // Apply rotation if needed
            val rotatedBitmap = rotateBitmapIfRequired(sampledBitmap, orientation)

            // Resize if still too large
            val resizedBitmap = resizeBitmapIfNeeded(rotatedBitmap)

            // Compress to WebP
            val base64 = compressToWebPBase64(resizedBitmap)

            // Clean up
            if (resizedBitmap != rotatedBitmap) {
                rotatedBitmap.recycle()
            }
            if (rotatedBitmap != sampledBitmap) {
                sampledBitmap.recycle()
            }
            resizedBitmap.recycle()

            // Check size limit
            if (base64.length > MAX_BASE64_LENGTH) {
                return ImageProcessingResult.Error("图片太大，请选择较小的图片")
            }

            ImageProcessingResult.Success(base64)
        } catch (e: Exception) {
            ImageProcessingResult.Error("处理图片时出错: ${e.message}")
        }
    }

    /**
     * Decode Base64 string to Bitmap
     *
     * @param base64 Base64 encoded image string
     * @return Bitmap or null if decoding fails
     */
    fun decodeBase64ToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if a Base64 string is valid
     */
    fun isValidBase64Image(base64: String?): Boolean {
        if (base64.isNullOrEmpty()) return false
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            bytes.isNotEmpty() && BitmapFactory.decodeByteArray(bytes, 0, bytes.size) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Calculate appropriate sample size for loading large images
     */
    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        val maxDimension = maxOf(width, height)

        while (maxDimension / sampleSize > MAX_IMAGE_DIMENSION * 2) {
            sampleSize *= 2
        }

        return sampleSize
    }

    /**
     * Rotate bitmap based on EXIF orientation
     */
    private fun rotateBitmapIfRequired(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            else -> return bitmap
        }

        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            bitmap
        }
    }

    /**
     * Resize bitmap if it exceeds maximum dimension
     */
    private fun resizeBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= MAX_IMAGE_DIMENSION && height <= MAX_IMAGE_DIMENSION) {
            return bitmap
        }

        val scale = if (width > height) {
            MAX_IMAGE_DIMENSION.toFloat() / width
        } else {
            MAX_IMAGE_DIMENSION.toFloat() / height
        }

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Compress bitmap to WebP format and encode as Base64
     */
    private fun compressToWebPBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()

        // Use WebP compression (lossy for smaller size)
        // For Android 11+ (API 30+), use WEBP_LOSSY
        // For older versions, use WEBP which defaults to lossy
        @Suppress("DEPRECATION")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, WEBP_QUALITY, outputStream)
        } else {
            bitmap.compress(Bitmap.CompressFormat.WEBP, WEBP_QUALITY, outputStream)
        }

        val bytes = outputStream.toByteArray()
      // Use NO_WRAP to avoid newlines in Base64 string which can cause issues with APIs
      return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Get estimated size of Base64 encoded image in KB
     */
    fun getBase64SizeKB(base64: String): Int {
        // Base64 adds about 33% overhead, so actual binary size is about 75% of string length
        return (base64.length * 0.75 / 1024).toInt()
    }
}