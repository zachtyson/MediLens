from sqlalchemy.orm import sessionmaker
from sqlalchemy.orm import Session
from sqlalchemy import create_engine
from databases import Database
from core.config import settings
from models.user import User, Base as UserBase
from models.medication import Medication, Base as MedicationBase
from models.drug_interaction import DrugInteraction, Base as DrugInteractionBase
# Create the engine
engine = create_engine(settings.DATABASE_URL, pool_size=5, max_overflow=1)

# Create a session factory
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Create a databases.Database instance
database = Database(settings.DATABASE_URL)

# Create tables
UserBase.metadata.create_all(bind=engine)
MedicationBase.metadata.create_all(bind=engine)
DrugInteractionBase.metadata.create_all(bind=engine)

def get_db() -> Session:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
