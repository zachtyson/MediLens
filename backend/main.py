from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from api.routes import camera
import os

app = FastAPI()
app.include_router(camera.router)

# gets the origins from the environment variable CORS_ORIGINS, if it exists, or defaults to the android studio emulator
origins_env = os.getenv("CORS_ORIGINS", "http://localhost:5555,https://localhost:5555,http://localhost")
origins = origins_env.split(",")

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["POST", "GET", "PUT", "DELETE"],
    allow_headers=["*"],
)


@app.get("/")
def read_root():
    return {"Hello": "World"}


@app.on_event("startup")
async def startup():
    # await database.connect()
    pass


@app.on_event("shutdown")
async def shutdown():
    # await database.disconnect()
    pass
