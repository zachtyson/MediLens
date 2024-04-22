import os

from fastapi import APIRouter, Depends, HTTPException, BackgroundTasks
from sqlalchemy.orm import Session
from models.user import User
from db.session import SessionLocal
from core.security import get_token_from_header, \
    get_id_from_token
from schemas.email import EmailRequest, EmailResponse
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail
from core.config import MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM

router = APIRouter()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

api_key = os.environ.get('SENDGRID_API_KEY')

# Define endpoint to send emails
@router.post("/send-email/", response_model=EmailResponse)
async def send_email(request: EmailRequest):
    # Define email settings
    message = Mail(
        from_email=MAIL_FROM,
        to_emails=request.to,
        subject=request.subject,
        html_content=request.body
    )
    try:
        sg = SendGridAPIClient(api_key)
        response = sg.send(message)
        return EmailResponse(message="Email sent successfully")
    except Exception as e:
        print(e)
        return EmailResponse(message="Email not sent")


# returns name and email of user
@router.get('/medicard/user_info')
def get_user_basic_medicard(db: Session = Depends(get_db), token: str = Depends(get_token_from_header)):
    user_id = get_id_from_token(token)
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return {"name": user.name, "email": user.email}
