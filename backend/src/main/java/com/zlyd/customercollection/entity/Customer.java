package com.zlyd.customercollection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("customer")
public class Customer {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String customerName;
    private Integer customerLevel;
    private String industry;
    private String city;
    private String address;
    private String contactName;
    private String contactPhone;
    private String source;
    private Integer status;
    private Integer izValid;
    private LocalDateTime lastVisitTime;
    private String remark;
    @TableLogic
    private Integer izDeleted;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
