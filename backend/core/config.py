import os
import secrets
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    SECRET_KEY: str = secrets.token_urlsafe(32)
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60  # 1 hour
    DATABASE_URL: str

    class Config:
        case_sensitive = True


DATABASE_URL = os.environ.get("DATABASE_URL")
if DATABASE_URL is None:
    raise ValueError("DATABASE_URL environment variable is not set.")
DATABASE_URL = DATABASE_URL.replace("postgres://", "postgresql://")

settings = Settings()
settings.DATABASE_URL = DATABASE_URL
