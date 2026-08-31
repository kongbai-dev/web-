package com.campus.market.controller;

import com.campus.market.common.*;
import com.campus.market.dto.Views.*;
import com.campus.market.entity.*;
import com.campus.market.entity.Enums.OrderStatus;
import com.campus.market.repository.*;
import com.campus.market.security.CurrentUser;
import com.campus.market.service.ViewMapper;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@RestController @RequestMapping("/api")
public class ReviewReportController {
    private final CurrentUser current;private final OrderRepository orders;private final ProductRepository products;private final ReviewRepository reviews;private final ReportRepository reports;private final ViewMapper mapper;
    public ReviewReportController(CurrentUser c,OrderRepository o,ProductRepository p,ReviewRepository r,ReportRepository rp,ViewMapper m){current=c;orders=o;products=p;reviews=r;reports=rp;mapper=m;}
    public record ReviewRequest(@NotNull(message="请选择评分") @Min(value=1,message="评分须为 1~5") @Max(value=5,message="评分须为 1~5") Integer rating,@Size(max=500,message="评价不能超过 500 字") String content){}
    @PostMapping("/orders/{id}/reviews") @Transactional public ApiResponse<ReviewView> review(@PathVariable Long id,@Valid @RequestBody ReviewRequest req){User u=current.require();MarketOrder o=orders.findForUpdate(id).orElseThrow(()->new BusinessException(40402,"订单不存在"));if(o.status!=OrderStatus.COMPLETED)throw new BusinessException(40003,"订单完成后才能评价");if(!o.buyer.id.equals(u.id)&&!o.seller.id.equals(u.id))throw new BusinessException(40301,"无权评价");if(reviews.existsByOrderIdAndReviewerId(id,u.id))throw new BusinessException(40001,"该订单已评价");Review r=new Review();r.order=o;r.reviewer=u;r.targetUser=o.buyer.id.equals(u.id)?o.seller:o.buyer;r.rating=req.rating();r.content=req.content();return ApiResponse.ok(mapper.review(reviews.save(r)));}
    public record ReportRequest(@NotBlank(message="请选择举报原因") @Size(max=50,message="举报原因过长") String reason,@NotBlank(message="请填写举报说明") @Size(max=500,message="举报说明不能超过 500 字") String description){}
    @PostMapping("/products/{id}/reports") public ApiResponse<ReportView> report(@PathVariable Long id,@Valid @RequestBody ReportRequest req){User u=current.require();Product p=products.findById(id).orElseThrow(()->new BusinessException(40401,"商品不存在"));if(p.seller.id.equals(u.id))throw new BusinessException(40001,"不能举报自己的商品");Report r=new Report();r.reporter=u;r.product=p;r.reason=req.reason();r.description=req.description();return ApiResponse.ok(mapper.report(reports.save(r)));}
    @GetMapping("/users/me/reports") public ApiResponse<List<ReportView>> mine(){User u=current.require();return ApiResponse.ok(reports.findByReporterIdOrderByCreatedAtDesc(u.id).stream().map(mapper::report).toList());}
}
