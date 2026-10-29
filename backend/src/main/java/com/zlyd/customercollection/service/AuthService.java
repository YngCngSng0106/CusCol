package com.zlyd.customercollection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zlyd.customercollection.common.BusinessException;
import com.zlyd.customercollection.config.AppProperties;
import com.zlyd.customercollection.dto.EmailCodeResponse;
import com.zlyd.customercollection.dto.LoginRequest;
import com.zlyd.customercollection.dto.LoginResponse;
import com.zlyd.customercollection.dto.RegisterRequest;
import com.zlyd.customercollection.entity.User;
import com.zlyd.customercollection.entity.VerificationCode;
import com.zlyd.customercollection.mapper.UserMapper;
import com.zlyd.customercollection.mapper.VerificationCodeMapper;
import com.zlyd.customercollection.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserMapper userMapper;
    private final VerificationCodeMapper verificationCodeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;
    private final UserService userService;
    private final AppProperties properties;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    private final SecureRandom random = new SecureRandom();

    public LoginResponse login(LoginRequest request) {
        if (properties.isCaptchaEnabled()) {
            captchaService.validate(request.getCaptchaKey(), request.getCaptchaCode());
        }
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, request.getUsername().trim())
                .last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已停用，请联系管理员");
        }
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        String token = jwtUtil.createToken(user.getId(), user.getUsername());
        return new LoginResponse(token, userService.buildProfile(user));
    }

    @Transactional
    public void register(RegisterRequest request) {
        String phone = request.getPhone().trim();
        String email = request.getEmail().trim().toLowerCase();
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException("手机号格式不正确");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("邮箱格式不正确");
        }
        if (userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername().trim())) > 0) {
            throw new BusinessException("用户名已被注册");
        }
        if (userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getPhone, phone)) > 0) {
            throw new BusinessException("手机号已被注册");
        }
        if (userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getEmail, email)) > 0) {
            throw new BusinessException("邮箱已被注册");
        }
        verifyCode(email, 1, request.getCode());

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus(1);
        user.setIzDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Transactional
    public EmailCodeResponse sendEmailCode(String email, Integer scene, String ip) {
        String target = email == null ? "" : email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(target).matches()) {
            throw new BusinessException("邮箱格式不正确");
        }
        int targetScene = scene == null ? 1 : scene;
        LocalDateTime now = LocalDateTime.now();
        VerificationCode latest = verificationCodeMapper.selectOne(Wrappers.<VerificationCode>lambdaQuery()
                .eq(VerificationCode::getTarget, target)
                .eq(VerificationCode::getScene, targetScene)
                .orderByDesc(VerificationCode::getId)
                .last("LIMIT 1"));
        if (latest != null && latest.getCreateTime() != null
                && latest.getCreateTime().isAfter(now.minusSeconds(60))) {
            throw new BusinessException("验证码发送过于频繁，请60秒后再试");
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        VerificationCode entity = new VerificationCode();
        entity.setTarget(target);
        entity.setTargetType(2);
        entity.setScene(targetScene);
        entity.setCode(code);
        entity.setExpireTime(now.plusMinutes(properties.getMail().getCodeExpireMinutes()));
        entity.setIzUsed(0);
        entity.setSendIp(ip);
        entity.setCreateTime(now);
        verificationCodeMapper.insert(entity);

        String devCode = null;
        if (properties.getMail().isEnabled()) {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null) {
                log.warn("邮件服务未配置，验证码仅输出到日志");
                devCode = code;
            } else {
                try {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(properties.getMail().getFrom());
                    message.setTo(target);
                    message.setSubject("客户信息APP验证码");
                    message.setText("您的验证码是：" + code + "，"
                            + properties.getMail().getCodeExpireMinutes() + "分钟内有效，请勿泄露。");
                    mailSender.send(message);
                } catch (Exception e) {
                    log.error("发送邮箱验证码失败: {}", target, e);
                    throw new BusinessException("邮箱有误，无法验证，请确认邮箱是否填写正确");
                }
            }
        } else {
            log.info("开发模式邮箱验证码 target={}, code={}", target, code);
            devCode = code;
        }
        return new EmailCodeResponse("验证码已发送，请查收邮箱", devCode);
    }

    private void verifyCode(String email, int scene, String code) {
        VerificationCode entity = verificationCodeMapper.selectOne(Wrappers.<VerificationCode>lambdaQuery()
                .eq(VerificationCode::getTarget, email)
                .eq(VerificationCode::getScene, scene)
                .eq(VerificationCode::getIzUsed, 0)
                .orderByDesc(VerificationCode::getId)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException("请先获取邮箱验证码");
        }
        if (entity.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!entity.getCode().equals(code.trim())) {
            throw new BusinessException("邮箱验证码错误");
        }
        entity.setIzUsed(1);
        verificationCodeMapper.updateById(entity);
    }

    public void logout() {
        // JWT 无状态，客户端清除 token 即可
    }
}
