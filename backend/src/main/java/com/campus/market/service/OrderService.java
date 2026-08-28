package com.campus.market.service;

import com.campus.market.common.BusinessException;
import com.campus.market.dto.Views.OrderView;
import com.campus.market.entity.*;
import com.campus.market.entity.Enums.*;
import com.campus.market.repository.*;
import com.campus.market.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.*;

@Service
public class OrderService {
    private final OrderRepository orders; private final ProductRepository products; private final CurrentUser current; private final ViewMapper mapper;
    public OrderService(OrderRepository o,ProductRepository p,CurrentUser c,ViewMapper m){orders=o;products=p;current=c;mapper=m;}
    public record CreateOrderRequest(@NotNull(message="商品不能为空") Long productId,@Size(max=100,message="交易地点不能超过 100 字") String tradeLocation,@Size(max=255,message="备注不能超过 255 字") String buyerNote){}
    @Transactional public OrderView create(CreateOrderRequest r){ User buyer=current.require(); if(r.productId()==null)throw new BusinessException(40001,"商品不能为空"); Product p=products.findForUpdate(r.productId()).orElseThrow(()->new BusinessException(40401,"商品不存在"));
        if(p.seller.id.equals(buyer.id))throw new BusinessException(40002,"不能购买自己的商品"); if(p.status!=ProductStatus.ON_SALE)throw new BusinessException(40002,"商品当前不可交易");
        MarketOrder o=new MarketOrder();o.orderNo="ORD"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+UUID.randomUUID().toString().substring(0,6).toUpperCase();o.product=p;o.buyer=buyer;o.seller=p.seller;o.amount=p.price;o.tradeLocation=r.tradeLocation()==null?p.tradeLocation:r.tradeLocation();o.buyerNote=r.buyerNote();o.status=OrderStatus.PENDING_CONFIRM;p.status=ProductStatus.RESERVED;orders.save(o);return mapper.order(o,buyer.id); }
    public List<OrderView> mine(){User u=current.require();return orders.findMine(u.id).stream().map(o->mapper.order(o,u.id)).toList();}
    public OrderView detail(Long id){User u=current.require();MarketOrder o=find(id);participant(o,u);return mapper.order(o,u.id);}
    @Transactional public OrderView action(Long id,String action){User u=current.require();MarketOrder o=orders.findForUpdate(id).orElseThrow(()->new BusinessException(40402,"订单不存在"));LocalDateTime now=LocalDateTime.now();
        switch(action){
            case "confirm"->{seller(o,u);state(o,OrderStatus.PENDING_CONFIRM);o.status=OrderStatus.PENDING_PAYMENT;o.confirmedAt=now;}
            case "pay"->{buyer(o,u);state(o,OrderStatus.PENDING_PAYMENT);o.status=OrderStatus.PENDING_DELIVERY;o.paidAt=now;}
            case "deliver"->{seller(o,u);state(o,OrderStatus.PENDING_DELIVERY);o.status=OrderStatus.PENDING_RECEIVE;o.deliveredAt=now;}
            case "receive"->{buyer(o,u);state(o,OrderStatus.PENDING_RECEIVE);o.status=OrderStatus.COMPLETED;o.receivedAt=now;o.completedAt=now;o.product.status=ProductStatus.SOLD;}
            case "cancel"->{participant(o,u);if(o.status!=OrderStatus.PENDING_CONFIRM&&o.status!=OrderStatus.PENDING_PAYMENT)throw new BusinessException(40003,"当前订单状态不能取消");o.status=OrderStatus.CANCELLED;o.cancelledAt=now;o.product.status=ProductStatus.ON_SALE;}
            default->throw new BusinessException(40001,"未知操作");
        } return mapper.order(o,u.id);
    }
    private MarketOrder find(Long id){return orders.findById(id).orElseThrow(()->new BusinessException(40402,"订单不存在"));}
    private void participant(MarketOrder o,User u){if(!o.buyer.id.equals(u.id)&&!o.seller.id.equals(u.id))throw new BusinessException(40301,"无权查看此订单");}
    private void buyer(MarketOrder o,User u){if(!o.buyer.id.equals(u.id))throw new BusinessException(40301,"只有买家可以执行此操作");}
    private void seller(MarketOrder o,User u){if(!o.seller.id.equals(u.id))throw new BusinessException(40301,"只有卖家可以执行此操作");}
    private void state(MarketOrder o,OrderStatus s){if(o.status!=s)throw new BusinessException(40003,"订单状态错误");}
}
