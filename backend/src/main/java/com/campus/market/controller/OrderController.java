package com.campus.market.controller;

import com.campus.market.common.ApiResponse;
import com.campus.market.dto.Views.OrderView;
import com.campus.market.service.OrderService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController @RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service; public OrderController(OrderService s){service=s;}
    @PostMapping public ApiResponse<OrderView> create(@Valid @RequestBody OrderService.CreateOrderRequest r){return ApiResponse.ok(service.create(r));}
    @GetMapping public ApiResponse<List<OrderView>> list(){return ApiResponse.ok(service.mine());}
    @GetMapping("/{id}") public ApiResponse<OrderView> detail(@PathVariable Long id){return ApiResponse.ok(service.detail(id));}
    @PostMapping("/{id}/{action}") public ApiResponse<OrderView> action(@PathVariable Long id,@PathVariable String action){return ApiResponse.ok(service.action(id,action));}
}
