package com.zlyd.customercollection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("visit_record")
public class VisitRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private Long userId;
    private Integer visitType;
    private LocalDateTime visitTime;
    private String visitLocation;
    private String visitPurpose;
    private String visitContent;
    private String visitResult;
    private LocalDateTime nextVisitTime;
    @TableLogic
    private Integer izDeleted;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
