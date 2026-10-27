package com.debuggers.reloop.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Material item representing a seller listing in the ReLoop marketplace.
 */
data class MaterialItem(
    val id: String,
    val sellerName: String,
    val materialName: String,
    val sourceIndustry: String,
    val quantityTonnes: Double,
    val city: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val properties: Map<String, Any> = emptyMap(),
    val availableDocuments: List<String> = emptyList(),
    val isSynthetic: Boolean = false,
    val createdAt: String = ""
) {
    val displayQuantity: String
        get() = "${quantityTonnes.toInt().takeIf { it.toDouble() == quantityTonnes } ?: quantityTonnes} tonnes"

    val displayLocation: String
        get() = if (city.isNotEmpty() && state.isNotEmpty()) "$city, $state" else city.ifEmpty { state }

    val shortCategory: String
        get() = when {
            materialName.contains("Fly", ignoreCase = true) || sourceIndustry.contains("Thermal", ignoreCase = true) -> "Mineral By-Product"
            materialName.contains("Sand", ignoreCase = true) || sourceIndustry.contains("Foundry", ignoreCase = true) -> "Foundry By-Product"
            materialName.contains("Scrap", ignoreCase = true) || sourceIndustry.contains("Steel", ignoreCase = true) -> "Secondary Metal"
            materialName.contains("Water", ignoreCase = true) || sourceIndustry.contains("Process", ignoreCase = true) -> "Process Water"
            materialName.contains("Slag", ignoreCase = true) -> "Furnace Slag"
            else -> sourceIndustry.ifEmpty { "Industrial By-Product" }
        }

    val symbol: String
        get() = when {
            materialName.contains("Fly", ignoreCase = true) -> "FA"
            materialName.contains("Foundry", ignoreCase = true) || materialName.contains("Sand", ignoreCase = true) -> "FS"
            materialName.contains("Metal", ignoreCase = true) || materialName.contains("Scrap", ignoreCase = true) -> "MS"
            materialName.contains("Water", ignoreCase = true) -> "PW"
            materialName.contains("Slag", ignoreCase = true) -> "SL"
            else -> materialName.take(2).uppercase()
        }

    companion object {
        fun fromJson(obj: JSONObject): MaterialItem {
            val docs = mutableListOf<String>()
            val docsArr = obj.optJSONArray("available_documents")
            if (docsArr != null) {
                for (i in 0 until docsArr.length()) {
                    docs.add(docsArr.getString(i))
                }
            }

            val propsMap = mutableMapOf<String, Any>()
            val propsObj = obj.optJSONObject("material_properties")
            if (propsObj != null) {
                val keys = propsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    propsMap[k] = propsObj.get(k)
                }
            }

            return MaterialItem(
                id = obj.optString("id", System.currentTimeMillis().toString()),
                sellerName = obj.optString("seller_name", "Unknown Facility"),
                materialName = obj.optString("material_name", "Unnamed Material"),
                sourceIndustry = obj.optString("source_industry", "Industrial"),
                quantityTonnes = obj.optDouble("quantity_tonnes", 0.0),
                city = obj.optString("city", ""),
                state = obj.optString("state", ""),
                latitude = obj.optDouble("latitude", 0.0),
                longitude = obj.optDouble("longitude", 0.0),
                properties = propsMap,
                availableDocuments = docs,
                isSynthetic = obj.optBoolean("is_synthetic", false),
                createdAt = obj.optString("created_at", "")
            )
        }
    }
}

/**
 * Requirement item representing buyer demand in the ReLoop marketplace.
 */
data class RequirementItem(
    val id: String,
    val buyerName: String,
    val buyerIndustry: String,
    val requiredMaterial: String,
    val requiredQuantityTonnes: Double,
    val city: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val requiredProperties: Map<String, Any> = emptyMap(),
    val requiredDocuments: List<String> = emptyList(),
    val isSynthetic: Boolean = false,
    val createdAt: String = ""
) {
    val displayQuantity: String
        get() = "${requiredQuantityTonnes.toInt().takeIf { it.toDouble() == requiredQuantityTonnes } ?: requiredQuantityTonnes} tonnes"

    val displayLocation: String
        get() = if (city.isNotEmpty() && state.isNotEmpty()) "$city, $state" else city.ifEmpty { state }

    companion object {
        fun fromJson(obj: JSONObject): RequirementItem {
            val docs = mutableListOf<String>()
            val docsArr = obj.optJSONArray("required_documents")
            if (docsArr != null) {
                for (i in 0 until docsArr.length()) {
                    docs.add(docsArr.getString(i))
                }
            }

            val propsMap = mutableMapOf<String, Any>()
            val propsObj = obj.optJSONObject("required_properties")
            if (propsObj != null) {
                val keys = propsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    propsMap[k] = propsObj.get(k)
                }
            }

            return RequirementItem(
                id = obj.optString("id", System.currentTimeMillis().toString()),
                buyerName = obj.optString("buyer_name", "Unknown Buyer"),
                buyerIndustry = obj.optString("buyer_industry", "Industrial"),
                requiredMaterial = obj.optString("required_material", "Material"),
                requiredQuantityTonnes = obj.optDouble("required_quantity_tonnes", 0.0),
                city = obj.optString("city", ""),
                state = obj.optString("state", ""),
                latitude = obj.optDouble("latitude", 0.0),
                longitude = obj.optDouble("longitude", 0.0),
                requiredProperties = propsMap,
                requiredDocuments = docs,
                isSynthetic = obj.optBoolean("is_synthetic", false),
                createdAt = obj.optString("created_at", "")
            )
        }
    }
}

/**
 * Score breakdown for intelligent matching.
 */
data class ScoreBreakdown(
    val propertyCompatibilityPoints: Double = 0.0,
    val quantityPoints: Double = 0.0,
    val proximityPoints: Double = 0.0,
    val documentationPoints: Double = 0.0,
    val maxPossiblePoints: Double = 100.0
)

/**
 * Matched buyer or partner from the Intelligent Matching Engine.
 */
data class IntelligentMatch(
    val requirementId: String,
    val buyerName: String,
    val buyerIndustry: String,
    val city: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val requiredQuantityTonnes: Double,
    val quantitySufficient: Boolean,
    val pathwayId: String,
    val recommendedApplication: String,
    val standardReference: String?,
    val compatibilityScore: Double,
    val scoreBreakdown: ScoreBreakdown,
    val matchedDocuments: List<String>,
    val missingDocuments: List<String>,
    val unverifiedProperties: List<String>,
    val explanations: List<String>,
    val verificationRequired: Boolean,
    val matchStatus: String,
    val isSynthetic: Boolean
) {
    companion object {
        fun fromJson(obj: JSONObject): IntelligentMatch {
            val scoreObj = obj.optJSONObject("score_breakdown")
            val breakdown = ScoreBreakdown(
                propertyCompatibilityPoints = scoreObj?.optDouble("property_compatibility_points", 0.0) ?: 0.0,
                quantityPoints = scoreObj?.optDouble("quantity_points", 0.0) ?: 0.0,
                proximityPoints = scoreObj?.optDouble("proximity_points", 0.0) ?: 0.0,
                documentationPoints = scoreObj?.optDouble("documentation_points", 0.0) ?: 0.0,
                maxPossiblePoints = scoreObj?.optDouble("max_possible_points", 100.0) ?: 100.0
            )

            fun jsonArrayToList(arr: JSONArray?): List<String> {
                if (arr == null) return emptyList()
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) list.add(arr.getString(i))
                return list
            }

            return IntelligentMatch(
                requirementId = obj.optString("requirement_id", ""),
                buyerName = obj.optString("buyer_name", "Target Buyer"),
                buyerIndustry = obj.optString("buyer_industry", ""),
                city = obj.optString("city", ""),
                state = obj.optString("state", ""),
                latitude = obj.optDouble("latitude", 0.0),
                longitude = obj.optDouble("longitude", 0.0),
                distanceKm = obj.optDouble("distance_km", 0.0),
                requiredQuantityTonnes = obj.optDouble("required_quantity_tonnes", 0.0),
                quantitySufficient = obj.optBoolean("quantity_sufficient", true),
                pathwayId = obj.optString("pathway_id", ""),
                recommendedApplication = obj.optString("recommended_application", "Industrial Reuse"),
                standardReference = if (obj.has("standard_reference") && !obj.isNull("standard_reference")) obj.getString("standard_reference") else null,
                compatibilityScore = obj.optDouble("compatibility_score", 0.0),
                scoreBreakdown = breakdown,
                matchedDocuments = jsonArrayToList(obj.optJSONArray("matched_documents")),
                missingDocuments = jsonArrayToList(obj.optJSONArray("missing_documents")),
                unverifiedProperties = jsonArrayToList(obj.optJSONArray("unverified_properties")),
                explanations = jsonArrayToList(obj.optJSONArray("explanations")),
                verificationRequired = obj.optBoolean("verification_required", true),
                matchStatus = obj.optString("match_status", "eligible_for_review"),
                isSynthetic = obj.optBoolean("is_synthetic", false)
            )
        }
    }
}

/**
 * Result container for Intelligent Matching for a material listing.
 */
data class MaterialMatchesResult(
    val materialId: String,
    val sellerName: String,
    val materialName: String,
    val availableQuantityTonnes: Double,
    val sellerCity: String,
    val sellerState: String,
    val maxDistanceKm: Double,
    val totalMatches: Int,
    val matches: List<IntelligentMatch>,
    val disclaimer: String
)

/**
 * Circular reuse pathway definition supported by research and standards.
 */
data class ReusePathway(
    val id: String,
    val buyerIndustry: String,
    val application: String,
    val relevantProperties: List<String>,
    val referenceStandard: String?,
    val verificationRequired: Boolean,
    val evidenceSources: List<String>
) {
    companion object {
        fun fromJson(obj: JSONObject): ReusePathway {
            fun jsonArrayToList(arr: JSONArray?): List<String> {
                if (arr == null) return emptyList()
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) list.add(arr.getString(i))
                return list
            }
            return ReusePathway(
                id = obj.optString("id", ""),
                buyerIndustry = obj.optString("buyer_industry", ""),
                application = obj.optString("application", ""),
                relevantProperties = jsonArrayToList(obj.optJSONArray("relevant_properties")),
                referenceStandard = if (obj.has("reference_standard") && !obj.isNull("reference_standard")) obj.getString("reference_standard") else null,
                verificationRequired = obj.optBoolean("verification_required", true),
                evidenceSources = jsonArrayToList(obj.optJSONArray("evidence_sources"))
            )
        }
    }
}

/**
 * Server health information.
 */
data class ServerHealth(
    val isOnline: Boolean,
    val status: String = "offline",
    val project: String = "ReLoop",
    val database: String = "Supabase PostgreSQL",
    val latencyMs: Long = 0,
    val errorMessage: String? = null
)
