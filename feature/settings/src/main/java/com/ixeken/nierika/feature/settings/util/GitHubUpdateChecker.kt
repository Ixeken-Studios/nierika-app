package com.ixeken.nierika.feature.settings.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

/**
 * Resultado de la verificación de versiones en GitHub Releases.
 * Expone de forma exhaustiva los 5 estados de comprobación:
 * 1. [NewVersion]: Existe una nueva versión disponible.
 * 2. [UpToDate]: La aplicación está en la misma versión que GitHub.
 * 3. [FutureVersion]: La versión instalada es superior a la de GitHub (preview / viajero del tiempo).
 * 4. [RepoNotFoundOrPrivate]: El repositorio no existe o está configurado como privado.
 * 5. [NoInternet]: No hay conexión a internet o falló la comunicación de red.
 */
sealed interface UpdateResult {
    /** 1. Hay una nueva versión pública disponible en GitHub. */
    data class NewVersion(
        val version: String,
        val releaseUrl: String,
        val changelog: String?
    ) : UpdateResult

    /** 2. La versión instalada coincide exactamente con el último release público. */
    data class UpToDate(val currentVersion: String) : UpdateResult

    /** 3. La versión instalada es superior a la versión de GitHub (preview / viajero en el tiempo). */
    data class FutureVersion(val currentVersion: String, val latestVersion: String) : UpdateResult

    /** 4. El repositorio no existe (HTTP 404) o está configurado como privado / restringido (HTTP 403). */
    data object RepoNotFoundOrPrivate : UpdateResult

    /** 5. El dispositivo no tiene conexión a internet o falló la resolución de red. */
    data object NoInternet : UpdateResult
}

/**
 * Utilidad asíncrona para consultar la última versión pública de Nierika en GitHub Releases.
 */
object GitHubUpdateChecker {
    private const val REPO_LATEST_RELEASE_API = "https://api.github.com/repos/Ixeken-Studios/nierika-app/releases/latest"
    private const val FALLBACK_RELEASES_URL = "https://github.com/Ixeken-Studios/nierika-app/releases/latest"

    suspend fun checkForUpdates(context: Context): UpdateResult = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(context)) {
            return@withContext UpdateResult.NoInternet
        }

        var connection: HttpURLConnection? = null
        try {
            val url = URL(REPO_LATEST_RELEASE_API)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Nierika-Gallery-App")
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            val responseCode = connection.responseCode
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val tagName = json.optString("tag_name", "")
                    val htmlUrl = json.optString("html_url", FALLBACK_RELEASES_URL)
                    val body = if (json.has("body") && !json.isNull("body")) json.getString("body") else null

                    val currentVersion = try {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
                    } catch (_: Exception) {
                        "1.0"
                    }

                    val comparison = compareSemVer(currentVersion, tagName)
                    when {
                        comparison < 0 -> UpdateResult.NewVersion(
                            version = tagName,
                            releaseUrl = htmlUrl,
                            changelog = body
                        )
                        comparison > 0 -> UpdateResult.FutureVersion(
                            currentVersion = currentVersion,
                            latestVersion = tagName
                        )
                        else -> UpdateResult.UpToDate(
                            currentVersion = currentVersion
                        )
                    }
                }
                HttpURLConnection.HTTP_NOT_FOUND,
                HttpURLConnection.HTTP_FORBIDDEN -> {
                    // Estado 4: El repositorio no existe en GitHub o es privado
                    UpdateResult.RepoNotFoundOrPrivate
                }
                else -> {
                    UpdateResult.NoInternet
                }
            }
        } catch (_: UnknownHostException) {
            UpdateResult.NoInternet
        } catch (_: IOException) {
            UpdateResult.NoInternet
        } catch (_: Exception) {
            UpdateResult.NoInternet
        } finally {
            connection?.disconnect()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val network = connectivityManager.activeNetwork ?: return true
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return true
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    private fun compareSemVer(current: String, latest: String): Int {
        val cleanCurrent = current.removePrefix("v").removePrefix("V").trim()
        val cleanLatest = latest.removePrefix("v").removePrefix("V").trim()
        if (cleanCurrent == cleanLatest) return 0

        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until length) {
            val curr = currentParts.getOrElse(i) { 0 }
            val lat = latestParts.getOrElse(i) { 0 }
            if (curr > lat) return 1
            if (lat > curr) return -1
        }
        return 0
    }
}
