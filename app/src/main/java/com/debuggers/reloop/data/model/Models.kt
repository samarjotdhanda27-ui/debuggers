package com.debuggers.reloop.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Material item representing an industrial B2B listing in ReLoop marketplace.
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
        get() = "${quantityTonnes.toInt().takeIf { it.toDouble() == quantityTonnes } ?: quantityTonnes} $unit"

    val displayLocation: String
        get() = if (city.isNotEmpty() && state.isNotEmpty()) "$city, $state" else city.ifEmpty { state }.ifEmpty { "India" }

    val unit: String
        get() = properties["unit"]?.toString() ?: "tonnes"

    val grade: String?
        get() = properties["grade"]?.toString()
            ?: properties["material_grade"]?.toString()
            ?: properties["fineness"]?.let { "Fineness: $it m²/kg" }

    val condition: String?
        get() = properties["condition"]?.toString()
            ?: properties["moisture"]?.let { "Moisture: $it%" }

    val pricePerTon: Double?
        get() = (properties["price_per_tonne"] as? Number)?.toDouble()
            ?: (properties["price"] as? Number)?.toDouble()

    val pricingBasis: String
        get() = properties["pricing_basis"]?.toString() ?: "Ex-works / In-situ"

    val priceDisplay: String
        get() = pricePerTon?.let { "₹${String.format("%,.0f", it)} / tonne" } ?: "Request a quote"

    val availabilityStatus: String
        get() = properties["availability"]?.toString() ?: "Available for Dispatch"

    val description: String
        get() = properties["description"]?.toString()
            ?: "Industrial secondary material available for circular reuse and remanufacturing. Material specifications subject to batch inspection."

    val category: String
        get() {
            val name = materialName.lowercase()
            val ind = sourceIndustry.lowercase()
            return when {
                name.contains("metal") || name.contains("scrap") || name.contains("steel") || name.contains("iron") || name.contains("copper") || name.contains("aluminum") || ind.contains("steel") -> "Metals"
                name.contains("plastic") || name.contains("hdpe") || name.contains("pet") || name.contains("pvc") || name.contains("polymer") -> "Plastics"
                name.contains("concrete") || name.contains("aggregate") || name.contains("sand") || name.contains("brick") || ind.contains("construction") || ind.contains("cement") -> "Construction"
                name.contains("chemical") || name.contains("acid") || name.contains("solvent") || name.contains("water") || name.contains("effluent") || ind.contains("chemical") -> "Chemicals"
                name.contains("paper") || name.contains("cardboard") || name.contains("occ") || name.contains("pulp") -> "Paper"
                name.contains("fly ash") || name.contains("slag") || name.contains("mineral") || ind.contains("thermal") || ind.contains("foundry") -> "Minerals & By-Products"
                else -> sourceIndustry.ifEmpty { "Industrial Materials" }
            }
        }

    val symbol: String
        get() = when {
            materialName.contains("Fly", ignoreCase = true) -> "FA"
            materialName.contains("Foundry", ignoreCase = true) || materialName.contains("Sand", ignoreCase = true) -> "FS"
            materialName.contains("Scrap", ignoreCase = true) || materialName.contains("Metal", ignoreCase = true) -> "MS"
            materialName.contains("HDPE", ignoreCase = true) || materialName.contains("Plastic", ignoreCase = true) -> "PL"
            materialName.contains("Acid", ignoreCase = true) || materialName.contains("Chemical", ignoreCase = true) -> "CH"
            materialName.contains("Paper", ignoreCase = true) || materialName.contains("OCC", ignoreCase = true) -> "PA"
            materialName.contains("Water", ignoreCase = true) -> "PW"
            else -> materialName.take(2).uppercase()
        }

    val isVerifiedListing: Boolean
        get() = !isSynthetic

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
                sellerName = obj.optString("seller_name", "Industrial Supplier"),
                materialName = obj.optString("material_name", "Industrial Material"),
                sourceIndustry = obj.optString("source_industry", "Industrial Processing"),
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
 * Requirement item representing buyer demand in ReLoop marketplace.
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
        get() = if (city.isNotEmpty() && state.isNotEmpty()) "$city, $state" else city.ifEmpty { state }.ifEmpty { "India" }

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
                buyerName = obj.optString("buyer_name", "Procuring Facility"),
                buyerIndustry = obj.optString("buyer_industry", "Manufacturing"),
                requiredMaterial = obj.optString("required_material", "Raw Feedstock"),
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
 * Real B2B Enquiry / Request for Quote (RFQ) record.
 */
data class EnquiryQuoteRequest(
    val id: String,
    val materialId: String,
    val materialName: String,
    val sellerName: String,
    val buyerName: String,
    val buyerContact: String,
    val requestedQuantityTonnes: Double,
    val deliveryCity: String,
    val message: String,
    val timestamp: String,
    val status: String = "RFQ Submitted"
)

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
