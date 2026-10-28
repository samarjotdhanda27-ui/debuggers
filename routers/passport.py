# Re-export from the integrated backend passport router
from backend.passport_routes import (
    router,
    get_passport,
    get_passport_qr,
    create_passport,
    list_passports,
    PassportCreate,
    CustodyEvent
)

__all__ = ["router", "get_passport", "get_passport_qr", "create_passport", "list_passports"]
