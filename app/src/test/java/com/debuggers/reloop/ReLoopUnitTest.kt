package com.debuggers.reloop

import com.debuggers.reloop.data.model.*
import com.debuggers.reloop.data.repository.MaterialRepository
import org.junit.Assert.*
import org.junit.Test

class ReLoopUnitTest {

    @Test
    fun testMaterialItemProperties() {
        val item = MaterialItem(
            id = "test-uuid-1234",
            sellerName = "Rajpura Thermal Hub",
            materialName = "Fly Ash",
            sourceIndustry = "Thermal Power Generation",
            quantityTonnes = 500.0,
            city = "Rajpura",
            state = "Punjab",
            latitude = 30.478,
            longitude = 76.593,
            properties = mapOf(
                "grade" to "Class F",
                "price_per_tonne" to 450.0,
                "unit" to "tonnes",
                "pricing_basis" to "Ex-Plant Silo"
            ),
            availableDocuments = listOf("Material Test Report"),
            isSynthetic = false
        )

        assertEquals("test-uuid-1234", item.id)
        assertEquals("Rajpura Thermal Hub", item.sellerName)
        assertEquals("Fly Ash", item.materialName)
        assertEquals("Minerals & By-Products", item.category)
        assertEquals("500 tonnes", item.displayQuantity)
        assertEquals("Rajpura, Punjab", item.displayLocation)
        assertEquals("Class F", item.grade)
        assertEquals(450.0, item.pricePerTon)
        assertEquals("₹450 / tonne", item.priceDisplay)
        assertEquals("Ex-Plant Silo", item.pricingBasis)
        assertTrue(item.isVerifiedListing)
        assertEquals(1, item.availableDocuments.size)
    }

    @Test
    fun testUnpricedMaterialShowsRequestAQuote() {
        val item = MaterialItem(
            id = "test-2",
            sellerName = "Foundry Works",
            materialName = "Used Foundry Sand",
            sourceIndustry = "Foundry & Casting",
            quantityTonnes = 100.0,
            city = "Ludhiana",
            state = "Punjab",
            latitude = 30.9,
            longitude = 75.8,
            properties = emptyMap(),
            isSynthetic = true
        )

        assertEquals("Construction", item.category)
        assertEquals("Request a quote", item.priceDisplay)
        assertFalse(item.isVerifiedListing)
    }

    @Test
    fun testCategoryResolution() {
        val metalItem = MaterialItem(
            id = "m1", sellerName = "S", materialName = "Steel Melting Scrap",
            sourceIndustry = "Steel Metallurgy", quantityTonnes = 10.0,
            city = "C", state = "S", latitude = 0.0, longitude = 0.0
        )
        assertEquals("Metals", metalItem.category)

        val plasticItem = MaterialItem(
            id = "p1", sellerName = "S", materialName = "Recycled HDPE Regrind",
            sourceIndustry = "Plastics", quantityTonnes = 10.0,
            city = "C", state = "S", latitude = 0.0, longitude = 0.0
        )
        assertEquals("Plastics", plasticItem.category)

        val paperItem = MaterialItem(
            id = "pa1", sellerName = "S", materialName = "OCC Baled Cardboard",
            sourceIndustry = "Packaging", quantityTonnes = 10.0,
            city = "C", state = "S", latitude = 0.0, longitude = 0.0
        )
        assertEquals("Paper", paperItem.category)

        val chemicalItem = MaterialItem(
            id = "ch1", sellerName = "S", materialName = "Spent Sulfuric Acid",
            sourceIndustry = "Chemical Processing", quantityTonnes = 20.0,
            city = "C", state = "S", latitude = 0.0, longitude = 0.0
        )
        assertEquals("Chemicals", chemicalItem.category)
    }

    @Test
    fun testSubmitQuoteRequestAddsToRepository() {
        val item = MaterialItem(
            id = "mat-100",
            sellerName = "Supplier Co",
            materialName = "Fly Ash",
            sourceIndustry = "Thermal",
            quantityTonnes = 500.0,
            city = "Patiala",
            state = "Punjab",
            latitude = 30.34,
            longitude = 76.38
        )

        val rfq = MaterialRepository.submitQuoteRequest(
            material = item,
            buyerName = "Cement Works",
            buyerContact = "test@cement.com",
            quantityTonnes = 200.0,
            deliveryCity = "Ambala",
            message = "Need urgent delivery"
        )

        assertNotNull(rfq)
        assertEquals("mat-100", rfq.materialId)
        assertEquals(200.0, rfq.requestedQuantityTonnes, 0.01)
        assertTrue(MaterialRepository.quoteRequests.value.any { it.id == rfq.id })
    }

    @Test
    fun testRequirementItemModel() {
        val req = RequirementItem(
            id = "req-1",
            buyerName = "Eco Concrete",
            buyerIndustry = "Construction",
            requiredMaterial = "Fly Ash",
            requiredQuantityTonnes = 300.0,
            city = "Bathinda",
            state = "Punjab",
            latitude = 30.2,
            longitude = 74.9
        )

        assertEquals("300 tonnes", req.displayQuantity)
        assertEquals("Bathinda, Punjab", req.displayLocation)
    }
}
