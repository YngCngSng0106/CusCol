package com.zlyd.customercollection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class VisitFormRequest {

    @NotNull(message = "请选择客户")
    private Long customerId;

    @NotNull(message = "请选择拜访方式")
    private Integer visitType;

    @NotNull(message = "请选择拜访时间")
    private LocalDateTime visitTime;

    private String visitLocation;
    private String visitPurpose;

    @NotBlank(message = "请输入拜访内容")
    private String visitContent;

    private String visitResult;
    private LocalDateTime nextVisitTime;
    private List<Long> attachmentIds = new ArrayList<>();
}
