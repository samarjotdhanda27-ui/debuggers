from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
import os

# For now we use SQLite (easy, no setup needed)
# Later Samarjot can give you the real PostgreSQL URL from Render
DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./reloop.db")

# connect_args only needed for SQLite
connect_args = {"check_same_thread": False} if "sqlite" in DATABASE_URL else {}

engine = create_engine(DATABASE_URL, connect_args=connect_args)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()