package com.campus.market.controller;

import com.campus.market.common.*;
import com.campus.market.dto.Views.*;
import com.campus.market.entity.*;
import com.campus.market.entity.Enums.*;
import com.campus.market.repository.*;
import com.campus.market.security.CurrentUser;
import com.campus.market.service.ViewMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController @RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users;private final ProductRepository products;private final OrderRepository orders;private final ReportRepository reports;private final CategoryRepository categories;private final CurrentUser current;private final ViewMapper mapper;
    public AdminController(UserRepository u,ProductRepository p,OrderRepository o,ReportRepository r,CategoryRepository c,CurrentUser cu,ViewMapper m){users=u;products=p;orders=o;reports=r;categories=c;current=cu;mapper=m;}
    @GetMapping("/statistics") public ApiResponse<Map<String,Object>> stats(){Map<String,Object> s=new LinkedHashMap<>();s.put("users",users.count());s.put("products",products.count());s.put("onSaleProducts",products.countByStatus(ProductStatus.ON_SALE));s.put("orders",orders.count());s.put("completedOrders",orders.countByStatus(OrderStatus.COMPLETED));s.put("pendingReports",reports.countByStatus(ReportStatus.PENDING));return ApiResponse.ok(s);}
    @GetMapping("/users") public ApiResponse<List<UserView>> users(){return ApiResponse.ok(users.findAll(Sort.by("createdAt").descending()).stream().map(mapper::user).toList());}
    @PostMapping("/users/{id}/{action}") @Transactional public ApiResponse<Void> userAction(@PathVariable Long id,@PathVariable String action){if(!Set.of("ban","unban").contains(action))throw new BusinessException(40001,"不支持的用户操作");User u=users.findById(id).orElseThrow(()->new BusinessException(40401,"用户不存在"));if(u.role==Role.ADMIN)throw new BusinessException(40301,"不能封禁管理员");u.status="ban".equals(action)?UserStatus.BANNED:UserStatus.ACTIVE;return ApiResponse.ok();}
    @GetMapping("/products") public ApiResponse<Page<ProductView>> products(@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="50")int size){int safeSize=size<=0?50:Math.min(size,50);return ApiResponse.ok(products.findAll(PageRequest.of(Math.max(0,page),safeSize,Sort.by("createdAt").descending())).map(p->mapper.product(p,null)));}
    @PostMapping("/products/{id}/off-shelf") @Transactional public ApiResponse<Void> offShelf(@PathVariable Long id){Product p=products.findForUpdate(id).orElseThrow(()->new BusinessException(40401,"商品不存在"));if(p.status==ProductStatus.RESERVED)throw new BusinessException(40002,"已有进行中订单，不能下架");if(p.status==ProductStatus.SOLD)throw new BusinessException(40002,"已售商品不能下架");if(p.status==ProductStatus.OFF_SHELF)return ApiResponse.ok();p.status=ProductStatus.OFF_SHELF;p.updatedAt=LocalDateTime.now();return ApiResponse.ok();}
    @GetMapping("/orders") public ApiResponse<List<OrderView>> allOrders(){return ApiResponse.ok(orders.findAll(Sort.by("createdAt").descending()).stream().map(o->mapper.order(o,null)).toList());}
    @GetMapping("/reports") public ApiResponse<List<ReportView>> reports(){return ApiResponse.ok(reports.findAllByOrderByCreatedAtDesc().stream().map(mapper::report).toList());}
    public record HandleRequest(String result,Boolean banSeller){}
    @PostMapping("/reports/{id}/{action}") @Transactional public ApiResponse<ReportView> reportAction(@PathVariable Long id,@PathVariable String action,@RequestBody(required=false)HandleRequest req){if(!Set.of("approve","reject").contains(action))throw new BusinessException(40001,"不支持的举报操作");Report r=reports.findForUpdate(id).orElseThrow(()->new BusinessException(40401,"举报不存在"));if(r.status!=ReportStatus.PENDING)throw new BusinessException(40001,"举报已处理");r.admin=current.require();r.handledAt=LocalDateTime.now();r.handleResult=req==null?null:req.result();if("approve".equals(action)){r.status=ReportStatus.APPROVED;r.product=products.findForUpdate(r.product.id).orElseThrow(()->new BusinessException(40401,"商品不存在"));if(r.product.status==ProductStatus.RESERVED)throw new BusinessException(40002,"商品存在进行中订单，暂不能下架");r.product.status=ProductStatus.OFF_SHELF;r.product.updatedAt=LocalDateTime.now();if(req!=null&&Boolean.TRUE.equals(req.banSeller()))r.product.seller.status=UserStatus.BANNED;}else r.status=ReportStatus.REJECTED;return ApiResponse.ok(mapper.report(r));}
    public record CategoryRequest(String name,Integer sortOrder,Boolean active){}
    @PostMapping("/categories") public ApiResponse<CategoryView> addCategory(@RequestBody CategoryRequest req){String name=categoryName(req.name());if(categories.existsByNameIgnoreCase(name))throw new BusinessException(40001,"分类名称已存在");Category c=new Category();c.name=name;c.sortOrder=req.sortOrder()==null?0:req.sortOrder();c.active=req.active()==null||req.active();categories.save(c);return ApiResponse.ok(new CategoryView(c.id,c.name,c.sortOrder,c.active));}
    @PutMapping("/categories/{id}") @Transactional public ApiResponse<CategoryView> updateCategory(@PathVariable Long id,@RequestBody CategoryRequest req){Category c=categories.findById(id).orElseThrow(()->new BusinessException(40401,"分类不存在"));if(req.name()!=null){String name=categoryName(req.name());if(categories.existsByNameIgnoreCaseAndIdNot(name,id))throw new BusinessException(40001,"分类名称已存在");c.name=name;}if(req.sortOrder()!=null)c.sortOrder=req.sortOrder();if(req.active()!=null)c.active=req.active();return ApiResponse.ok(new CategoryView(c.id,c.name,c.sortOrder,c.active));}
    @GetMapping("/categories") public ApiResponse<List<CategoryView>> categories(){return ApiResponse.ok(categories.findAllByOrderBySortOrderAsc().stream().map(c->new CategoryView(c.id,c.name,c.sortOrder,c.active)).toList());}
    private String categoryName(String name){if(name==null||name.isBlank())throw new BusinessException(40001,"分类名称不能为空");String value=name.trim();if(value.length()>50)throw new BusinessException(40001,"分类名称不能超过 50 字");return value;}
}
