from sqlalchemy import Column, Integer, String, DateTime, func, ForeignKey, Table, UniqueConstraint
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base
from .user import User
from .base import Base


class DrugInteraction(Base):
    __tablename__ = "drug_interactions"
    drug_a = Column(String(100), primary_key=True)
    drug_b = Column(String(100), primary_key=True)
    severity = Column(String(100), nullable=False)
    description = Column(String(100), nullable=True)
    extended_description = Column(String(1000), nullable=True)

    # composite unique constraint for drug_a and drug_b so that a-b and b-a are not repeated
    __table_args__ = (UniqueConstraint('drug_a', 'drug_b', name='uq_drug_interaction'),)

    def __repr__(self):
        return f"<DrugInteraction(drug_a={self.drug_a!r}, drug_b={self.drug_b!r}, " \
               f"severity={self.severity!r}, description={self.description!r}, " \
               f"extended_description={self.extended_description!r})>"

    def __init__(self, drug_a, drug_b, severity, description, extended_description):
        drug_a, drug_b = sorted([drug_a, drug_b])  # sort the drugs alphabetically
        self.drug_a = drug_a
        self.drug_b = drug_b
        self.severity = severity
        self.description = description
        self.extended_description = extended_description
