package com.campus.market.controller;

import com.campus.market.common.*;
import com.campus.market.entity.User;
import com.campus.market.entity.Enums.UserStatus;
import com.campus.market.repository.UserRepository;
import com.campus.market.security.JwtService;
import com.campus.market.service.EmailVerificationService;
import com.campus.market.service.ViewMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt; private final ViewMapper mapper; private final EmailVerificationService emailVerification;
    public AuthController(UserRepository users,PasswordEncoder encoder,JwtService jwt,ViewMapper mapper,EmailVerificationService emailVerification){this.users=users;this.encoder=encoder;this.jwt=jwt;this.mapper=mapper;this.emailVerification=emailVerification;}
    public record EmailCodeRequest(@NotBlank(message="请输入邮箱") @Email(message="邮箱格式错误") @Size(max=100,message="邮箱过长") String email){}
    public record RegisterRequest(@NotBlank(message="用户名不能为空") @Size(max=50,message="用户名不能超过 50 字") String username,@NotBlank(message="密码不能为空") @Size(min=6,max=72,message="密码长度须为 6~72 位") String password,@NotBlank(message="学号不能为空") @Size(max=30,message="学号不能超过 30 字") String studentNo,@NotBlank(message="昵称不能为空") @Size(max=50,message="昵称不能超过 50 字") String nickname,@NotBlank(message="请输入邮箱") @Email(message="邮箱格式错误") @Size(max=100,message="邮箱过长") String email,@NotBlank(message="请输入邮箱验证码") @Pattern(regexp="\\d{6}",message="邮箱验证码须为 6 位数字") String emailCode,@Size(max=30,message="手机号过长") String phone){}
    public record LoginRequest(@NotBlank(message="请输入用户名或邮箱") String username,@NotBlank(message="请输入密码") String password){}
    @PostMapping("/email-code") public ApiResponse<?> emailCode(@Valid @RequestBody EmailCodeRequest r){
        long retryAfter=emailVerification.sendRegistrationCode(r.email());
        return ApiResponse.ok(Map.of("retryAfterSeconds",retryAfter,"expiresInSeconds",emailVerification.expirationSeconds()));
    }
    @PostMapping("/register") public ApiResponse<?> register(@Valid @RequestBody RegisterRequest r){
        String username=r.username().trim(),studentNo=r.studentNo().trim(),email=EmailVerificationService.normalize(r.email());
        if(users.existsByUsername(username)) throw new BusinessException(40001,"用户名已存在");
        if(users.existsByStudentNo(studentNo)) throw new BusinessException(40001,"学号已存在");
        if(users.existsByEmailIgnoreCase(email)) throw new BusinessException(40001,"该邮箱已注册");
        if(!emailVerification.verifyAndConsume(email,r.emailCode()))throw new BusinessException(40003,"邮箱验证码错误或已过期");
        User u=new User(); u.username=username;u.passwordHash=encoder.encode(r.password());u.studentNo=studentNo;u.nickname=r.nickname().trim();u.email=email;u.phone=r.phone(); users.save(u);
        return ApiResponse.ok(Map.of("token",jwt.create(u),"user",mapper.user(u)));
    }
    @PostMapping("/login") public ApiResponse<?> login(@Valid @RequestBody LoginRequest r){
        String account=r.username().trim();
        User u=users.findByUsername(account).or(()->users.findByEmailIgnoreCase(account)).orElseThrow(()->new BusinessException(40101,"账号或密码错误"));
        if(!encoder.matches(r.password(),u.passwordHash)) throw new BusinessException(40101,"账号或密码错误");
        if(u.status==UserStatus.BANNED) throw new BusinessException(40302,"账号已被封禁");
        return ApiResponse.ok(Map.of("token",jwt.create(u),"user",mapper.user(u)));
    }
}
