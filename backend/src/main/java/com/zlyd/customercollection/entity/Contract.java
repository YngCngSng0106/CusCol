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
@TableName("contract")
public class Contract {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String contractNo;
    private Long customerId;
    private Long userId;
    private String contractName;
    private BigDecimal contractAmount;
    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer contractStatus;
    private Integer paymentStatus;
    private BigDecimal paidAmount;
    private String remark;
    @TableLogic
    private Integer izDeleted;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
