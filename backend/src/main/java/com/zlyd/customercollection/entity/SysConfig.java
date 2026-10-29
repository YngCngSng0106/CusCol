package com.zlyd.customercollection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_config")
public class SysConfig {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String configKey;
    private String configValue;
    private String configName;
    private String remark;
    private Long updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
