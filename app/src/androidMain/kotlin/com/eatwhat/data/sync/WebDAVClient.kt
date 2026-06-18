package com.eatwhat.data.sync

import at.bitfire.dav4jvm.DavResource
import at.bitfire.dav4jvm.exception.DavException
import at.bitfire.dav4jvm.exception.HttpException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * WebDAV 客户端封装
 * 提供与 WebDAV 服务器交互的功能
 */
class WebDAVClient(
    private val serverUrl: String,
    private val username: String,
    private val password: String
) {
    private val credentials = Credentials.basic(username, password)

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(false)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            // 使用预认证拦截器，每个请求都带上 Authorization 头
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", credentials)
                    .build()
                chain.proceed(request)
            })
            .build()
    }

    /**
     * 测试连接
     */
    suspend fun testConnection(): ConnectionResult = withContext(Dispatchers.IO) {
        try {
            val url = normalizeUrl(serverUrl).toHttpUrl()
            val davResource = DavResource(client, url)

            var success = false
            davResource.propfind(depth = 0) { _, _ ->
                // 如果回调被执行，说明请求成功
                success = true
            }

            if (success) {
                ConnectionResult.Success
            } else {
                ConnectionResult.Error("无法连接到服务器")
            }
        } catch (e: HttpException) {
            when (e.code) {
                401 -> ConnectionResult.Error("认证失败，请检查用户名和密码", e.code)
                403 -> ConnectionResult.Error("访问被拒绝", e.code)
                404 -> ConnectionResult.Error("路径不存在", e.code)
                else -> ConnectionResult.Error("HTTP 错误: ${e.code}", e.code)
            }
        } catch (e: DavException) {
            ConnectionResult.Error("WebDAV 错误: ${e.message}")
        } catch (e: IOException) {
            ConnectionResult.Error("网络错误: ${e.message}")
        } catch (e: Exception) {
            ConnectionResult.Error("连接失败: ${e.message}")
        }
    }

    /**
     * 确保远程目录存在
     * 逐级创建目录路径
     */
    suspend fun ensureDirectory(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 分解路径，逐级创建
            val pathParts = remotePath.trim('/').split('/')
            var currentPath = ""

            for (part in pathParts) {
                if (part.isEmpty()) continue
                currentPath += "/$part"

                val url = buildUrl(currentPath).toHttpUrl()
                val davResource = DavResource(client, url)

                // 先检查目录是否存在
                var exists = false
                try {
                    davResource.propfind(depth = 0) { _, _ ->
                        exists = true
                    }
                } catch (e: HttpException) {
                    if (e.code != 404) {
                        // 非 404 错误，可能是权限问题
                        return@withContext false
                    }
                }

                if (!exists) {
                    // 目录不存在，创建它
                    try {
                        davResource.mkCol(null) {}
                    } catch (e: HttpException) {
                        // 405 表示目录已存在（并发创建时可能发生）
                        if (e.code != 405) {
                            return@withContext false
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 上传数据
     */
    suspend fun upload(remotePath: String, data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(remotePath).toHttpUrl()
            val davResource = DavResource(client, url)
            val requestBody = data.toRequestBody("application/json".toMediaType())

            davResource.put(requestBody) { response ->
                if (!response.isSuccessful) {
                    throw HttpException(response)
                }
            }
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(WebDAVException("上传失败: HTTP ${e.code}", e))
        } catch (e: Exception) {
            Result.failure(WebDAVException("上传失败: ${e.message}", e))
        }
    }

    /**
     * 下载数据
     */
    suspend fun download(remotePath: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(remotePath).toHttpUrl()
            val davResource = DavResource(client, url)
            var result: ByteArray? = null

            davResource.get("application/json") { response ->
                if (response.isSuccessful) {
                    result = response.body?.bytes()
                } else {
                    throw HttpException(response)
                }
            }

            result?.let {
                Result.success(it)
            } ?: Result.failure(WebDAVException("下载失败: 响应为空"))
        } catch (e: HttpException) {
            when (e.code) {
                404 -> Result.failure(WebDAVException("文件不存在", e))
                else -> Result.failure(WebDAVException("下载失败: HTTP ${e.code}", e))
            }
        } catch (e: Exception) {
            Result.failure(WebDAVException("下载失败: ${e.message}", e))
        }
    }

    /**
     * 检查文件是否存在
     */
    suspend fun exists(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(remotePath).toHttpUrl()
            val davResource = DavResource(client, url)
            var exists = false

            davResource.propfind(depth = 0) { _, _ ->
                // 如果回调被执行，说明文件存在
                exists = true
            }
            exists
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 删除文件
     */
    suspend fun delete(remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(remotePath).toHttpUrl()
            val davResource = DavResource(client, url)

            davResource.delete {}
            Result.success(Unit)
        } catch (e: HttpException) {
            if (e.code == 404) {
                // 文件不存在，视为成功
                Result.success(Unit)
            } else {
                Result.failure(WebDAVException("删除失败: HTTP ${e.code}", e))
            }
        } catch (e: Exception) {
            Result.failure(WebDAVException("删除失败: ${e.message}", e))
        }
    }

    private fun normalizeUrl(url: String): String {
        return url.trimEnd('/')
    }

    /**
     * 拼接服务器地址和远程路径，避免双斜杠
     */
    private fun buildUrl(remotePath: String): String {
        val base = serverUrl.trimEnd('/')
        val path = remotePath.trimStart('/')
        return "$base/$path".trimEnd('/')
    }
}

/**
 * WebDAV 异常
 */
class WebDAVException(message: String, cause: Throwable? = null) : Exception(message, cause)
