package com.zlyd.customercollection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CustomerFormRequest {

    @NotBlank(message = "请输入客户名称")
    private String customerName;

    private Integer customerLevel;
    private String industry;
    private String city;
    private String address;
    private String contactName;
    private String contactPhone;
    private String source;
    private Integer status;
    private String remark;
    private List<Long> attachmentIds = new ArrayList<>();
}
