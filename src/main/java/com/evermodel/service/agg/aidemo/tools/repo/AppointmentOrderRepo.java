package com.evermodel.service.agg.aidemo.tools.repo;

import com.evermodel.service.agg.aidemo.tools.dto.AppointmentOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tianwl
 * @date 2025/8/31
 * @description
 */
@Slf4j
@Component
public class AppointmentOrderRepo {

    private static List<AppointmentOrder> appointmentOrders = new ArrayList<>();

    public Integer addAppointmentOrder(String userName, String idNo, String phone){
        AppointmentOrder order = AppointmentOrder.builder()
                .id(appointmentOrders.size() + 1)
                .userName(userName)
                .idNo(idNo)
                .phone(phone)
                .build();
        appointmentOrders.add(order);
        log.info("添加预约单成功，姓名：{}，身份证号码：{}, 联系方式：{}",
                order.getUserName(),
                order.getIdNo(),
                order.getPhone());
        return order.getId();
    }
}