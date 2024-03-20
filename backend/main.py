from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import os


# add routes from /api/routes
from api.routes import user_login_routes, user_registration_routes, ml_model_routes
from api.healthcare_info import medication_routes

# add routes to the app
app = FastAPI()
app.include_router(user_login_routes.router)
app.include_router(user_registration_routes.router)
app.include_router(ml_model_routes.router)
app.include_router(medication_routes.router)



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
