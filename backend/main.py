from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import os
import uuid
import datetime

app = FastAPI(
    title="AK Bank Digital Banking API",
    description="Fictional Digital Banking Application REST API for AK Bank (Banking Made Simple)",
    version="1.0.0"
)

# CORS Middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Pydantic Request/Response Models ---
class LoginRequest(BaseModel):
    email: str
    password: str

class RegisterRequest(BaseModel):
    full_name: str
    email: str
    phone: str
    cnic: str
    address: str
    city: str
    password: str

class TransferRequest(BaseModel):
    sender_account_id: int
    recipient_name: str
    bank_name: str
    destination_account: str
    amount: float
    purpose: str
    pin: str
    transfer_type: str

class BillPaymentRequest(BaseModel):
    account_id: int
    biller_name: str
    consumer_number: str
    amount: float
    pin: str

class TopupRequest(BaseModel):
    account_id: int
    network: str
    phone_number: str
    amount: float
    pin: str

class AssistantQueryRequest(BaseModel):
    query: str

# --- Endpoints ---
@app.get("/")
def read_root():
    return {
        "bank": "AK Bank",
        "tagline": "Banking Made Simple.",
        "status": "Online",
        "demo_disclaimer": "AK Bank is a fictional banking application created for demonstration purposes. No real banking, payment, or financial services are provided."
    }

@app.post("/api/auth/login")
def login(req: LoginRequest):
    if req.email == "demo@akbank.demo" and req.password == "Demo@12345":
        return {
            "access_token": "demo_jwt_customer_token",
            "token_type": "bearer",
            "user": {
                "id": 1,
                "email": "demo@akbank.demo",
                "full_name": "Muhammad Mawiya",
                "role": "CUSTOMER"
            }
        }
    elif req.email == "admin@akbank.demo" and req.password == "Admin@12345":
        return {
            "access_token": "demo_jwt_admin_token",
            "token_type": "bearer",
            "user": {
                "id": 2,
                "email": "admin@akbank.demo",
                "full_name": "AK Bank Admin",
                "role": "ADMIN"
            }
        }
    elif req.email == "support@akbank.demo" and req.password == "Support@12345":
        return {
            "access_token": "demo_jwt_support_token",
            "token_type": "bearer",
            "user": {
                "id": 3,
                "email": "support@akbank.demo",
                "full_name": "Ayesha Support",
                "role": "SUPPORT"
            }
        }
    raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid demo credentials")

@app.post("/api/auth/register")
def register(req: RegisterRequest):
    return {
        "message": "Demo customer account registered successfully",
        "user_id": 4,
        "email": req.email,
        "full_name": req.full_name,
        "role": "CUSTOMER"
    }

@app.get("/api/accounts")
def get_accounts():
    return [
        {
            "id": 1,
            "title": "Muhammad Mawiya - Current",
            "account_number": "001234567890",
            "iban": "PK78AKBK0012345678901234",
            "type": "CURRENT",
            "balance": 145250.00,
            "available_balance": 145250.00,
            "status": "ACTIVE"
        },
        {
            "id": 2,
            "title": "Muhammad Mawiya - Asaan Savings",
            "account_number": "009876543210",
            "iban": "PK21AKBK0098765432109876",
            "type": "SAVINGS",
            "balance": 380000.00,
            "available_balance": 380000.00,
            "status": "ACTIVE"
        }
    ]

@app.post("/api/transfers")
def transfer_funds(req: TransferRequest):
    if req.pin != "1234":
        raise HTTPException(status_code=400, detail="Invalid Transaction PIN")
    ref = f"AKB-{uuid.uuid4().hex[:8].upper()}"
    return {
        "status": "COMPLETED",
        "transaction_id": 105,
        "reference": ref,
        "amount": req.amount,
        "recipient": req.recipient_name,
        "bank": req.bank_name,
        "timestamp": datetime.datetime.utcnow().isoformat()
    }

@app.post("/api/bills/payments")
def pay_bill(req: BillPaymentRequest):
    if req.pin != "1234":
        raise HTTPException(status_code=400, detail="Invalid Transaction PIN")
    return {
        "status": "COMPLETED",
        "reference": f"BILL-{uuid.uuid4().hex[:6].upper()}",
        "biller": req.biller_name,
        "consumer_number": req.consumer_number,
        "amount": req.amount
    }

@app.post("/api/topups")
def mobile_topup(req: TopupRequest):
    return {
        "status": "COMPLETED",
        "reference": f"TOP-{uuid.uuid4().hex[:6].upper()}",
        "network": req.network,
        "phone_number": req.phone_number,
        "amount": req.amount
    }

@app.get("/api/cards")
def get_cards():
    return [
        {
            "id": 1,
            "masked_number": "4532 •••• •••• 8842",
            "card_holder": "MUHAMMAD MAWIYA",
            "expiry": "12/28",
            "is_frozen": False,
            "is_online_enabled": True,
            "is_intl_enabled": False,
            "spending_limit": 250000.00,
            "card_type": "DEBIT_VISA"
        }
    ]

@app.get("/api/admin/dashboard")
def get_admin_dashboard():
    return {
        "total_customers": 1420,
        "active_customers": 1395,
        "total_system_balance": 525250.00,
        "total_transactions": 8420,
        "today_volume": 420000.00,
        "open_tickets": 3
    }

@app.post("/api/assistant/query")
def query_ai_assistant(req: AssistantQueryRequest):
    q = req.query.lower()
    if "spend" in q:
        return {"response": "Based on your demo account activity, you have spent Rs. 46,320.00 this month."}
    elif "highest" in q:
        return {"response": "Your highest expense was Rs. 18,450.00 at Metro Cash & Carry (Groceries & Shopping)."}
    elif "receive" in q or "income" in q:
        return {"response": "You received Rs. 285,000.00 in total credits this month, primarily salary and freelance revenue."}
    return {"response": "AK Assistant is ready to help you analyze your accounts, spending patterns, and transactions."}
