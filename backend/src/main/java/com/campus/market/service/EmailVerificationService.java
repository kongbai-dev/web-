package com.campus.market.service;

import com.campus.market.common.BusinessException;
import com.campus.market.entity.EmailVerificationCode;
import com.campus.market.repository.EmailVerificationCodeRepository;
import com.campus.market.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;
import java.util.Locale;

@Service
public class EmailVerificationService {
    private final EmailVerificationCodeRepository codes;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();
    private final String from;
    private final long intervalSeconds;
    private final long expirationMinutes;

    public EmailVerificationService(EmailVerificationCodeRepository codes, UserRepository users,
                                    PasswordEncoder encoder, JavaMailSender mailSender,
                                    @Value("${app.mail.from:}") String from,
                                    @Value("${app.mail.code-send-interval-seconds:60}") long intervalSeconds,
                                    @Value("${app.mail.code-expiration-minutes:10}") long expirationMinutes) {
        this.codes=codes;this.users=users;this.encoder=encoder;this.mailSender=mailSender;this.from=from.trim();
        this.intervalSeconds=Math.max(60,intervalSeconds);this.expirationMinutes=Math.max(1,expirationMinutes);
    }

    public static String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    public long expirationSeconds(){return expirationMinutes*60;}

    @Transactional
    public synchronized long sendRegistrationCode(String rawEmail) {
        String email=normalize(rawEmail);
        if(users.existsByEmailIgnoreCase(email))throw new BusinessException(40001,"该邮箱已注册");
        if(from.isBlank())throw new BusinessException(50001,"邮件服务未配置");
        LocalDateTime now=LocalDateTime.now();
        EmailVerificationCode record=codes.findForUpdate(email).orElseGet(EmailVerificationCode::new);
        if(record.lastSentAt!=null){
            long elapsed=Math.max(0,Duration.between(record.lastSentAt,now).getSeconds());
            if(elapsed<intervalSeconds)throw new BusinessException(40002,"请在 "+(intervalSeconds-elapsed)+" 秒后重新发送");
        }
        String code=String.format("%06d",random.nextInt(1_000_000));
        record.email=email;record.codeHash=encoder.encode(code);record.lastSentAt=now;
        record.expiresAt=now.plusMinutes(expirationMinutes);record.attempts=0;
        codes.saveAndFlush(record);
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom(from);message.setTo(email);message.setSubject("[南小集] 注册验证码");
        message.setText("您的注册验证码是："+code+"\n\n验证码 "+expirationMinutes+" 分钟内有效。若非本人操作，请忽略本邮件。");
        try { mailSender.send(message); }
        catch(MailException e){throw new BusinessException(50002,"验证码发送失败，请稍后重试");}
        return intervalSeconds;
    }

    @Transactional
    public boolean verifyAndConsume(String rawEmail,String code){
        String email=normalize(rawEmail);
        EmailVerificationCode record=codes.findForUpdate(email).orElse(null);
        if(record==null)return false;
        if(record.expiresAt.isBefore(LocalDateTime.now())){codes.delete(record);return false;}
        if(record.attempts>=5){codes.delete(record);return false;}
        if(!encoder.matches(code,record.codeHash)){
            record.attempts++;
            if(record.attempts>=5)codes.delete(record);
            return false;
        }
        codes.delete(record);
        return true;
    }
}
