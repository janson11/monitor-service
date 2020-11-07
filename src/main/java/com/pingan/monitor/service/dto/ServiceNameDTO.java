package com.pingan.monitor.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 服务名DRO
 * @Author: Janson
 * @Date: 2020/11/7 23:54
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceNameDTO {

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 服务个数
     */
    private Integer count;
}
