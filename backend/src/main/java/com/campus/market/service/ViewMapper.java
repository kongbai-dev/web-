package com.campus.market.service;

import com.campus.market.dto.Views;
import com.campus.market.dto.Views.*;
import com.campus.market.entity.*;
import com.campus.market.repository.*;
import org.springframework.stereotype.Component;

@Component
public class ViewMapper {
    private final ReviewRepository reviews; private final FavoriteRepository favorites;
    public ViewMapper(ReviewRepository reviews, FavoriteRepository favorites) { this.reviews=reviews; this.favorites=favorites; }
    public UserView user(User u) { return new UserView(u.id,u.username,u.studentNo,u.nickname,u.avatar,u.email,u.phone,u.campus,u.dormArea,u.bio,u.role.name(),u.status.name(),reviews.averageForUser(u.id),reviews.countByTargetUserId(u.id)); }
    public ProductView product(Product p, Long currentUserId) {
        boolean f = currentUserId != null && favorites.existsByUserIdAndProductId(currentUserId,p.id);
        var c = new CategoryView(p.category.id,p.category.name,p.category.sortOrder,p.category.active);
        var s = new SellerView(p.seller.id,p.seller.nickname,p.seller.avatar,p.seller.campus,reviews.averageForUser(p.seller.id));
        return new ProductView(p.id,p.title,p.description,p.originalPrice,p.price,p.conditionLevel,p.tradeType.name(),p.tradeLocation,p.status.name(),p.viewCount,p.favoriteCount,new java.util.ArrayList<>(p.imageUrls),p.createdAt,c,s,f);
    }
    public OrderView order(MarketOrder o, Long currentUserId) { return new OrderView(o.id,o.orderNo,product(o.product,currentUserId),Views.brief(o.buyer),Views.brief(o.seller),o.amount,o.tradeLocation,o.buyerNote,o.status.name(),o.createdAt,o.confirmedAt,o.paidAt,o.deliveredAt,o.completedAt,currentUserId!=null&&reviews.existsByOrderIdAndReviewerId(o.id,currentUserId)); }
    public ReviewView review(Review r) { return new ReviewView(r.id,r.order.id,Views.brief(r.reviewer),r.rating,r.content,r.createdAt); }
    public ReportView report(Report r) { return new ReportView(r.id,Views.brief(r.reporter),product(r.product,null),r.reason,r.description,r.status.name(),r.handleResult,r.createdAt,r.handledAt); }
}
