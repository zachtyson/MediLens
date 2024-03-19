from sqlalchemy.orm import sessionmaker
from sqlalchemy.orm import Session
from sqlalchemy import create_engine, MetaData
from databases import Database
from core.config import settings
from models.user import Base, User

# Create the engine
engine = create_engine(settings.DATABASE_URL, pool_size=5, max_overflow=1)

# Create a session factory
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Create a databases.Database instance
database = Database(settings.DATABASE_URL)

# Define the metadata
metadata = MetaData()

Base.metadata.create_all(bind=engine)


def get_db() -> Session:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
