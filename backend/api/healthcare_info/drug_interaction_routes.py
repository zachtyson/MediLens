from core.security import get_token_from_header, get_id_from_token, verify_token
from db.session import SessionLocal
from fastapi import APIRouter, HTTPException, Form, Depends
from sqlalchemy.orm import Session
from models.drug_interaction import DrugInteraction
from schemas.drug_interaction import UserDrugs

router = APIRouter()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.get("/medication/interactions")
async def get_medication_interaction(drug_a: str, drug_b: str, db: Session = Depends(get_db)):
    # sort the drugs alphabetically
    drug_a, drug_b = sorted([drug_a, drug_b])

    # put drugs in lowercase
    drug_a = drug_a.lower()
    drug_b = drug_b.lower()

    interaction = db.query(DrugInteraction).filter(DrugInteraction.drug_a == drug_a,
                                                   DrugInteraction.drug_b == drug_b).first()
    if interaction is None:
        raise HTTPException(status_code=204, detail="Interaction not found")
    return {
        "drug_a": interaction.drug_a,
        "drug_b": interaction.drug_b,
        "severity": interaction.severity,
        "description": interaction.description,
        "extended_description": interaction.extended_description
    }

@router.post("/medication/get_all_user_interactions/")
# compares all drugs within the user's medication list
# this scales poorly, but it's the best we can do for now
async def get_all_user_interactions(user_drugs: UserDrugs, db: Session = Depends(get_db),
                                    token: str = Depends(get_token_from_header)):
    print(user_drugs.drugs)
    if not user_drugs:
        raise HTTPException(status_code=204, detail="User drugs not found")
    if not verify_token(token):
        raise HTTPException(status_code=401, detail="Unauthorized")
    # get user id from token
    user_id = get_id_from_token(token)
    # put drugs in lowercase
    user_drugs.drugs = [drug.lower() for drug in user_drugs.drugs]
    interactions = []
    for i in range(len(user_drugs.drugs)):
        for j in range(i + 1, len(user_drugs.drugs)):
            drug_a, drug_b = sorted([user_drugs.drugs[i], user_drugs.drugs[j]])
            interaction = db.query(DrugInteraction).filter(DrugInteraction.drug_a == drug_a,
                                                           DrugInteraction.drug_b == drug_b).first()
            if interaction is not None:
                interactions.append({
                    "drug_a": interaction.drug_a,
                    "drug_b": interaction.drug_b,
                    "severity": interaction.severity,
                    "description": interaction.description,
                    "extended_description": interaction.extended_description
                })
    print(interactions)
    return interactions

