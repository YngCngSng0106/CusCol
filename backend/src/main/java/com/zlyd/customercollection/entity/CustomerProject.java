package com.zlyd.customercollection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("customer_project")
public class CustomerProject {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private Long userId;
    private String projectName;
    private String projectType;
    private BigDecimal projectAmount;
    private Integer projectStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private String projectDesc;
    @TableLogic
    private Integer izDeleted;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
