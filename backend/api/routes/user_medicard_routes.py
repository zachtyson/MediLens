from fastapi import APIRouter, Depends, HTTPException, status, Form
from sqlalchemy.orm import Session
from datetime import timedelta
from models.user import User
from db.session import SessionLocal, get_db
from fastapi.security import OAuth2PasswordRequestForm
from typing import Annotated
from core.security import Token, verify_password, access_token_minutes, create_access_token, get_token_from_header, \
    get_id_from_token
from models.email import EmailRequest, EmailResponse
from fastapi_mail import FastMail, MessageSchema, ConnectionConfig
from pydantic import EmailStr
from typing import List

router = APIRouter()

# Define email settings
conf = ConnectionConfig(
    MAIL_USERNAME = "your_email@gmail.com",
    MAIL_PASSWORD = "your_password",
    MAIL_FROM = "your_email@gmail.com",
    MAIL_PORT = 587,
    MAIL_SERVER = "smtp.gmail.com",
    MAIL_TLS = True,
    MAIL_SSL = False,
    TEMPLATE_FOLDER = "templates"
)



def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
# returns name and email of user
@router.get('/medicard/user_info')
def get_user_basic_medicard(db: Session = Depends(get_db), token: str = Depends(get_token_from_header)):
    user_id = get_id_from_token(token)
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return {"name": user.name, "email": user.email}


# Define endpoint to send emails
@app.post("/send-email/", response_model=EmailResponse)
async def send_email(request: EmailRequest, background_tasks: BackgroundTasks):
    # Define email settings
    conf = ConnectionConfig(
        MAIL_USERNAME="<insert email>@gmail.com",
        MAIL_PASSWORD="<insert password",
        MAIL_FROM="<insert email>@gmail.com",
        MAIL_PORT=587,
        MAIL_SERVER="smtp.gmail.com",
        MAIL_TLS=True,
        MAIL_SSL=False,
        TEMPLATE_FOLDER="templates"
    )

    # Initialize FastMail instance
    fast_mail = FastMail(conf)

    message = MessageSchema(
        subject=request.subject,
        recipients=[request.to],  # Convert the string to a list containing a single email address
        body=request.body,
        subtype="html"
    )
    background_tasks.add_task(fast_mail.send_message, message)
    return True
