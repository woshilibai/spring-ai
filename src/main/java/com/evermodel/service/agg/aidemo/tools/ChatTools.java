package com.evermodel.service.agg.aidemo.tools;

import com.evermodel.service.agg.aidemo.tools.dto.AppointmentOrder;
import com.evermodel.service.agg.aidemo.tools.dto.HealthCompo;
import com.evermodel.service.agg.aidemo.tools.repo.AppointmentOrderRepo;
import com.evermodel.service.agg.aidemo.tools.repo.HealthCompoRepo;
import com.evermodel.service.agg.aidemo.utils.HealthAssessmentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author tianwl
 * @date 2025/8/31
 * @description
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatTools {

    private final HealthCompoRepo userRepo;

    private final AppointmentOrderRepo appointmentOrderRepo;

    @Tool(description = "根据身高和体重查询健康等级")
    public String getHealth(@ToolParam (description = "身高，单位为厘米") double height,
                            @ToolParam (description = "体重，单位为千克") double weight){
        log.info("=======tools开始查询健康等级，身高：{}，体重：{}", height, weight);
        HealthAssessmentUtil.HealthLevel healthLevel = HealthAssessmentUtil.assessHealth( height, weight);
        return healthLevel.getDescription();
    }

    @Tool(description = "查询所有体检套餐")
    public List<HealthCompo> getHealthCompos(){
        log.info("=======tools开始查询所有体检套餐");
        return userRepo.getAllHealthCompos();
    }


    @Tool(description = "保存体检预约单")
    public Integer saveAppointmentOrder(@ToolParam(description = "用户姓名") String userName,
                                        @ToolParam(description = "身份证号") String idNo,
                                        @ToolParam(description = "联系方式") String phone){
        log.info("=======tools开始保存体检预约单");
        return appointmentOrderRepo.addAppointmentOrder(userName, idNo, phone);
    }

}