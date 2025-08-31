package com.evermodel.service.agg.aidemo.tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tianwl
 * @date 2025/8/31
 * @description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthCompo {
    /**
     * 套餐id
     */
    private Integer id;
    /**
     * 套餐名称
     */
    private String name;

    /**
     * 适用人群
     */
    private String applyUser;

    /**
     * 套餐价格
     */
    private double price;
}