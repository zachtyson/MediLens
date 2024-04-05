from sqlalchemy import Column, Integer, String, UniqueConstraint
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class DrugInteraction(Base):
    __tablename__ = "drug_interactions"
    id = Column(Integer, primary_key=True)
    drug_a = Column(String(255), nullable=False)
    drug_b = Column(String(255), nullable=False)
    severity = Column(String(255), nullable=False)
    description = Column(String(255), nullable=True)
    extended_description = Column(String(2000), nullable=True)

    # composite unique constraint for drug_a and drug_b so that a-b and b-a are not repeated
    __table_args__ = (UniqueConstraint('drug_a', 'drug_b', name='uq_drug_interaction'),)

    def __repr__(self):
        return f"<DrugInteraction(id={self.id}, drug_a={self.drug_a!r}, drug_b={self.drug_b!r}, " \
               f"severity={self.severity!r}, description={self.description!r}, " \
               f"extended_description={self.extended_description!r})>"

    def __init__(self, id, drug_a, drug_b, severity, description, extended_description):
        self.id = id
        drug_a, drug_b = sorted([drug_a, drug_b])  # sort the drugs alphabetically
        self.drug_a = drug_a
        self.drug_b = drug_b
        self.severity = severity
        self.description = description
        self.extended_description = extended_description

