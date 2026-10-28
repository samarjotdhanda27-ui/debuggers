from datetime import datetime
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from .geo_routes import router as geo_router
from .pathway_routes import router as pathway_router
from .listing_routes import router as listing_router
from .matching_routes import router as matching_router
from .passport_routes import router as passport_router
from .cloudinary_routes import router as cloudinary_router
from .quote_routes import router as quote_router
from .mongodb import check_mongodb_connection


app = FastAPI(
    title="ReLoop Industrial B2B Marketplace API",
    description="Intelligent Circular By-Product Exchange & Secondary Materials Matching",
    version="2.0.0",
)

# Enable CORS for Android clients, web dashboards, and mobile devices
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register all modular routers
app.include_router(geo_router)
app.include_router(pathway_router)
app.include_router(listing_router)
app.include_router(matching_router)
app.include_router(passport_router)
app.include_router(cloudinary_router)
app.include_router(quote_router)


@app.get("/health")
def health_check():
    mongo_ok = check_mongodb_connection()
    return {
        "status": "ok",
        "service": "ReLoop B2B Marketplace API",
        "version": "2.0.0",
        "timestamp": datetime.utcnow().isoformat(),
        "database": {
            "primary": "Supabase PostgreSQL",
            "mongodb_atlas": {
                "cluster": "cluster0.avzqsos.mongodb.net",
                "status": "connected" if mongo_ok else "disconnected"
            }
        },
        "cloudinary": {
            "cloud_name": "ru3ejcrx",
            "status": "configured"
        }
    }


@app.get("/mongodb-status")
def mongodb_status():
    connected = check_mongodb_connection()
    return {
        "database": "MongoDB Atlas",
        "cluster": "cluster0.avzqsos.mongodb.net",
        "connected": connected,
        "status": "connected" if connected else "disconnected",
    }
