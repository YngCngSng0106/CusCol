package com.zlyd.customercollection.service;

import com.zlyd.customercollection.entity.SysConfig;
import com.zlyd.customercollection.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final SysConfigMapper sysConfigMapper;

    public String get(String key, String defaultValue) {
        SysConfig config = sysConfigMapper.selectOne(com.baomidou.mybatisplus.core.toolkit.Wrappers
                .<SysConfig>lambdaQuery()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        return config == null || config.getConfigValue() == null ? defaultValue : config.getConfigValue();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }
}
