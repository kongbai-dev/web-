package com.campus.market.dto;

import com.campus.market.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class Views {
    private Views() {}
    public record UserView(Long id,String username,String studentNo,String nickname,String avatar,String email,String phone,String campus,String dormArea,String bio,String role,String status,Double creditScore,long reviewCount) {}
    public record SellerView(Long id,String nickname,String avatar,String campus,Double creditScore) {}
    public record CategoryView(Long id,String name,Integer sortOrder,Boolean active) {}
    public record ProductView(Long id,String title,String description,BigDecimal originalPrice,BigDecimal price,String conditionLevel,String tradeType,String tradeLocation,String status,Integer viewCount,Integer favoriteCount,List<String> imageUrls,LocalDateTime createdAt,CategoryView category,SellerView seller,boolean favorited) {}
    public record OrderView(Long id,String orderNo,ProductView product,UserBrief buyer,UserBrief seller,BigDecimal amount,String tradeLocation,String buyerNote,String status,LocalDateTime createdAt,LocalDateTime confirmedAt,LocalDateTime paidAt,LocalDateTime deliveredAt,LocalDateTime completedAt,boolean reviewed) {}
    public record UserBrief(Long id,String username,String nickname,String avatar) {}
    public record ReviewView(Long id,Long orderId,UserBrief reviewer,Integer rating,String content,LocalDateTime createdAt) {}
    public record ReportView(Long id,UserBrief reporter,ProductView product,String reason,String description,String status,String handleResult,LocalDateTime createdAt,LocalDateTime handledAt) {}
    public static UserBrief brief(User u) { return new UserBrief(u.id,u.username,u.nickname,u.avatar); }
}
