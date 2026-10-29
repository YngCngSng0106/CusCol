package com.zlyd.customercollection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_verification_code")
public class VerificationCode {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String target;
    private Integer targetType;
    private Integer scene;
    private String code;
    private LocalDateTime expireTime;
    private Integer izUsed;
    private String sendIp;
    private LocalDateTime createTime;
}
