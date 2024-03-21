# routes/items.py
from fastapi import APIRouter, File, UploadFile
from fastapi.responses import JSONResponse
import logging

logging.basicConfig(level=logging.INFO)

router = APIRouter()

# Define the endpoint for handling the image upload
@router.post("/upload-image")
async def upload_image(file: UploadFile = File()):
    try:
            # USE ML models to process the image


            logging.info("Received file: %s", file.filename)
            logging.info("Content type: %s", file.content_type)

            # maybe save the image?
            #open(file.filename, "wb").write(file.file.read())


            # Return a JSON sucess response
            return JSONResponse(content={"message": "File uploaded successfully"}, status_code=200)

    except Exception as e:
        # Handle any exceptions that may occur during processing
        return JSONResponse(content={"error": str(e)}, status_code=500)