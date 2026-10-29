package com.zlyd.customercollection.dto;

import com.zlyd.customercollection.entity.Attachment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContractVO {

    private Long id;
    private String contractNo;
    private Long customerId;
    private String customerName;
    private String contractName;
    private BigDecimal contractAmount;
    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer contractStatus;
    private Integer paymentStatus;
    private BigDecimal paidAmount;
    private String remark;
    private LocalDateTime createTime;
    private List<Attachment> attachments;
}
