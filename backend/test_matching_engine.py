import copy
import unittest

from backend.matching_engine import (
    evaluate_properties,
    match_material_with_requirements,
)


class TestReLoopMatchingEngine(unittest.TestCase):

    def setUp(self):

        self.seller = {
            "id": "seller-demo-001",
            "seller_name": "Rajpura Thermal Hub (Demo)",
            "material_name": "Fly Ash",
            "source_industry": "Thermal Power Generation",
            "quantity_tonnes": 500,
            "city": "Rajpura",
            "state": "Punjab",
            "latitude": 30.478,
            "longitude": 76.593,
            "material_properties": {
                "moisture": 2.5,
                "fineness": 320,
            },
            "available_documents": [
                "Material Test Report",
            ],
            "is_synthetic": True,
        }

        self.buyer = {
            "id": "buyer-demo-001",
            "buyer_name": "Ambala Green Cement Works (Demo)",
            "buyer_industry": "Cement Manufacturing",
            "required_material": "Fly Ash",
            "required_quantity_tonnes": 300,
            "city": "Ambala",
            "state": "Haryana",
            "latitude": 30.3782,
            "longitude": 76.7767,
            "required_properties": {
                "moisture": {"max": 5},
            },
            "required_documents": [
                "Material Test Report",
            ],
            "is_synthetic": True,
        }

    def match(self, seller=None, buyer=None):
        return match_material_with_requirements(
            seller if seller is not None else self.seller,
            [buyer if buyer is not None else self.buyer],
        )

    # 1. Basic valid match

    def test_valid_match(self):

        result = self.match()

        self.assertEqual(len(result["matches"]), 1)

        match = result["matches"][0]

        self.assertEqual(match["distance_km"], 20.82)

        self.assertEqual(
            match["standard_reference"],
            "IS 3812 Part 1",
        )

        self.assertEqual(
            match["match_status"],
            "eligible_for_review",
        )

    # 2. Mandatory moisture rejection

    def test_failed_moisture_limit(self):

        seller = copy.deepcopy(self.seller)

        seller["material_properties"]["moisture"] = 8

        result = self.match(seller=seller)

        self.assertEqual(len(result["matches"]), 0)

        self.assertEqual(
            len(result["rejected_candidates"]),
            1,
        )

        self.assertIn(
            "exceeds buyer maximum",
            str(
                result["rejected_candidates"][0][
                    "rejection_reasons"
                ]
            ),
        )

    # 3. Unsupported material

    def test_unsupported_material(self):

        seller = copy.deepcopy(self.seller)

        seller["material_name"] = "Steel Slag"

        result = self.match(seller=seller)

        self.assertFalse(result["material_supported"])

        self.assertEqual(len(result["matches"]), 0)

    # 4. Missing property evaluation

    def test_missing_property_is_unverified(self):

        result = evaluate_properties(
            {},
            {"moisture": {"max": 5}},
            ["chemical_composition", "moisture"],
        )

        self.assertTrue(result[0])

        self.assertEqual(len(result[2]), 0)

        self.assertGreater(len(result[3]), 0)

    # 5. Partial quantity supply

    def test_quantity_shortfall(self):

        seller = copy.deepcopy(self.seller)

        seller["quantity_tonnes"] = 100

        result = self.match(seller=seller)

        self.assertEqual(len(result["matches"]), 1)

        match = result["matches"][0]

        self.assertFalse(match["quantity_sufficient"])

        self.assertEqual(
            match["quantity_status"],
            "partial_supply",
        )

        self.assertAlmostEqual(
            match["quantity_coverage_percent"],
            33.3,
            places=1,
        )

    # 6. Missing document

    def test_missing_document(self):

        seller = copy.deepcopy(self.seller)

        seller["available_documents"] = []

        result = self.match(seller=seller)

        self.assertEqual(len(result["matches"]), 1)

        match = result["matches"][0]

        self.assertIn(
            "Material Test Report",
            match["missing_documents"],
        )

        self.assertEqual(
            match["verification_status"],
            "pending_verification",
        )

    # 7. Opportunity Score

    def test_opportunity_score(self):

        result = self.match()

        match = result["matches"][0]

        self.assertIn("opportunity_score", match)

        self.assertGreaterEqual(
            match["opportunity_score"],
            0,
        )

        self.assertLessEqual(
            match["opportunity_score"],
            100,
        )

        self.assertEqual(
            match["opportunity_score"],
            match["compatibility_score"],
        )

    # 8. Verification Status

    def test_verification_status(self):

        result = self.match()

        match = result["matches"][0]

        self.assertEqual(
            match["verification_status"],
            "eligible_for_review",
        )

        self.assertTrue(
            match["verification_required"]
        )

    # 9. Missing mandatory buyer property

    def test_missing_buyer_property_pending(self):

        seller = copy.deepcopy(self.seller)

        seller["material_properties"].pop("moisture")

        result = self.match(seller=seller)

        self.assertEqual(len(result["matches"]), 1)

        match = result["matches"][0]

        self.assertEqual(
            match["verification_status"],
            "pending_verification",
        )

        self.assertIn(
            "moisture",
            match["missing_buyer_properties"],
        )

    # 10. Invalid numeric property

    def test_invalid_numeric_property_pending(self):

        seller = copy.deepcopy(self.seller)

        seller["material_properties"]["moisture"] = "unknown"

        result = self.match(seller=seller)

        self.assertEqual(len(result["matches"]), 1)

        match = result["matches"][0]

        self.assertEqual(
            match["verification_status"],
            "pending_verification",
        )

    # 11. Research evidence

    def test_research_evidence(self):

        result = self.match()

        match = result["matches"][0]

        self.assertIn(
            "research_evidence",
            match,
        )

        self.assertIsInstance(
            match["research_evidence"],
            list,
        )

        self.assertGreater(
            len(match["research_evidence"]),
            0,
        )

    # 12. Straight-line geographic distance

    def test_distance_type(self):

        result = self.match()

        match = result["matches"][0]

        self.assertEqual(
            match["distance_type"],
            "straight_line_haversine",
        )

    # 13. Hard rejection beyond distance

    def test_distance_limit(self):

        result = match_material_with_requirements(
            self.seller,
            [self.buyer],
            max_distance_km=10,
        )

        self.assertEqual(len(result["matches"]), 0)

        self.assertEqual(
            len(result["rejected_candidates"]),
            1,
        )

    # 14. Partial supply with missing evidence

    def test_partial_supply_and_pending_verification(self):

        seller = copy.deepcopy(self.seller)

        seller["quantity_tonnes"] = 100

        seller["available_documents"] = []

        result = self.match(seller=seller)

        match = result["matches"][0]

        self.assertEqual(
            match["quantity_status"],
            "partial_supply",
        )

        self.assertEqual(
            match["verification_status"],
            "pending_verification",
        )

        self.assertEqual(
            match["match_status"],
            "pending_verification",
        )

    # 15. Synthetic demo labeling

    def test_synthetic_label(self):

        result = self.match()

        match = result["matches"][0]

        self.assertTrue(match["is_synthetic"])

        self.assertIn("disclaimer", match)


if __name__ == "__main__":
    unittest.main()