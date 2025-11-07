package com.swp.mmostore.entity;

public enum TransactionType {
    DEPOSIT("Nạp tiền"),
    WITHDRAWAL("Rút tiền");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
