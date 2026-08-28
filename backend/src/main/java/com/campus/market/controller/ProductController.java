package com.campus.market.controller;

import com.campus.market.common.ApiResponse;
import com.campus.market.dto.Views.*;
import com.campus.market.entity.Enums.ProductStatus;
import com.campus.market.repository.CategoryRepository;
import com.campus.market.service.*;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;

@RestController @RequestMapping("/api")
public class ProductController {
    private final ProductService service; private final CategoryRepository categories;
    public ProductController(ProductService service,CategoryRepository categories){this.service=service;this.categories=categories;}
    @GetMapping("/categories") public ApiResponse<List<CategoryView>> categories(){return ApiResponse.ok(categories.findAllByOrderBySortOrderAsc().stream().filter(c->c.active).map(c->new CategoryView(c.id,c.name,c.sortOrder,c.active)).toList());}
    @GetMapping("/products") public ApiResponse<Page<ProductView>> list(@RequestParam(required=false)String keyword,@RequestParam(required=false)Long categoryId,@RequestParam(required=false)BigDecimal minPrice,@RequestParam(required=false)BigDecimal maxPrice,@RequestParam(defaultValue="ON_SALE")ProductStatus status,@RequestParam(defaultValue="newest")String sort,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="12")int size){return ApiResponse.ok(service.search(keyword,categoryId,minPrice,maxPrice,status,sort,page,size));}
    @GetMapping("/products/{id}") public ApiResponse<ProductView> detail(@PathVariable Long id){return ApiResponse.ok(service.detail(id));}
    @PostMapping("/products") public ApiResponse<ProductView> create(@Valid @RequestBody ProductService.ProductRequest r){return ApiResponse.ok(service.create(r));}
    @PutMapping("/products/{id}") public ApiResponse<ProductView> update(@PathVariable Long id,@Valid @RequestBody ProductService.ProductRequest r){return ApiResponse.ok(service.update(id,r));}
    @DeleteMapping("/products/{id}") public ApiResponse<Void> delete(@PathVariable Long id){service.offShelf(id);return ApiResponse.ok();}
    @PostMapping("/products/{id}/off-shelf") public ApiResponse<Void> offShelf(@PathVariable Long id){service.offShelf(id);return ApiResponse.ok();}
    @PostMapping("/products/{id}/relist") public ApiResponse<Void> relist(@PathVariable Long id){service.relist(id);return ApiResponse.ok();}
    @PostMapping("/products/{id}/favorite") public ApiResponse<Boolean> favorite(@PathVariable Long id){return ApiResponse.ok(service.favorite(id));}
    @DeleteMapping("/products/{id}/favorite") public ApiResponse<Void> unfavorite(@PathVariable Long id){service.unfavorite(id);return ApiResponse.ok();}
    @GetMapping("/users/me/favorites") public ApiResponse<List<ProductView>> favorites(){return ApiResponse.ok(service.favorites());}
    @GetMapping("/users/me/history") public ApiResponse<List<ProductView>> history(){return ApiResponse.ok(service.history());}
    @GetMapping("/users/me/products") public ApiResponse<Page<ProductView>> mine(@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return ApiResponse.ok(service.mine(page,size));}
}
