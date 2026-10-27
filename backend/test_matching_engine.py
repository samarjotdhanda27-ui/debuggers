import unittest

from backend.matching_engine import (
    evaluate_properties,
    match_material_with_requirements,
)


class TestReLoopMatchingEngine(unittest.TestCase):

    def setUp(self):
        self.seller = {
            "material_name": "Fly Ash",
            "quantity_tonnes": 500,
            "latitude": 30.478,
            "longitude": 76.593,
            "material_properties": {
                "moisture": 2.5,
                "fineness": 320,
            },
            "available_documents": ["Material Test Report"],
            "is_synthetic": True,
        }

        self.buyer = {
            "id": "TEST001",
            "buyer_name": "Ambala Demo Buyer",
            "buyer_industry": "Cement Manufacturing",
            "required_material": "Fly Ash",
            "required_quantity_tonnes": 300,
            "latitude": 30.3782,
            "longitude": 76.7767,
            "required_properties": {
                "moisture": {"max": 5}
            },
            "required_documents": ["Material Test Report"],
            "is_synthetic": True,
        }

    def test_valid_match(self):
        result = match_material_with_requirements(
            self.seller, [self.buyer]
        )

        self.assertEqual(len(result["matches"]), 1)
        self.assertEqual(result["matches"][0]["distance_km"], 20.82)
        self.assertEqual(
            result["matches"][0]["standard_reference"],
            "IS 3812 Part 1",
        )

    def test_failed_moisture_limit(self):
        seller = dict(self.seller)
        seller["material_properties"] = {"moisture": 8}

        result = match_material_with_requirements(
            seller, [self.buyer]
        )

        self.assertEqual(len(result["matches"]), 0)
        self.assertEqual(len(result["rejected_candidates"]), 1)

    def test_unsupported_material(self):
        seller = dict(self.seller)
        seller["material_name"] = "Steel Slag"

        result = match_material_with_requirements(
            seller, [self.buyer]
        )

        self.assertFalse(result["material_supported"])
        self.assertEqual(len(result["matches"]), 0)

    def test_missing_property_is_unverified(self):
        result = evaluate_properties(
            {},
            {"moisture": {"max": 5}},
            ["chemical_composition", "moisture"],
        )

        self.assertTrue(result[0])
        self.assertEqual(len(result[2]), 0)
        self.assertGreater(len(result[3]), 0)

    def test_quantity_shortfall(self):
        seller = dict(self.seller)
        seller["quantity_tonnes"] = 100

        result = match_material_with_requirements(
            seller, [self.buyer]
        )

        self.assertEqual(len(result["matches"]), 1)
        self.assertFalse(result["matches"][0]["quantity_sufficient"])

    def test_missing_document(self):
        seller = dict(self.seller)
        seller["available_documents"] = []

        result = match_material_with_requirements(
            seller, [self.buyer]
        )

        self.assertEqual(len(result["matches"]), 1)
        self.assertIn(
            "Material Test Report",
            result["matches"][0]["missing_documents"],
        )


if __name__ == "__main__":
    unittest.main()