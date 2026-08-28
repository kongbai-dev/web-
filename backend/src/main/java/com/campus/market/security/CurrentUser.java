package com.campus.market.security;

import com.campus.market.common.BusinessException;
import com.campus.market.entity.User;
import com.campus.market.entity.Enums.UserStatus;
import com.campus.market.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final UserRepository users;
    public CurrentUser(UserRepository users) { this.users = users; }
    public User require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
            throw new BusinessException(40101, "请先登录");
        User user = users.findById(Long.valueOf(auth.getName())).orElseThrow(() -> new BusinessException(40101,"用户不存在"));
        if (user.status == UserStatus.BANNED) throw new BusinessException(40302, "账号已被封禁");
        return user;
    }
    public User optional() {
        try { return require(); } catch (BusinessException e) { return null; }
    }
}
