package com.zlyd.customercollection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ContractFormRequest {

    @NotNull(message = "请选择客户")
    private Long customerId;

    @NotBlank(message = "请输入合同编号")
    private String contractNo;

    @NotBlank(message = "请输入合同名称")
    private String contractName;

    @NotNull(message = "请输入合同金额")
    private BigDecimal contractAmount;

    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer contractStatus;
    private Integer paymentStatus;
    private BigDecimal paidAmount;
    private String remark;
    private List<Long> attachmentIds = new ArrayList<>();
}
