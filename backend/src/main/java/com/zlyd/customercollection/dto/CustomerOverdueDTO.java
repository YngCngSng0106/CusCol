package com.zlyd.customercollection.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerOverdueDTO {

    private Long id;
    private Long userId;
    private String customerName;
    private LocalDateTime lastVisitTime;
    private LocalDateTime createTime;
    private Integer overdueDays;
}
