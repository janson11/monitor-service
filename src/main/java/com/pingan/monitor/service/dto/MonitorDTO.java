package com.pingan.monitor.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 全局事务DTO
 * @Author: Janson
 * @Date: 2020/11/7 19:02
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorDTO {

    /**
     * 时间戳
     */
    private Long time;

    /**
     * 全局事务XID
     */
    private String xid;

    /**
     * 事务提交的状态（1提交 2回滚 3异常）
     */
    private Integer status;
    /**
     * 事务的开始时间
     */
    private Long startTime;

    /**
     * 事务的结束时间
     */
    private Long endTime;

    /**
     * 事务耗时
     */
    private Long elapsedTime;

}
