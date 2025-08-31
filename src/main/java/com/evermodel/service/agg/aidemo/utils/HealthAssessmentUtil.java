package com.evermodel.service.agg.aidemo.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 健康评估工具类，根据性别(1男0女)、身高和体重评估健康程度
 */
public class HealthAssessmentUtil {
    
    /**
     * 健康程度枚举
     */
    public enum HealthLevel {
        VERY_HEALTHY("非常健康"),
        HEALTHY("正常"),
        MODERATE("一般"),
        SUB_HEALTHY("亚健康"),
        VERY_UNHEALTHY("很不健康");
        
        private final String description;
        
        HealthLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 根据性别(1男0女)、身高(厘米)和体重(千克)评估健康程度
     * 
     * @param gender 性别，1为男性，0为女性
     * @param height 身高，单位：厘米
     * @param weight 体重，单位：千克
     * @return 健康程度枚举
     * @throws IllegalArgumentException 如果输入参数不合法
     */
    public static HealthLevel assessHealth(double height, double weight) {
        int gender = 1;
        // 验证性别参数
        if (gender != 0 && gender != 1) {
            throw new IllegalArgumentException("性别参数必须为0(女性)或1(男性)");
        }
        
        // 验证身高体重参数
        validateHeightAndWeight(height, weight);
        
        // 计算BMI指数：体重(kg) / 身高(m)的平方
        double heightInMeter = height / 100;
        double bmi = weight / (heightInMeter * heightInMeter);
        
        // 保留两位小数
        bmi = new BigDecimal(bmi).setScale(2, RoundingMode.HALF_UP).doubleValue();
        
        // 根据性别和BMI值判断健康程度
        return determineHealthLevel(gender, bmi);
    }
    
    /**
     * 验证身高和体重参数的合法性
     */
    private static void validateHeightAndWeight(double height, double weight) {
        if (height <= 0 || height > 250) {
            throw new IllegalArgumentException("身高必须在0-250厘米之间");
        }
        if (weight <= 0 || weight > 300) {
            throw new IllegalArgumentException("体重必须在0-300千克之间");
        }
    }
    
    /**
     * 根据性别和BMI值判断健康程度
     */
    private static HealthLevel determineHealthLevel(int gender, double bmi) {
        // 1表示男性，0表示女性
        if (gender == 1) {
            // 男性判断标准
            if (bmi < 18.5) {
                return HealthLevel.SUB_HEALTHY;
            } else if (bmi < 22.9) {
                return HealthLevel.HEALTHY;
            } else if (bmi < 24.9) {
                return HealthLevel.MODERATE;
            } else if (bmi < 29.9) {
                return HealthLevel.SUB_HEALTHY;
            } else {
                return HealthLevel.VERY_UNHEALTHY;
            }
        } else {
            // 女性判断标准
            if (bmi < 17.5) {
                return HealthLevel.SUB_HEALTHY;
            } else if (bmi < 21.9) {
                return HealthLevel.HEALTHY;
            } else if (bmi < 23.9) {
                return HealthLevel.MODERATE;
            } else if (bmi < 28.9) {
                return HealthLevel.SUB_HEALTHY;
            } else {
                return HealthLevel.VERY_UNHEALTHY;
            }
        }
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试案例
        testCase(1, 175, 70);   // 男性，175cm，70kg
        testCase(0, 160, 50);   // 女性，160cm，50kg
        testCase(1, 180, 90);   // 男性，180cm，90kg
        testCase(0, 155, 70);   // 女性，155cm，70kg
        testCase(1, 170, 50);   // 男性，170cm，50kg
        testCase(2, 170, 60);   // 无效性别测试
    }
    
    /**
     * 测试用例辅助方法
     */
    private static void testCase(int gender, double height, double weight) {
        try {
            HealthLevel level = assessHealth( height, weight);
            System.out.printf("性别：%s，身高：%.1fcm，体重：%.1fkg，健康程度：%s%n",
                    gender == 1 ? "男" : "女", height, weight, level.getDescription());
        } catch (IllegalArgumentException e) {
            System.out.printf("测试失败(性别：%d，身高：%.1fcm，体重：%.1fkg)：%s%n",
                    gender, height, weight, e.getMessage());
        }
    }
}
