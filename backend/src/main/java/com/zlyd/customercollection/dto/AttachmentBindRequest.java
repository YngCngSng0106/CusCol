package com.zlyd.customercollection.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AttachmentBindRequest {

    @NotEmpty(message = "请选择附件")
    private List<Long> ids;

    @NotNull(message = "请指定业务类型")
    private Integer bizType;

    @NotNull(message = "请指定业务ID")
    private Long bizId;
}
