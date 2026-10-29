package com.zlyd.customercollection.dto;

import com.zlyd.customercollection.entity.Attachment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VisitVO {

    private Long id;
    private Long customerId;
    private String customerName;
    private Integer visitType;
    private LocalDateTime visitTime;
    private String visitLocation;
    private String visitPurpose;
    private String visitContent;
    private String visitResult;
    private LocalDateTime nextVisitTime;
    private LocalDateTime createTime;
    private List<Attachment> attachments;
}
