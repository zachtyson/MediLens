import os
import secrets
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    SECRET_KEY: str = secrets.token_urlsafe(32)
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7  # one week
    DATABASE_URL: str
    MAIL_USERNAME: str
    MAIL_PASSWORD: str
    MAIL_FROM: str

    class Config:
        case_sensitive = True


# Load test database URL if it exists, otherwise load the production database URL
DATABASE_URL = os.environ.get("TEST_DATABASE_URL")
if DATABASE_URL is None:
    DATABASE_URL = os.environ.get("DATABASE_URL")
if DATABASE_URL is None:
    raise ValueError("DATABASE_URL environment variable is not set.")
DATABASE_URL = DATABASE_URL.replace("postgres://", "postgresql://")

MAIL_USERNAME = os.environ.get("MAIL_USERNAME")
MAIL_PASSWORD = os.environ.get("MAIL_PASSWORD")
MAIL_FROM = os.environ.get("MAIL_FROM")

settings = Settings()
settings.DATABASE_URL = DATABASE_URL
settings.MAIL_USERNAME = MAIL_USERNAME
settings.MAIL_PASSWORD = MAIL_PASSWORD
settings.MAIL_FROM = MAIL_FROM
