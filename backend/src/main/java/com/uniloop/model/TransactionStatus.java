package com.uniloop.model;

public enum TransactionStatus {
    REQUESTED,    // borrower asked for the item
    APPROVED,     // owner accepted, handover QR generated
    HANDED_OVER,  // owner verified handover QR, item is with borrower
    RETURNED,     // owner verified return QR, transaction complete
    REJECTED,     // owner declined
    CANCELLED     // borrower cancelled before handover
}
