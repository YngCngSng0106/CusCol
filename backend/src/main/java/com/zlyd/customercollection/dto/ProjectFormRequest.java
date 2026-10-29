package com.zlyd.customercollection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectFormRequest {

    @NotBlank(message = "请输入项目名称")
    private String projectName;

    private String projectType;
    private BigDecimal projectAmount;
    private Integer projectStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private String projectDesc;
    private List<Long> attachmentIds = new ArrayList<>();
}
