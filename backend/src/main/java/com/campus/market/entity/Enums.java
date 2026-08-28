package com.campus.market.entity;

public final class Enums {
    private Enums() {}
    public enum Role { USER, ADMIN }
    public enum UserStatus { ACTIVE, BANNED }
    public enum ProductStatus { DRAFT, ON_SALE, RESERVED, SOLD, OFF_SHELF }
    public enum TradeType { SALE, GIFT, EXCHANGE }
    public enum OrderStatus { PENDING_CONFIRM, PENDING_PAYMENT, PENDING_DELIVERY, PENDING_RECEIVE, COMPLETED, CANCELLED }
    public enum ReportStatus { PENDING, APPROVED, REJECTED }
}
