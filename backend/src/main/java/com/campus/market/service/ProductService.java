package com.campus.market.service;

import com.campus.market.common.BusinessException;
import com.campus.market.dto.Views.ProductView;
import com.campus.market.entity.*;
import com.campus.market.entity.Enums.*;
import com.campus.market.repository.*;
import com.campus.market.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.*;

@Service
public class ProductService {
    private final ProductRepository products; private final CategoryRepository categories; private final FavoriteRepository favorites; private final BrowseHistoryRepository history; private final CurrentUser current; private final ViewMapper mapper;
    public ProductService(ProductRepository p,CategoryRepository c,FavoriteRepository f,BrowseHistoryRepository h,CurrentUser u,ViewMapper m){products=p;categories=c;favorites=f;history=h;current=u;mapper=m;}
    public record ProductRequest(@NotBlank(message="商品标题不能为空") @Size(max=100,message="商品标题不能超过 100 字") String title,@Size(max=5000,message="商品描述过长") String description,@NotNull(message="请选择分类") Long categoryId,@DecimalMin(value="0",message="原价不能小于 0") BigDecimal originalPrice,@NotNull(message="请输入价格") @DecimalMin(value="0",message="价格不能小于 0") BigDecimal price,@Size(max=30,message="成色描述过长") String conditionLevel,TradeType tradeType,@Size(max=100,message="交易地点不能超过 100 字") String tradeLocation,@Size(max=8,message="最多上传 8 张图片") List<@Size(max=500,message="图片地址过长") String> imageUrls){}
    @Transactional public ProductView create(ProductRequest r){ User u=current.require(); validate(r); Product p=new Product(); apply(p,r);p.seller=u; return mapper.product(products.save(p),u.id); }
    @Transactional public ProductView update(Long id,ProductRequest r){ User u=current.require(); Product p=find(id); owner(p,u);if(p.status==ProductStatus.SOLD)throw new BusinessException(40002,"已售商品不能编辑");validate(r);apply(p,r);p.updatedAt=LocalDateTime.now();return mapper.product(p,u.id);}
    @Transactional public void offShelf(Long id){User u=current.require();Product p=find(id);owner(p,u);if(p.status==ProductStatus.RESERVED)throw new BusinessException(40002,"已有进行中订单，不能下架");if(p.status==ProductStatus.SOLD)throw new BusinessException(40002,"已售商品不能下架");p.status=ProductStatus.OFF_SHELF;p.updatedAt=LocalDateTime.now();}
    @Transactional public void relist(Long id){User u=current.require();Product p=find(id);owner(p,u);if(p.status!=ProductStatus.OFF_SHELF)throw new BusinessException(40002,"当前状态不能重新上架");p.status=ProductStatus.ON_SALE;p.updatedAt=LocalDateTime.now();}
    @Transactional public ProductView detail(Long id){ Product p=products.findDetail(id).orElseThrow(()->new BusinessException(40401,"商品不存在")); p.viewCount++; User u=current.optional(); if(u!=null){BrowseHistory h=history.findByUserIdAndProductId(u.id,id).orElseGet(BrowseHistory::new);h.user=u;h.product=p;h.viewedAt=LocalDateTime.now();history.save(h);} return mapper.product(p,u==null?null:u.id); }
    public Page<ProductView> search(String keyword,Long categoryId,BigDecimal min,BigDecimal max,ProductStatus status,String sort,int page,int size){
        Sort s=switch(sort==null?"newest":sort){case "priceAsc"->Sort.by("price").ascending();case "priceDesc"->Sort.by("price").descending();case "popular"->Sort.by("viewCount").descending();default->Sort.by("createdAt").descending();};
        User u=current.optional();return products.search(blank(keyword)?null:keyword,categoryId,status,min,max,PageRequest.of(Math.max(0,page),Math.min(size,50),s)).map(p->mapper.product(p,u==null?null:u.id));}
    public Page<ProductView> mine(int page,int size){User u=current.require();return products.findBySellerId(u.id,PageRequest.of(page,size,Sort.by("createdAt").descending())).map(p->mapper.product(p,u.id));}
    @Transactional public boolean favorite(Long id){User u=current.require();Product p=find(id);if(favorites.existsByUserIdAndProductId(u.id,id))return false;Favorite f=new Favorite();f.user=u;f.product=p;favorites.save(f);p.favoriteCount++;return true;}
    @Transactional public void unfavorite(Long id){User u=current.require();favorites.findByUserIdAndProductId(u.id,id).ifPresent(f->{favorites.delete(f);Product p=f.product;p.favoriteCount=Math.max(0,p.favoriteCount-1);});}
    public List<ProductView> favorites(){User u=current.require();return favorites.findByUserIdOrderByCreatedAtDesc(u.id).stream().map(f->mapper.product(f.product,u.id)).toList();}
    public List<ProductView> history(){User u=current.require();return history.findTop30ByUserIdOrderByViewedAtDesc(u.id).stream().map(h->mapper.product(h.product,u.id)).toList();}
    private Product find(Long id){return products.findById(id).orElseThrow(()->new BusinessException(40401,"商品不存在"));}
    private void owner(Product p,User u){if(!p.seller.id.equals(u.id))throw new BusinessException(40301,"只能操作自己的商品");}
    private void validate(ProductRequest r){if(blank(r.title()))throw new BusinessException(40001,"商品标题不能为空");if(r.categoryId()==null)throw new BusinessException(40001,"请选择分类");if(r.price()==null||r.price().signum()<0)throw new BusinessException(40001,"价格不能小于 0");}
    private boolean blank(String s){return s==null||s.isBlank();}
    private void apply(Product p,ProductRequest r){p.title=r.title();p.description=r.description();p.category=categories.findById(r.categoryId()).orElseThrow(()->new BusinessException(40001,"分类不存在"));p.originalPrice=r.originalPrice();p.price=r.price();p.conditionLevel=r.conditionLevel();p.tradeType=r.tradeType()==null?TradeType.SALE:r.tradeType();p.tradeLocation=r.tradeLocation();p.imageUrls=r.imageUrls()==null?new java.util.ArrayList<>():new java.util.ArrayList<>(r.imageUrls());}
}
