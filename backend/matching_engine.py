from typing import Any, Dict, List, Tuple
import math

from .geo_matching import haversine_distance, load_json


def evaluate_properties(
    seller_props: Dict[str, Any],
    buyer_req_props: Dict[str, Any],
    pathway_key_props: List[str],
) -> Tuple[bool, List[str], List[str], List[str], float]:
    """
    Compare seller-declared property values with buyer-declared rules.

    A failed mandatory buyer rule rejects the candidate.
    Missing properties indicate incomplete verification, not proven incompatibility.
    """
    passed_reasons: List[str] = []
    failed_reasons: List[str] = []
    unverified_props: List[str] = []

    seller_norm = {
        str(k).strip().lower(): v
        for k, v in (seller_props or {}).items()
    }
    buyer_norm = {
        str(k).strip().lower(): v
        for k, v in (buyer_req_props or {}).items()
    }

    verified_count = 0
    total_required = len(buyer_norm)

    for prop_name, rule in buyer_norm.items():
        if prop_name not in seller_norm or seller_norm[prop_name] is None:
            unverified_props.append(
                f"{prop_name} (required by buyer, not declared by seller)"
            )
            continue

        seller_val = seller_norm[prop_name]

        if isinstance(rule, dict):
            try:
                numeric_val = float(seller_val)
                if not math.isfinite(numeric_val):
                    raise ValueError("Invalid numeric property value")
            except (TypeError, ValueError):
                unverified_props.append(
                    f"{prop_name} (seller value is not a valid number)"
                )
                continue

            try:
                min_val = (
                    float(rule["min"])
                    if rule.get("min") is not None
                    else None
                )
                max_val = (
                    float(rule["max"])
                    if rule.get("max") is not None
                    else None
                )

                if min_val is not None and not math.isfinite(min_val):
                    raise ValueError("Invalid minimum")
                if max_val is not None and not math.isfinite(max_val):
                    raise ValueError("Invalid maximum")
            except (TypeError, ValueError):
                unverified_props.append(
                    f"{prop_name} (buyer rule is invalid)"
                )
                continue

            if min_val is not None and numeric_val < min_val:
                failed_reasons.append(
                    f"Declared {prop_name} ({numeric_val}) is below buyer minimum ({min_val})"
                )
                continue

            if max_val is not None and numeric_val > max_val:
                failed_reasons.append(
                    f"Declared {prop_name} ({numeric_val}) exceeds buyer maximum ({max_val})"
                )
                continue

            verified_count += 1
            passed_reasons.append(
                f"Declared {prop_name} ({numeric_val}) satisfies buyer requirement"
            )

        else:
            if str(seller_val).strip().lower() == str(rule).strip().lower():
                verified_count += 1
                passed_reasons.append(
                    f"Declared {prop_name} ('{seller_val}') matches buyer requirement"
                )
            else:
                failed_reasons.append(
                    f"Declared {prop_name} ('{seller_val}') does not match buyer requirement ('{rule}')"
                )

    # Avoid duplicate warnings for properties listed in both the
    # buyer's mandatory requirements and the research pathway.
    already_reported = set()

    for warning in unverified_props:
        property_name = warning.split(" (", 1)[0]
        already_reported.add(property_name)

    for key_prop in pathway_key_props or []:
        key_norm = str(key_prop).strip().lower()

        if key_norm not in seller_norm and key_norm not in already_reported:
            unverified_props.append(
                f"{key_norm} (relevant to pathway, not declared by seller)"
            )
            already_reported.add(key_norm)

    if failed_reasons:
        return False, passed_reasons, failed_reasons, unverified_props, 0.0

    if total_required > 0:
        ratio = verified_count / total_required
        property_points = round(15.0 + 20.0 * ratio, 1)
    else:
        property_points = 25.0

    return True, passed_reasons, failed_reasons, unverified_props, property_points


def evaluate_documents(
    seller_docs: List[str],
    buyer_req_docs: List[str],
) -> Tuple[List[str], List[str], float]:
    """
    Compare seller-declared document names with buyer requirements.

    Presence in available_documents is only a declaration,
    not evidence of independent document verification.
    """
    seller_map = {
        str(d).strip().lower(): str(d).strip()
        for d in (seller_docs or [])
    }

    matched_docs: List[str] = []
    missing_docs: List[str] = []

    for req_doc in buyer_req_docs or []:
        req_clean = str(req_doc).strip()

        if req_clean.lower() in seller_map:
            matched_docs.append(req_clean)
        else:
            missing_docs.append(req_clean)

    total_required = len(buyer_req_docs or [])

    if total_required > 0:
        doc_points = round(
            15.0 * len(matched_docs) / total_required,
            1,
        )
    else:
        doc_points = 15.0 if seller_map else 7.5

    return matched_docs, missing_docs, doc_points


def match_material_with_requirements(
    material: Dict[str, Any],
    requirements: List[Dict[str, Any]],
    max_distance_km: float = 300.0,
) -> Dict[str, Any]:
    """
    Match stored seller materials against stored buyer requirements.

    Hard rejection conditions:
    - Material mismatch
    - Missing documented industrial reuse pathway
    - Distance exceeds search radius
    - Declared property fails a mandatory buyer limit

    Other conditions:
    - Missing properties/documents -> pending verification
    - Insufficient quantity -> partial supply opportunity

    No output constitutes safety or regulatory certification.
    """
    pathways_data = load_json("reuse_pathways.json")
    supported_material = pathways_data["material"]["name"]
    pathways = pathways_data["reuse_pathways"]

    seller_material = str(material.get("material_name", "")).strip()
    seller_qty = float(material.get("quantity_tonnes", 0))
    seller_lat = float(material.get("latitude", 0))
    seller_lon = float(material.get("longitude", 0))

    valid_matches: List[Dict[str, Any]] = []
    rejected_candidates: List[Dict[str, Any]] = []

    if seller_material.lower() != supported_material.lower():
        return {
            "material_supported": False,
            "matches": [],
            "rejected_candidates": [
                {
                    "reason": (
                        f"Material '{seller_material}' has no documented "
                        "reuse pathway in the current dataset."
                    )
                }
            ],
        }

    for req in requirements or []:
        buyer_id = req.get("id")
        buyer_name = req.get("buyer_name", "Unknown Buyer")
        buyer_industry = str(req.get("buyer_industry", "")).strip()
        req_material = str(req.get("required_material", "")).strip()
        req_qty = float(req.get("required_quantity_tonnes", 0))
        buyer_lat = float(req.get("latitude", 0))
        buyer_lon = float(req.get("longitude", 0))

        # 1. Mandatory material check
        if req_material.lower() != seller_material.lower():
            rejected_candidates.append(
                {
                    "requirement_id": buyer_id,
                    "buyer_name": buyer_name,
                    "match_status": "rejected",
                    "rejection_reasons": [
                        f"Material mismatch: buyer requires '{req_material}', seller offers '{seller_material}'."
                    ],
                }
            )
            continue

        # 2. Documented pathway check
        pathway = next(
            (
                p for p in pathways
                if p["buyer_industry"].strip().lower()
                == buyer_industry.lower()
            ),
            None,
        )

        if not pathway:
            rejected_candidates.append(
                {
                    "requirement_id": buyer_id,
                    "buyer_name": buyer_name,
                    "match_status": "rejected",
                    "rejection_reasons": [
                        f"No documented fly ash reuse pathway for '{buyer_industry}'."
                    ],
                }
            )
            continue

        # 3. Geographic radius check
        distance_km = haversine_distance(
            seller_lat,
            seller_lon,
            buyer_lat,
            buyer_lon,
        )

        if distance_km > max_distance_km:
            rejected_candidates.append(
                {
                    "requirement_id": buyer_id,
                    "buyer_name": buyer_name,
                    "match_status": "rejected",
                    "distance_km": distance_km,
                    "rejection_reasons": [
                        f"Distance {distance_km} km exceeds search radius {max_distance_km} km."
                    ],
                }
            )
            continue

        # 4. Buyer-declared property constraints
        (
            properties_compatible,
            passed_properties,
            failed_properties,
            unverified_properties,
            property_points,
        ) = evaluate_properties(
            material.get("material_properties") or {},
            req.get("required_properties") or {},
            pathway.get("relevant_properties") or [],
        )

        if not properties_compatible:
            rejected_candidates.append(
                {
                    "requirement_id": buyer_id,
                    "buyer_name": buyer_name,
                    "match_status": "rejected",
                    "distance_km": distance_km,
                    "rejection_reasons": failed_properties,
                }
            )
            continue

        # 5. Quantity evaluation
        quantity_sufficient = seller_qty >= req_qty

        if quantity_sufficient:
            quantity_points = 25.0
            quantity_explanation = (
                f"Seller quantity ({seller_qty} t) covers buyer demand ({req_qty} t)."
            )
        else:
            quantity_points = round(
                25.0 * seller_qty / req_qty, 1
            ) if req_qty > 0 else 0.0

            quantity_explanation = (
                f"Partial supply: {seller_qty} t available against {req_qty} t requested."
            )

        # 6. Geographic proximity score
        proximity_points = round(
            25.0 * max(
                0.0,
                1.0 - distance_km / max_distance_km,
            ),
            1,
        )

        # 7. Documentation comparison
        matched_docs, missing_docs, document_points = evaluate_documents(
            material.get("available_documents") or [],
            req.get("required_documents") or [],
        )

        # 8. Preliminary scoring
        compatibility_score = round(
            property_points
            + quantity_points
            + proximity_points
            + document_points,
            1,
        )

        # 9. Clear match status
        # Buyer-mandatory property gaps and document gaps mean the
        # candidate needs verification before further evaluation.
        missing_buyer_properties = [
            p for p in (req.get("required_properties") or {}).keys()
            if str(p).strip().lower()
            not in {
                str(k).strip().lower()
                for k in (material.get("material_properties") or {}).keys()
            }
        ]

        if missing_buyer_properties or missing_docs:
            match_status = "pending_verification"
        elif not quantity_sufficient:
            match_status = "partial_supply"
        else:
            match_status = "eligible_for_review"

        reference_standard = pathway.get("reference_standard")

        explanations = [
            f"Research-supported pathway: {buyer_industry} → {pathway['application']}.",
            (
                f"Reference standard: {reference_standard or 'Not specified'}. "
                "Applicability and current requirements require verification."
            ),
            f"Straight-line distance: {distance_km} km.",
            quantity_explanation,
        ]

        explanations.extend(passed_properties)

        if matched_docs:
            explanations.append(
                "Seller declares: "
                + ", ".join(matched_docs)
                + ". Document authenticity is not verified."
            )

        if missing_docs:
            explanations.append(
                "Missing requested documents: "
                + ", ".join(missing_docs)
            )

        if unverified_properties:
            explanations.append(
                "Additional pathway properties require verification."
            )

        valid_matches.append(
            {
                "requirement_id": buyer_id,
                "buyer_name": buyer_name,
                "buyer_industry": buyer_industry,
                "city": req.get("city"),
                "state": req.get("state"),
                "latitude": buyer_lat,
                "longitude": buyer_lon,
                "distance_km": distance_km,
                "required_quantity_tonnes": req_qty,
                "quantity_sufficient": quantity_sufficient,
                "pathway_id": pathway["id"],
                "recommended_application": pathway["application"],
                "standard_reference": reference_standard,
                "compatibility_score": compatibility_score,
                "score_breakdown": {
                    "property_compatibility_points": property_points,
                    "quantity_points": quantity_points,
                    "proximity_points": proximity_points,
                    "documentation_points": document_points,
                    "max_possible_points": 100.0,
                },
                "matched_documents": matched_docs,
                "missing_documents": missing_docs,
                "unverified_properties": unverified_properties,
                "missing_buyer_properties": missing_buyer_properties,
                "explanations": explanations,
                "verification_required": True,
                "match_status": match_status,
                "is_synthetic": bool(
                    material.get("is_synthetic")
                    or req.get("is_synthetic")
                ),
            }
        )

    # Full supply first, then higher score, then shorter distance.
    valid_matches.sort(
        key=lambda m: (
            not m["quantity_sufficient"],
            -m["compatibility_score"],
            m["distance_km"],
        )
    )

    return {
        "material_supported": True,
        "matches": valid_matches,
        "rejected_candidates": rejected_candidates,
    }