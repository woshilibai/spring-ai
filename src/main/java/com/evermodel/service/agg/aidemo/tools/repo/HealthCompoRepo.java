package com.evermodel.service.agg.aidemo.tools.repo;

import com.evermodel.service.agg.aidemo.tools.dto.AppointmentOrder;
import com.evermodel.service.agg.aidemo.tools.dto.HealthCompo;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author tianwl
 * @date 2025/8/31
 * @description 体检套餐仓库
 */
@Component
public class HealthCompoRepo {

    private static List<HealthCompo> healthCompos;

    @PostConstruct
    public void init() {
        healthCompos = List.of(
                new HealthCompo(1,"基础健康筛查套餐", "适合年轻、无重大疾病史且希望进行基础健康检查的人群。", 199),
                new HealthCompo(2,"全面综合体检套餐", "适用于各年龄段，尤其是有家族病史、长期处于不良工作环境或生活习惯不佳的人群，进行较全面的健康评估。", 299),
                new HealthCompo(3,"高端深度体检套餐", "适合年龄较大、有慢性疾病家族史、高风险职业人群或对自身健康有更高要求，希望进行深度健康检查的人士。", 399)
        );
    }

    public List<HealthCompo> getAllHealthCompos() {
        return healthCompos;
    }

}