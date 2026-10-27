from fastapi import FastAPI
from database import Base, engine
from routers import passport

app = FastAPI(title="ReLoop API - Material Passport")

Base.metadata.create_all(bind=engine)

app.include_router(passport.router)


@app.get("/")
def root():
    return {"message": "ReLoop Material Passport API is running ✅"}