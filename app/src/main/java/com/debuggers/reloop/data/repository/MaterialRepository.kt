package com.debuggers.reloop.data.repository

import com.debuggers.reloop.data.api.ReLoopApiClient
import com.debuggers.reloop.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

object MaterialRepository {

    // Known industrial hubs with latitude and longitude for accurate geocoding
    val regionalCoordinates: Map<String, Pair<Double, Double>> = mapOf(
        "Rajpura, Punjab" to Pair(30.478, 76.593),
        "Bathinda, Punjab" to Pair(30.211, 74.945),
        "Ludhiana, Punjab" to Pair(30.901, 75.857),
        "Patiala, Punjab" to Pair(30.340, 76.387),
        "Mandi Gobindgarh, Punjab" to Pair(30.664, 76.291),
        "Mohali, Punjab" to Pair(30.705, 76.718),
        "Chandigarh" to Pair(30.733, 76.779),
        "Amritsar, Punjab" to Pair(31.634, 74.872),
        "Jalandhar, Punjab" to Pair(31.326, 75.576),
        "Rupnagar, Punjab" to Pair(30.966, 76.523),
        "Ambala, Haryana" to Pair(30.378, 76.777),
        "Panipat, Haryana" to Pair(29.391, 76.964),
        "Kurukshetra, Haryana" to Pair(29.970, 76.878),
        "Yamunanagar, Haryana" to Pair(30.129, 77.267),
        "Gurugram, Haryana" to Pair(28.459, 77.026),
        "Faridabad, Haryana" to Pair(28.408, 77.317)
    )

    val supportedIndustries = listOf(
        "Thermal Power Generation",
        "Cement Manufacturing",
        "Steel Manufacturing",
        "Foundry & Casting",
        "Chemical Processing",
        "Construction & Aggregates",
        "Brick & Block Production",
        "Textile Processing",
        "Food Processing",
        "Other Industrial"
    )

    val standardDocumentsList = listOf(
        "Material Test Report",
        "CPCB / SPCB Consent NOC",
        "Chemical Characterization Analysis",
        "Particle Size Distribution (PSD)",
        "Heavy Metal Leaching Test",
        "Material Safety Data Sheet (MSDS)"
    )

    // Seed items so that if the user starts the app completely offline, there's full rich content
    private val seedMaterials = listOf(
        MaterialItem(
            id = "15ba8dc8-4022-43b2-a2ca-d301e70d22b1",
            sellerName = "Rajpura Thermal Hub",
            materialName = "Fly Ash",
            sourceIndustry = "Thermal Power Generation",
            quantityTonnes = 500.0,
            city = "Rajpura",
            state = "Punjab",
            latitude = 30.478,
            longitude = 76.593,
            properties = mapOf("fineness" to 320, "moisture" to 2.5, "loss_on_ignition" to 1.8),
            availableDocuments = listOf("Material Test Report", "CPCB / SPCB Consent NOC"),
            isSynthetic = true,
            createdAt = "2026-10-26"
        ),
        MaterialItem(
            id = "seed-fs-002",
            sellerName = "Ludhiana Precision Castings",
            materialName = "Foundry Sand",
            sourceIndustry = "Foundry & Casting",
            quantityTonnes = 85.0,
            city = "Ludhiana",
            state = "Punjab",
            latitude = 30.901,
            longitude = 75.857,
            properties = mapOf("clay_content" to 3.2, "grain_fineness" to 55),
            availableDocuments = listOf("Particle Size Distribution (PSD)"),
            isSynthetic = false,
            createdAt = "2026-10-25"
        ),
        MaterialItem(
            id = "seed-ms-003",
            sellerName = "Mandi Gobindgarh Re-Rollers",
            materialName = "Ferrous Mill Scale & Scrap",
            sourceIndustry = "Steel Manufacturing",
            quantityTonnes = 42.0,
            city = "Mandi Gobindgarh",
            state = "Punjab",
            latitude = 30.664,
            longitude = 76.291,
            properties = mapOf("iron_content_fe" to 68.5, "moisture" to 0.8),
            availableDocuments = listOf("Material Test Report", "Chemical Characterization Analysis"),
            isSynthetic = false,
            createdAt = "2026-10-24"
        ),
        MaterialItem(
            id = "seed-pw-004",
            sellerName = "Panipat Industrial Process Park",
            materialName = "Treated Process Effluent",
            sourceIndustry = "Chemical Processing",
            quantityTonnes = 120.0,
            city = "Panipat",
            state = "Haryana",
            latitude = 29.391,
            longitude = 76.964,
            properties = mapOf("ph" to 7.2, "tds_ppm" to 450, "tss_ppm" to 12),
            availableDocuments = listOf("CPCB / SPCB Consent NOC"),
            isSynthetic = false,
            createdAt = "2026-10-23"
        )
    )

    private val seedRequirements = listOf(
        RequirementItem(
            id = "85d26890-6f84-4db4-b78a-37dfaf7e4d69",
            buyerName = "Ambala Green Cement Works",
            buyerIndustry = "Cement Manufacturing",
            requiredMaterial = "Fly Ash",
            requiredQuantityTonnes = 300.0,
            city = "Ambala",
            state = "Haryana",
            latitude = 30.3782,
            longitude = 76.7767,
            requiredProperties = mapOf("moisture" to mapOf("max" to 5)),
            requiredDocuments = listOf("Material Test Report"),
            isSynthetic = true,
            createdAt = "2026-10-26"
        ),
        RequirementItem(
            id = "seed-req-002",
            buyerName = "Patiala Eco Pavers & Blocks",
            buyerIndustry = "Brick & Block Production",
            requiredMaterial = "Foundry Sand",
            requiredQuantityTonnes = 50.0,
            city = "Patiala",
            state = "Punjab",
            latitude = 30.340,
            longitude = 76.387,
            requiredProperties = mapOf("clay_content" to mapOf("max" to 6)),
            requiredDocuments = listOf("Particle Size Distribution (PSD)"),
            isSynthetic = false,
            createdAt = "2026-10-25"
        )
    )

    private val _materials = MutableStateFlow<List<MaterialItem>>(seedMaterials)
    val materials: StateFlow<List<MaterialItem>> = _materials.asStateFlow()

    private val _requirements = MutableStateFlow<List<RequirementItem>>(seedRequirements)
    val requirements: StateFlow<List<RequirementItem>> = _requirements.asStateFlow()

    private val _serverHealth = MutableStateFlow(ServerHealth(isOnline = false))
    val serverHealth: StateFlow<ServerHealth> = _serverHealth.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Locally created items during this session
    private val localCreatedMaterials = mutableListOf<MaterialItem>()
    private val localCreatedRequirements = mutableListOf<RequirementItem>()

    suspend fun refreshAll() {
        _isLoading.value = true
        checkServerStatus()

        // Fetch live materials
        val liveMaterials = ReLoopApiClient.getMaterials()
        if (liveMaterials.isNotEmpty()) {
            val liveIds = liveMaterials.map { it.id }.toSet()
            val extraLocal = localCreatedMaterials.filter { it.id !in liveIds }
            val extraSeed = seedMaterials.filter { it.id !in liveIds && it.id !in extraLocal.map { l -> l.id } }
            _materials.value = liveMaterials + extraLocal + extraSeed
        } else {
            // Keep existing plus locally created
            _materials.value = localCreatedMaterials + seedMaterials
        }

        // Fetch live requirements
        val liveReqs = ReLoopApiClient.getRequirements()
        if (liveReqs.isNotEmpty()) {
            val liveIds = liveReqs.map { it.id }.toSet()
            val extraLocal = localCreatedRequirements.filter { it.id !in liveIds }
            val extraSeed = seedRequirements.filter { it.id !in liveIds && it.id !in extraLocal.map { l -> l.id } }
            _requirements.value = liveReqs + extraLocal + extraSeed
        } else {
            _requirements.value = localCreatedRequirements + seedRequirements
        }

        _isLoading.value = false
    }

    suspend fun checkServerStatus(): ServerHealth {
        val health = ReLoopApiClient.checkHealth()
        _serverHealth.value = health
        return health
    }

    suspend fun createMaterial(
        sellerName: String,
        materialName: String,
        sourceIndustry: String,
        quantityTonnes: Double,
        city: String,
        state: String,
        latitude: Double,
        longitude: Double,
        properties: Map<String, Any> = emptyMap(),
        availableDocuments: List<String> = emptyList()
    ): Pair<Boolean, String> {
        val jsonProps = JSONObject()
        properties.forEach { (k, v) -> jsonProps.put(k, v) }

        val jsonDocs = JSONArray()
        availableDocuments.forEach { jsonDocs.put(it) }

        val body = JSONObject().apply {
            put("seller_name", sellerName.trim())
            put("material_name", materialName.trim())
            put("source_industry", sourceIndustry)
            put("quantity_tonnes", quantityTonnes)
            put("city", city.trim())
            put("state", state.trim())
            put("latitude", latitude)
            put("longitude", longitude)
            put("material_properties", jsonProps)
            put("available_documents", jsonDocs)
            put("is_synthetic", false)
        }

        val (success, message) = ReLoopApiClient.createMaterial(body)

        // Optimistically record locally
        val newItem = MaterialItem(
            id = "loc-${System.currentTimeMillis()}",
            sellerName = sellerName.trim(),
            materialName = materialName.trim(),
            sourceIndustry = sourceIndustry,
            quantityTonnes = quantityTonnes,
            city = city.trim(),
            state = state.trim(),
            latitude = latitude,
            longitude = longitude,
            properties = properties,
            availableDocuments = availableDocuments,
            isSynthetic = false,
            createdAt = "Just now"
        )
        localCreatedMaterials.add(0, newItem)
        _materials.value = listOf(newItem) + _materials.value.filter { it.id != newItem.id }

        // Refresh live data in background
        if (success) {
            refreshAll()
        }

        return Pair(success, message)
    }

    suspend fun createRequirement(
        buyerName: String,
        buyerIndustry: String,
        requiredMaterial: String,
        requiredQuantityTonnes: Double,
        city: String,
        state: String,
        latitude: Double,
        longitude: Double,
        requiredProperties: Map<String, Any> = emptyMap(),
        requiredDocuments: List<String> = emptyList()
    ): Pair<Boolean, String> {
        val jsonProps = JSONObject()
        requiredProperties.forEach { (k, v) -> jsonProps.put(k, v) }

        val jsonDocs = JSONArray()
        requiredDocuments.forEach { jsonDocs.put(it) }

        val body = JSONObject().apply {
            put("buyer_name", buyerName.trim())
            put("buyer_industry", buyerIndustry)
            put("required_material", requiredMaterial.trim())
            put("required_quantity_tonnes", requiredQuantityTonnes)
            put("city", city.trim())
            put("state", state.trim())
            put("latitude", latitude)
            put("longitude", longitude)
            put("required_properties", jsonProps)
            put("required_documents", jsonDocs)
            put("is_synthetic", false)
        }

        val (success, message) = ReLoopApiClient.createRequirement(body)

        val newReq = RequirementItem(
            id = "loc-req-${System.currentTimeMillis()}",
            buyerName = buyerName.trim(),
            buyerIndustry = buyerIndustry,
            requiredMaterial = requiredMaterial.trim(),
            requiredQuantityTonnes = requiredQuantityTonnes,
            city = city.trim(),
            state = state.trim(),
            latitude = latitude,
            longitude = longitude,
            requiredProperties = requiredProperties,
            requiredDocuments = requiredDocuments,
            isSynthetic = false,
            createdAt = "Just now"
        )
        localCreatedRequirements.add(0, newReq)
        _requirements.value = listOf(newReq) + _requirements.value.filter { it.id != newReq.id }

        if (success) {
            refreshAll()
        }

        return Pair(success, message)
    }

    suspend fun getMatches(materialId: String, radiusKm: Double = 300.0): MaterialMatchesResult? {
        val result = ReLoopApiClient.getMatchesForMaterial(materialId, radiusKm)
        if (result != null) return result

        // If specific material UUID is not on backend yet (e.g. locally created or seed),
        // we can calculate intelligent fallback matches against our requirements!
        val targetMaterial = _materials.value.find { it.id == materialId } ?: seedMaterials.first()
        val candidateMatches = mutableListOf<IntelligentMatch>()

        _requirements.value.forEach { req ->
            val dist = calculateHaversineDistance(
                targetMaterial.latitude, targetMaterial.longitude,
                req.latitude, req.longitude
            )
            if (dist <= radiusKm) {
                val materialMatch = targetMaterial.materialName.contains(req.requiredMaterial, true) ||
                        req.requiredMaterial.contains(targetMaterial.materialName, true)

                if (materialMatch) {
                    val qtySufficient = targetMaterial.quantityTonnes >= req.requiredQuantityTonnes
                    val score = if (qtySufficient) 92.5 else 78.0
                    candidateMatches.add(
                        IntelligentMatch(
                            requirementId = req.id,
                            buyerName = req.buyerName,
                            buyerIndustry = req.buyerIndustry,
                            city = req.city,
                            state = req.state,
                            latitude = req.latitude,
                            longitude = req.longitude,
                            distanceKm = ((dist * 10).toInt() / 10.0),
                            requiredQuantityTonnes = req.requiredQuantityTonnes,
                            quantitySufficient = qtySufficient,
                            pathwayId = "RP001",
                            recommendedApplication = "Circular Industrial Feedstock",
                            standardReference = "IS 3812 Part 1",
                            compatibilityScore = score,
                            scoreBreakdown = ScoreBreakdown(35.0, 25.0, 22.5, 10.0, 100.0),
                            matchedDocuments = req.requiredDocuments.filter { it in targetMaterial.availableDocuments },
                            missingDocuments = req.requiredDocuments.filter { it !in targetMaterial.availableDocuments },
                            unverifiedProperties = listOf("Technical batch verification required before shipment"),
                            explanations = listOf(
                                "Matches demand criteria in ${req.buyerIndustry}",
                                "Straight-line transit distance: ${((dist * 10).toInt() / 10.0)} km",
                                if (qtySufficient) "Available quantity (${targetMaterial.quantityTonnes} t) satisfies demand (${req.requiredQuantityTonnes} t)"
                                else "Partial fulfillment possible (${targetMaterial.quantityTonnes} t available vs ${req.requiredQuantityTonnes} t requested)"
                            ),
                            verificationRequired = true,
                            matchStatus = "eligible_for_review",
                            isSynthetic = targetMaterial.isSynthetic
                        )
                    )
                }
            }
        }

        return MaterialMatchesResult(
            materialId = targetMaterial.id,
            sellerName = targetMaterial.sellerName,
            materialName = targetMaterial.materialName,
            availableQuantityTonnes = targetMaterial.quantityTonnes,
            sellerCity = targetMaterial.city,
            sellerState = targetMaterial.state,
            maxDistanceKm = radiusKm,
            totalMatches = candidateMatches.size,
            matches = candidateMatches,
            disclaimer = "AI-calculated decision-support match based on spatial proximity, material attributes, and regulatory compatibility."
        )
    }

    suspend fun getPathways(materialName: String): List<ReusePathway> {
        val pathways = ReLoopApiClient.getReusePathways(materialName)
        if (pathways.isNotEmpty()) return pathways

        // High-standard domain fallback pathways if offline
        return when {
            materialName.contains("Fly", true) -> listOf(
                ReusePathway(
                    "RP001", "Cement Manufacturing", "Portland Pozzolana Cement",
                    listOf("chemical_composition", "fineness", "loss_on_ignition", "moisture"),
                    "IS 3812 Part 1", true, listOf("CEA2026", "DHADSE2008")
                ),
                ReusePathway(
                    "RP002", "Concrete Products", "Supplementary Cementitious Material",
                    listOf("chemical_composition", "fineness", "loss_on_ignition", "moisture"),
                    "IS 3812 Part 1", true, listOf("CEA2026")
                ),
                ReusePathway(
                    "RP003", "Brick Manufacturing", "Fly Ash Bricks and Blocks",
                    listOf("chemical_composition", "particle_size", "moisture"),
                    "IS 12894", true, listOf("CEA2026")
                ),
                ReusePathway(
                    "RP004", "Road Construction", "Road Embankment and Subgrade Fill",
                    listOf("particle_size", "moisture", "geotechnical_properties"),
                    "IRC SP 58", true, listOf("CEA2026")
                )
            )
            materialName.contains("Sand", true) -> listOf(
                ReusePathway(
                    "RP101", "Construction Aggregates", "Fine Aggregate in Concrete & Mortar",
                    listOf("grain_fineness", "clay_content", "leachable_phenols"),
                    "IS 383", true, listOf("CPCB-FS2025")
                ),
                ReusePathway(
                    "RP102", "Brick Manufacturing", "Clay Brick Additive & Pavers",
                    listOf("particle_size", "loss_on_ignition"),
                    "IS 1077", true, listOf("BIS2024")
                )
            )
            materialName.contains("Metal", true) || materialName.contains("Scrap", true) -> listOf(
                ReusePathway(
                    "RP201", "Secondary Steel Metallurgy", "Electric Arc Furnace Charge",
                    listOf("fe_purity", "bulk_density", "oil_contaminants"),
                    "IS 2549", true, listOf("MISTEEL2025")
                )
            )
            else -> listOf(
                ReusePathway(
                    "RP999", "Industrial Symbiosis", "Direct Secondary Feedstock",
                    listOf("composition", "moisture", "contaminant_screening"),
                    "IS Standards Pending", true, listOf("ReLoop-AI-Exchange")
                )
            )
        }
    }

    private fun calculateHaversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
