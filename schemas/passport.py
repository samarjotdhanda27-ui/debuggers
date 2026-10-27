from pydantic import BaseModel
from typing import Optional, List, Dict
from datetime import datetime


class PassportCreate(BaseModel):
    material_name: str
    batch_id: str
    source_factory: str
    quantity: Optional[str] = None
    properties: Optional[Dict] = {}
    reports: Optional[List[Dict]] = []


class PassportResponse(BaseModel):
    id: str
    material_name: str
    batch_id: str
    source_factory: str
    quantity: Optional[str] = None
    properties: Optional[Dict] = {}
    reports: Optional[List[Dict]] = []
    docs_completeness: str
    history: Optional[List] = []
    created_at: datetime

    class Config:
        from_attributes = True