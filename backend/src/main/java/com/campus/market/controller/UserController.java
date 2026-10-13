package com.campus.market.controller;

import com.campus.market.common.*;
import com.campus.market.dto.Views.*;
import com.campus.market.entity.User;
import com.campus.market.repository.*;
import com.campus.market.security.CurrentUser;
import com.campus.market.service.ViewMapper;
import com.campus.market.service.EmailVerificationService;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@RestController @RequestMapping("/api/users")
public class UserController {
    private final CurrentUser current; private final UserRepository users; private final ReviewRepository reviews; private final ViewMapper mapper;
    public UserController(CurrentUser current,UserRepository users,ReviewRepository reviews,ViewMapper mapper){this.current=current;this.users=users;this.reviews=reviews;this.mapper=mapper;}
    @GetMapping("/me") public ApiResponse<UserView> me(){return ApiResponse.ok(mapper.user(current.require()));}
    public record UpdateRequest(@Size(max=50,message="昵称不能超过 50 字") String nickname,@Size(max=255,message="头像地址过长") String avatar,@Email(message="邮箱格式错误") @Size(max=100,message="邮箱过长") String email,@Size(max=30,message="手机号过长") String phone,@Size(max=100,message="校区名称过长") String campus,@Size(max=100,message="宿舍区域过长") String dormArea,@Size(max=255,message="个人简介不能超过 255 字") String bio){}
    @PutMapping("/me") @Transactional public ApiResponse<UserView> update(@Valid @RequestBody UpdateRequest r){ User u=current.require();
        if(r.nickname()!=null){if(r.nickname().isBlank())throw new BusinessException(40001,"昵称不能为空");u.nickname=r.nickname().trim();} if(r.avatar()!=null)u.avatar=r.avatar(); if(r.email()!=null&&!java.util.Objects.equals(EmailVerificationService.normalize(r.email()),u.email))throw new BusinessException(40001,"注册邮箱不能直接修改"); if(r.phone()!=null)u.phone=r.phone(); if(r.campus()!=null)u.campus=r.campus(); if(r.dormArea()!=null)u.dormArea=r.dormArea(); if(r.bio()!=null)u.bio=r.bio(); u.updatedAt=LocalDateTime.now(); return ApiResponse.ok(mapper.user(u)); }
    @GetMapping("/{id}") public ApiResponse<UserView> user(@PathVariable Long id){return ApiResponse.ok(mapper.user(users.findById(id).orElseThrow(()->new BusinessException(40401,"用户不存在"))));}
    @GetMapping("/{id}/reviews") public ApiResponse<List<ReviewView>> reviews(@PathVariable Long id){return ApiResponse.ok(reviews.findByTargetUserIdOrderByCreatedAtDesc(id).stream().map(mapper::review).toList());}
}
