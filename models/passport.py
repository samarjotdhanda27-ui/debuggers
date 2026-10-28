from sqlalchemy import Column, String, JSON, DateTime
import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from database import Base
import uuid
import datetime


class MaterialPassport(Base):
    __tablename__ = "material_passports"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()), unique=True, index=True)
    material_name = Column(String, nullable=False)
    batch_id = Column(String, unique=True, nullable=False)
    source_factory = Column(String, nullable=False)
    quantity = Column(String, nullable=True)
    properties = Column(JSON, nullable=True, default={})
    reports = Column(JSON, nullable=True, default=[])
    docs_completeness = Column(String, default="Partial")
    history = Column(JSON, default=[])
    created_at = Column(DateTime, default=datetime.datetime.utcnow)