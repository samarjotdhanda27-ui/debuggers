from fastapi import FastAPI

from .geo_routes import router as geo_router
from .pathway_routes import router as pathway_router
from .listing_routes import router as listing_router
from .matching_routes import router as matching_router
from .mongodb import check_mongodb_connection


app = FastAPI(
    title="ReLoop API",
    description="Intelligent Industrial By-Product Exchange",
    version="1.1",
)

app.include_router(geo_router)
app.include_router(pathway_router)
app.include_router(listing_router)
app.include_router(matching_router)


@app.get("/health")
def health_check():
    return {
        "status": "ok",
        "project": "ReLoop",
        "database": "Supabase PostgreSQL",
    }


@app.get("/mongodb-status")
def mongodb_status():
    connected = check_mongodb_connection()

    return {
        "database": "MongoDB Atlas",
        "connected": connected,
        "status": "connected" if connected else "disconnected",
    }