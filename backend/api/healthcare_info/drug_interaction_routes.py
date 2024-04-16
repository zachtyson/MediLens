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


@router.post("/medication/get_new_interaction/")
# compares new drug interaction with every drug within the user's medication list
async def get_new_interaction(user_drugs: UserDrugs, db: Session = Depends(get_db),
                              token: str = Depends(get_token_from_header)):
    if not user_drugs:
        raise HTTPException(status_code=204, detail="User drugs not found")
    if not verify_token(token):
        raise HTTPException(status_code=401, detail="Unauthorized")
    # get user id from token
    user_id = get_id_from_token(token)
    # compare token user id with user_drugs user id
    if user_id != user_drugs.user_id:
        raise HTTPException(status_code=401, detail="Unauthorized")
    interactions = []
    drug_a = user_drugs.new_drug.lower()
    drug_a_orig = drug_a
    print(user_drugs.drugs)
    print(user_drugs.new_drug)
    for drug_b in user_drugs.drugs:
        # sort the drugs alphabetically
        drug_b = drug_b.lower()
        drug_a, drug_b = sorted([drug_a, drug_b])
        print(drug_a, drug_b)
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
        drug_a = drug_a_orig
    return interactions
