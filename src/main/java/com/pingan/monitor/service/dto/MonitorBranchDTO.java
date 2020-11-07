package com.pingan.monitor.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 分支事务DTO
 * @Author: Janson
 * @Date: 2020/11/7 19:02
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorBranchDTO {
    /**
     * 时间戳
     */
    private Long time;

    /**
     * 分支ID
     */
    private String branchId;

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 服务器IP
     */
    private String ip;
    /**
     * 服务器端口号
     */
    private String port;
    /**
     * SQL列表
     */
    private List<String> sqlList;
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


}
