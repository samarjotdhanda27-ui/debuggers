from typing import Any, Dict, List, Tuple
import math

from .geo_matching import haversine_distance, load_json


DISCLAIMER = (
    "ReLoop provides preliminary industrial reuse decision support. "
    "Opportunity scores are not material quality, safety, or regulatory "
    "certifications. Properties and documents are seller-declared unless "
    "independently verified. Distances are straight-line estimates."
)


def _normalize(value: Any) -> str:
    return str(value).strip().lower()


def _valid_number(value: Any):
    try:
        if isinstance(value, bool):
            return None

        number = float(value)

        if not math.isfinite(number):
            return None

        return number

    except (TypeError, ValueError):
        return None


def evaluate_properties(
    seller_props: Dict[str, Any],
    buyer_req_props: Dict[str, Any],
    pathway_key_props: List[str],
) -> Tuple[bool, List[str], List[str], List[str], float]:

    passed_reasons = []
    failed_reasons = []
    unverified_props = []

    seller_norm = {
        _normalize(k): v
        for k, v in (seller_props or {}).items()
    }

    buyer_norm = {
        _normalize(k): v
        for k, v in (buyer_req_props or {}).items()
    }

    passed_count = 0
    total_required = len(buyer_norm)

    for prop_name, rule in buyer_norm.items():

        if (
            prop_name not in seller_norm
            or seller_norm[prop_name] is None
            or seller_norm[prop_name] == ""
        ):
            unverified_props.append(
                f"{prop_name} (required by buyer, not declared by seller)"
            )
            continue

        seller_val = seller_norm[prop_name]

        if isinstance(rule, dict):

            numeric_val = _valid_number(seller_val)

            if numeric_val is None:
                unverified_props.append(
                    f"{prop_name} (seller value is not a valid number)"
                )
                continue

            min_val = None
            max_val = None

            if rule.get("min") is not None:
                min_val = _valid_number(rule["min"])

                if min_val is None:
                    unverified_props.append(
                        f"{prop_name} (buyer minimum is invalid)"
                    )
                    continue

            if rule.get("max") is not None:
                max_val = _valid_number(rule["max"])

                if max_val is None:
                    unverified_props.append(
                        f"{prop_name} (buyer maximum is invalid)"
                    )
                    continue

            if min_val is None and max_val is None:
                unverified_props.append(
                    f"{prop_name} (no usable numeric buyer limit)"
                )
                continue

            if (
                min_val is not None
                and max_val is not None
                and min_val > max_val
            ):
                unverified_props.append(
                    f"{prop_name} (buyer minimum exceeds maximum)"
                )
                continue

            if min_val is not None and numeric_val < min_val:
                failed_reasons.append(
                    f"Declared {prop_name} ({numeric_val}) "
                    f"is below buyer minimum ({min_val})"
                )
                continue

            if max_val is not None and numeric_val > max_val:
                failed_reasons.append(
                    f"Declared {prop_name} ({numeric_val}) "
                    f"exceeds buyer maximum ({max_val})"
                )
                continue

            passed_count += 1

            passed_reasons.append(
                f"Declared {prop_name} ({numeric_val}) "
                "satisfies buyer requirement"
            )

        else:

            if _normalize(seller_val) == _normalize(rule):
                passed_count += 1

                passed_reasons.append(
                    f"Declared {prop_name} ('{seller_val}') "
                    "matches buyer requirement"
                )

            else:
                failed_reasons.append(
                    f"Declared {prop_name} ('{seller_val}') "
                    f"does not match buyer requirement ('{rule}')"
                )

    already_reported = {
        warning.split(" (", 1)[0]
        for warning in unverified_props
    }

    for key_prop in pathway_key_props or []:

        key_norm = _normalize(key_prop)

        if key_norm in already_reported:
            continue

        if (
            key_norm not in seller_norm
            or seller_norm[key_norm] is None
            or seller_norm[key_norm] == ""
        ):
            unverified_props.append(
                f"{key_norm} "
                "(relevant to pathway, not declared by seller)"
            )

            already_reported.add(key_norm)

    if failed_reasons:
        return (
            False,
            passed_reasons,
            failed_reasons,
            unverified_props,
            0.0,
        )

    if total_required > 0:
        property_points = round(
            35.0 * passed_count / total_required,
            1,
        )
    else:
        # Neutral score when the buyer specifies no property rules.
        # This does not indicate verified material compatibility.
        property_points = 17.5

    return (
        True,
        passed_reasons,
        failed_reasons,
        unverified_props,
        property_points,
    )


def evaluate_documents(
    seller_docs: List[str],
    buyer_req_docs: List[str],
) -> Tuple[List[str], List[str], float]:

    seller_map = {
        _normalize(doc): str(doc).strip()
        for doc in (seller_docs or [])
    }

    matched_docs = []
    missing_docs = []

    for required_doc in buyer_req_docs or []:

        cleaned = str(required_doc).strip()

        if _normalize(cleaned) in seller_map:
            matched_docs.append(cleaned)
        else:
            missing_docs.append(cleaned)

    total = len(buyer_req_docs or [])

    if total > 0:
        document_points = round(
            15.0 * len(matched_docs) / total,
            1,
        )
    else:
        document_points = 7.5

    return matched_docs, missing_docs, document_points


def _research_evidence(
    pathway: Dict[str, Any],
    sources: Any,
) -> List[Dict[str, Any]]:

    evidence = []

    source_lookup = {}

    if isinstance(sources, list):
        source_lookup = {
            str(source.get("id")): source
            for source in sources
            if isinstance(source, dict)
        }

    elif isinstance(sources, dict):
        source_lookup = sources

    for item in pathway.get("evidence") or []:

        if not isinstance(item, dict):
            continue

        source_id = item.get("source_id")

        source = source_lookup.get(
            str(source_id),
            {},
        )

        if not isinstance(source, dict):
            source = {}

        evidence.append(
            {
                "source_id": source_id,
                "page": item.get("page"),
                "source_details": source,
            }
        )

    return evidence


def match_material_with_requirements(
    material: Dict[str, Any],
    requirements: List[Dict[str, Any]],
    max_distance_km: float = 300.0,
) -> Dict[str, Any]:

    pathways_data = load_json("reuse_pathways.json")

    supported_material = pathways_data["material"]["name"]
    pathways = pathways_data["reuse_pathways"]

    seller_material = str(
        material.get("material_name", "")
    ).strip()

    seller_qty = float(
        material.get("quantity_tonnes", 0)
    )

    seller_lat = float(
        material.get("latitude", 0)
    )

    seller_lon = float(
        material.get("longitude", 0)
    )

    valid_matches = []
    rejected_candidates = []

    if _normalize(seller_material) != _normalize(supported_material):

        return {
            "material_supported": False,
            "matches": [],
            "rejected_candidates": [
                {
                    "match_status": "rejected",
                    "reason": (
                        f"Material '{seller_material}' has no "
                        "documented reuse pathway in the current dataset."
                    ),
                    "rejection_reasons": [
                        "Unsupported material in current research dataset."
                    ],
                }
            ],
            "disclaimer": DISCLAIMER,
        }

    for req in requirements or []:

        buyer_id = req.get("id")
        buyer_name = req.get(
            "buyer_name",
            "Unknown Buyer",
        )

        buyer_industry = str(
            req.get("buyer_industry", "")
        ).strip()

        req_material = str(
            req.get("required_material", "")
        ).strip()

        req_qty = float(
            req.get("required_quantity_tonnes", 0)
        )

        buyer_lat = float(
            req.get("latitude", 0)
        )

        buyer_lon = float(
            req.get("longitude", 0)
        )

        if _normalize(req_material) != _normalize(seller_material):

            rejected_candidates.append(
                {
                    "requirement_id": buyer_id,
                    "buyer_name": buyer_name,
                    "match_status": "rejected",
                    "rejection_reasons": [
                        f"Material mismatch: buyer requires "
                        f"'{req_material}', seller offers "
                        f"'{seller_material}'."
                    ],
                }
            )

            continue

        pathway = next(
            (
                p for p in pathways
                if _normalize(p["buyer_industry"])
                == _normalize(buyer_industry)
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
                        f"No documented fly ash reuse pathway "
                        f"for '{buyer_industry}'."
                    ],
                }
            )

            continue

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
                        f"Distance {distance_km} km exceeds "
                        f"search radius {max_distance_km} km."
                    ],
                }
            )

            continue

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

        quantity_sufficient = seller_qty >= req_qty

        if req_qty > 0:

            quantity_coverage = round(
                min(
                    100.0,
                    100.0 * seller_qty / req_qty,
                ),
                1,
            )

            quantity_points = round(
                25.0 * quantity_coverage / 100.0,
                1,
            )

        else:
            quantity_coverage = 0.0
            quantity_points = 0.0

        quantity_status = (
            "full_supply"
            if quantity_sufficient
            else "partial_supply"
        )

        if quantity_sufficient:

            quantity_explanation = (
                f"Seller quantity ({seller_qty} t) "
                f"covers buyer demand ({req_qty} t)."
            )

        else:

            quantity_explanation = (
                f"Partial supply: {seller_qty} t available "
                f"against {req_qty} t requested."
            )

        proximity_points = round(
            25.0 * max(
                0.0,
                1.0 - distance_km / max_distance_km,
            ),
            1,
        )

        (
            matched_docs,
            missing_docs,
            document_points,
        ) = evaluate_documents(
            material.get("available_documents") or [],
            req.get("required_documents") or [],
        )

        opportunity_score = round(
            property_points
            + quantity_points
            + proximity_points
            + document_points,
            1,
        )

        seller_props = {
            _normalize(k): v
            for k, v in (
                material.get("material_properties") or {}
            ).items()
        }

        missing_buyer_properties = []

        for prop_name in (
            req.get("required_properties") or {}
        ).keys():

            normalized = _normalize(prop_name)

            if (
                normalized not in seller_props
                or seller_props[normalized] is None
                or seller_props[normalized] == ""
            ):
                missing_buyer_properties.append(prop_name)

        invalid_or_incomplete_properties = [
            warning
            for warning in unverified_properties
            if (
                "required by buyer" in warning
                or "not a valid number" in warning
                or "buyer minimum is invalid" in warning
                or "buyer maximum is invalid" in warning
                or "no usable numeric buyer limit" in warning
                or "buyer minimum exceeds maximum" in warning
            )
        ]

        if (
            missing_buyer_properties
            or invalid_or_incomplete_properties
            or missing_docs
        ):
            verification_status = "pending_verification"

        else:
            verification_status = "eligible_for_review"

        if verification_status == "pending_verification":
            match_status = "pending_verification"

        elif not quantity_sufficient:
            match_status = "partial_supply"

        else:
            match_status = "eligible_for_review"

        reference_standard = pathway.get(
            "reference_standard"
        )

        research_evidence = _research_evidence(
            pathway,
            pathways_data.get("sources", []),
        )

        explanations = [
            (
                f"Research-supported pathway: "
                f"{buyer_industry} → {pathway['application']}."
            ),
            (
                f"Reference standard: "
                f"{reference_standard or 'Not specified'}. "
                "Applicability and compliance are not verified."
            ),
            (
                f"Straight-line distance: {distance_km} km. "
                "This is not road travel distance."
            ),
            quantity_explanation,
            (
                f"Quantity coverage: {quantity_coverage}% "
                "of buyer demand."
            ),
            (
                f"Preliminary opportunity score: "
                f"{opportunity_score}/100. "
                "This is not a certification score."
            ),
        ]

        explanations.extend(passed_properties)

        if matched_docs:

            explanations.append(
                "Seller declares documents: "
                + ", ".join(matched_docs)
                + ". Authenticity is not independently verified."
            )

        if missing_docs:

            explanations.append(
                "Missing requested documents: "
                + ", ".join(missing_docs)
            )

        if unverified_properties:

            explanations.append(
                "Additional properties require verification: "
                + "; ".join(unverified_properties)
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
                "distance_type": "straight_line_haversine",

                "required_quantity_tonnes": req_qty,
                "available_quantity_tonnes": seller_qty,
                "quantity_sufficient": quantity_sufficient,
                "quantity_coverage_percent": quantity_coverage,
                "quantity_status": quantity_status,

                "pathway_id": pathway["id"],
                "recommended_application": pathway["application"],
                "standard_reference": reference_standard,
                "research_evidence": research_evidence,

                "opportunity_score": opportunity_score,

                # Backward-compatible field for Android clients.
                "compatibility_score": opportunity_score,

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
                "verification_status": verification_status,
                "match_status": match_status,

                "is_synthetic": bool(
                    material.get("is_synthetic")
                    or req.get("is_synthetic")
                ),

                "disclaimer": DISCLAIMER,
            }
        )

    valid_matches.sort(
        key=lambda match: (
            not match["quantity_sufficient"],
            -match["opportunity_score"],
            match["distance_km"],
        )
    )

    return {
        "material_supported": True,
        "matches": valid_matches,
        "rejected_candidates": rejected_candidates,
        "disclaimer": DISCLAIMER,
    }