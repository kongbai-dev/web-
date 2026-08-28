package com.campus.market.controller;

import com.campus.market.common.*;
import com.campus.market.entity.User;
import com.campus.market.entity.Enums.UserStatus;
import com.campus.market.repository.UserRepository;
import com.campus.market.security.JwtService;
import com.campus.market.service.ViewMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt; private final ViewMapper mapper;
    public AuthController(UserRepository users,PasswordEncoder encoder,JwtService jwt,ViewMapper mapper){this.users=users;this.encoder=encoder;this.jwt=jwt;this.mapper=mapper;}
    public record RegisterRequest(@NotBlank(message="用户名不能为空") @Size(max=50,message="用户名不能超过 50 字") String username,@NotBlank(message="密码不能为空") @Size(min=6,max=72,message="密码长度须为 6~72 位") String password,@NotBlank(message="学号不能为空") @Size(max=30,message="学号不能超过 30 字") String studentNo,@NotBlank(message="昵称不能为空") @Size(max=50,message="昵称不能超过 50 字") String nickname,@Email(message="邮箱格式错误") @Size(max=100,message="邮箱过长") String email,@Size(max=30,message="手机号过长") String phone){}
    public record LoginRequest(@NotBlank(message="请输入用户名") String username,@NotBlank(message="请输入密码") String password){}
    @PostMapping("/register") public ApiResponse<?> register(@Valid @RequestBody RegisterRequest r){
        if(users.existsByUsername(r.username())) throw new BusinessException(40001,"用户名已存在");
        if(users.existsByStudentNo(r.studentNo())) throw new BusinessException(40001,"学号已存在");
        User u=new User(); u.username=r.username();u.passwordHash=encoder.encode(r.password());u.studentNo=r.studentNo();u.nickname=r.nickname();u.email=r.email();u.phone=r.phone(); users.save(u);
        return ApiResponse.ok(Map.of("token",jwt.create(u),"user",mapper.user(u)));
    }
    @PostMapping("/login") public ApiResponse<?> login(@Valid @RequestBody LoginRequest r){
        User u=users.findByUsername(r.username()).orElseThrow(()->new BusinessException(40101,"用户名或密码错误"));
        if(!encoder.matches(r.password(),u.passwordHash)) throw new BusinessException(40101,"用户名或密码错误");
        if(u.status==UserStatus.BANNED) throw new BusinessException(40302,"账号已被封禁");
        return ApiResponse.ok(Map.of("token",jwt.create(u),"user",mapper.user(u)));
    }
}
