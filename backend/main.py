from fastapi import FastAPI

from .cloudinary_config import check_cloudinary_connection
from .geo_routes import router as geo_router
from .listing_routes import router as listing_router
from .matching_routes import router as matching_router
from .media_routes import router as media_router
from .mongodb import check_mongodb_connection
from .pathway_routes import router as pathway_router


app = FastAPI(
    title="ReLoop API",
    description="Intelligent Industrial By-Product Exchange",
    version="1.1",
)

app.include_router(geo_router)
app.include_router(pathway_router)
app.include_router(listing_router)
app.include_router(matching_router)
app.include_router(media_router)


@app.get("/health")
def health_check():
    mongodb_ok = check_mongodb_connection()
    cloudinary_ok = check_cloudinary_connection()

    return {
        "status": "ok",
        "project": "ReLoop",
        "database": "Supabase PostgreSQL + MongoDB Atlas",
        "mongodb": "connected" if mongodb_ok else "disconnected",
        "cloudinary": "configured" if cloudinary_ok else "not_configured",
    }


@app.get("/mongodb-status")
def mongodb_status():
    connected = check_mongodb_connection()

    return {
        "database": "MongoDB Atlas",
        "connected": connected,
        "status": "connected" if connected else "disconnected",
    }


@app.get("/cloudinary-status")
def cloudinary_status():
    configured = check_cloudinary_connection()

    return {
        "service": "Cloudinary",
        "configured": configured,
        "status": "configured" if configured else "not_configured",
    } 