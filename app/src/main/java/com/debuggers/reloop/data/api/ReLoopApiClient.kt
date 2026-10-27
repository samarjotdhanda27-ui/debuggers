package com.debuggers.reloop.data.api

import com.debuggers.reloop.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ReLoopApiClient {
    var baseUrl: String = "https://reloop-ui.onrender.com"
    var apiKey: String = "K4ikJWBZZc5_3A2U1G74zr6wOial3tbiv17LzcJB8MM"

    private const val CONNECT_TIMEOUT = 10000
    private const val READ_TIMEOUT = 25000

    private suspend fun executeGet(path: String, auth: Boolean = false): Pair<Int, String?> =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl$path")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "ReLoop-Android/2.0")
                if (auth && apiKey.isNotEmpty()) {
                    conn.setRequestProperty("X-API-Key", apiKey)
                }
                conn.connectTimeout = CONNECT_TIMEOUT
                conn.readTimeout = READ_TIMEOUT

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val responseBody = stream?.bufferedReader()?.use { it.readText() }
                Pair(code, responseBody)
            } catch (e: Exception) {
                Pair(-1, e.message)
            }
        }

    private suspend fun executePost(path: String, body: JSONObject, auth: Boolean = false): Pair<Int, String?> =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl$path")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "ReLoop-Android/2.0")
                if (auth && apiKey.isNotEmpty()) {
                    conn.setRequestProperty("X-API-Key", apiKey)
                }
                conn.doOutput = true
                conn.connectTimeout = CONNECT_TIMEOUT
                conn.readTimeout = READ_TIMEOUT

                OutputStreamWriter(conn.outputStream, "UTF-8").use {
                    it.write(body.toString())
                    it.flush()
                }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val responseBody = stream?.bufferedReader()?.use { it.readText() }
                Pair(code, responseBody)
            } catch (e: Exception) {
                Pair(-1, e.message)
            }
        }

    suspend fun checkHealth(): ServerHealth {
        val start = System.currentTimeMillis()
        val (code, response) = executeGet("/health", auth = false)
        val latency = System.currentTimeMillis() - start

        return if (code == 200 && response != null) {
            try {
                val json = JSONObject(response)
                ServerHealth(
                    isOnline = true,
                    status = json.optString("status", "ok"),
                    project = json.optString("project", "ReLoop"),
                    database = json.optString("database", "Supabase PostgreSQL"),
                    latencyMs = latency
                )
            } catch (e: Exception) {
                ServerHealth(isOnline = false, latencyMs = latency, errorMessage = e.message)
            }
        } else {
            ServerHealth(isOnline = false, latencyMs = latency, errorMessage = response ?: "Connection failed (Code $code)")
        }
    }

    suspend fun getMaterials(): List<MaterialItem> {
        val (code, response) = executeGet("/materials", auth = true)
        if (code == 200 && response != null) {
            try {
                val json = JSONObject(response)
                val array = json.optJSONArray("materials") ?: JSONArray()
                val list = mutableListOf<MaterialItem>()
                for (i in 0 until array.length()) {
                    list.add(MaterialItem.fromJson(array.getJSONObject(i)))
                }
                return list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return emptyList()
    }

    suspend fun getRequirements(): List<RequirementItem> {
        val (code, response) = executeGet("/requirements", auth = true)
        if (code == 200 && response != null) {
            try {
                val json = JSONObject(response)
                val array = json.optJSONArray("requirements") ?: JSONArray()
                val list = mutableListOf<RequirementItem>()
                for (i in 0 until array.length()) {
                    list.add(RequirementItem.fromJson(array.getJSONObject(i)))
                }
                return list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return emptyList()
    }

    suspend fun getMatchesForMaterial(materialId: String, maxDistanceKm: Double = 300.0): MaterialMatchesResult? {
        val path = "/materials/$materialId/matches?max_distance_km=$maxDistanceKm"
        val (code, response) = executeGet(path, auth = true)
        if (code == 200 && response != null) {
            try {
                val json = JSONObject(response)
                val matchArray = json.optJSONArray("matches") ?: JSONArray()
                val matches = mutableListOf<IntelligentMatch>()
                for (i in 0 until matchArray.length()) {
                    matches.add(IntelligentMatch.fromJson(matchArray.getJSONObject(i)))
                }
                val sellerLoc = json.optJSONObject("seller_location")
                return MaterialMatchesResult(
                    materialId = json.optString("material_id", materialId),
                    sellerName = json.optString("seller_name", "Seller"),
                    materialName = json.optString("material_name", "Material"),
                    availableQuantityTonnes = json.optDouble("available_quantity_tonnes", 0.0),
                    sellerCity = sellerLoc?.optString("city", "") ?: "",
                    sellerState = sellerLoc?.optString("state", "") ?: "",
                    maxDistanceKm = json.optDouble("max_distance_km", maxDistanceKm),
                    totalMatches = json.optInt("total_matches", matches.size),
                    matches = matches,
                    disclaimer = json.optString("disclaimer", "")
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    suspend fun getReusePathways(materialName: String): List<ReusePathway> {
        val encoded = withContext(Dispatchers.IO) {
            URLEncoder.encode(materialName.trim(), "UTF-8")
        }
        val (code, response) = executeGet("/pathways/$encoded", auth = false)
        if (code == 200 && response != null) {
            try {
                val json = JSONObject(response)
                val arr = json.optJSONArray("pathways") ?: JSONArray()
                val list = mutableListOf<ReusePathway>()
                for (i in 0 until arr.length()) {
                    list.add(ReusePathway.fromJson(arr.getJSONObject(i)))
                }
                return list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return emptyList()
    }

    suspend fun createMaterial(body: JSONObject): Pair<Boolean, String> {
        val (code, response) = executePost("/materials", body, auth = true)
        return when {
            code in 200..299 -> {
                val msg = try {
                    JSONObject(response ?: "{}").optString("message", "Listing successfully created on ReLoop network.")
                } catch (e: Exception) {
                    "Listing created successfully."
                }
                Pair(true, msg)
            }
            code == 422 -> Pair(false, "Validation error: Check all required fields.")
            code == -1 -> Pair(false, "Network error: Unable to connect to ReLoop server ($response).")
            else -> Pair(false, "Server error ($code): ${response ?: "Unknown error"}")
        }
    }

    suspend fun createRequirement(body: JSONObject): Pair<Boolean, String> {
        val (code, response) = executePost("/requirements", body, auth = true)
        return when {
            code in 200..299 -> {
                val msg = try {
                    JSONObject(response ?: "{}").optString("message", "Requirement registered in matching engine.")
                } catch (e: Exception) {
                    "Requirement created successfully."
                }
                Pair(true, msg)
            }
            code == 422 -> Pair(false, "Validation error: Check all required fields.")
            code == -1 -> Pair(false, "Network error: Unable to connect to ReLoop server ($response).")
            else -> Pair(false, "Server error ($code): ${response ?: "Unknown error"}")
        }
    }

    suspend fun getPassportByMaterialId(materialId: String): JSONObject? {
        val (code, response) = executeGet("/passport/$materialId", auth = true)
        if (code in 200..299 && response != null) {
            return try {
                JSONObject(response)
            } catch (e: Exception) {
                null
            }
        }
        return null
    }
}
