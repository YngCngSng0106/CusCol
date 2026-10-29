package com.zlyd.customercollection.service;

import com.zlyd.customercollection.common.BusinessException;
import com.zlyd.customercollection.dto.CaptchaResponse;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private static final String CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final long EXPIRE_SECONDS = 300;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 42;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, CaptchaEntry> cache = new ConcurrentHashMap<>();

    public CaptchaResponse create() {
        String code = randomCode();
        String key = UUID.randomUUID().toString().replace("-", "");
        cache.put(key, new CaptchaEntry(code, Instant.now().plusSeconds(EXPIRE_SECONDS)));
        cleanExpired();
        return new CaptchaResponse(key, "data:image/png;base64," + draw(code));
    }

    public void validate(String key, String code) {
        if (key == null || code == null) {
            throw new BusinessException("验证码已过期，请刷新");
        }
        CaptchaEntry entry = cache.get(key);
        if (entry == null || entry.expireAt().isBefore(Instant.now())) {
            cache.remove(key);
            throw new BusinessException("验证码已过期，请刷新");
        }
        if (!entry.code().equalsIgnoreCase(code.trim())) {
            throw new BusinessException("验证码错误");
        }
        cache.remove(key);
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            builder.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return builder.toString();
    }

    private String draw(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(245, 247, 250));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        for (int i = 0; i < 6; i++) {
            graphics.setColor(randomColor(150, 220));
            graphics.setStroke(new BasicStroke(1.2f));
            graphics.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }
        graphics.setFont(new Font("Arial", Font.BOLD, 26));
        for (int i = 0; i < code.length(); i++) {
            graphics.setColor(randomColor(30, 120));
            graphics.drawString(String.valueOf(code.charAt(i)), 12 + i * 26, 30 + random.nextInt(4));
        }
        graphics.dispose();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            throw new BusinessException("验证码生成失败");
        }
    }

    private Color randomColor(int min, int max) {
        int range = max - min;
        return new Color(min + random.nextInt(range), min + random.nextInt(range), min + random.nextInt(range));
    }

    private void cleanExpired() {
        Instant now = Instant.now();
        cache.entrySet().removeIf(entry -> entry.getValue().expireAt().isBefore(now));
    }

    private record CaptchaEntry(String code, Instant expireAt) {
    }
}
