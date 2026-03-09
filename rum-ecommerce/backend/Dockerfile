# Classic Jazz backend – run with Docker Compose or locally with MongoDB
FROM python:3.12-slim

WORKDIR /app

# Copy backend into /app/backend so that "backend" is the package
COPY backend/ /app/backend/
RUN pip install --no-cache-dir -r /app/backend/requirements.txt

ENV PYTHONPATH=/app
EXPOSE 8000
CMD ["uvicorn", "backend.main:app", "--host", "0.0.0.0", "--port", "8000"]
