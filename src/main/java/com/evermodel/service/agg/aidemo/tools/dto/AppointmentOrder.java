package com.evermodel.service.agg.aidemo.tools.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author tianwl
 * @date 2025/8/31
 * @description 体检预约单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentOrder {
    private Integer id;
    private String userName;
    private String idNo;
    private String phone;
    private String orderDate;
}