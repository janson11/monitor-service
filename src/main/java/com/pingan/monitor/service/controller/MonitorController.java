package com.pingan.monitor.service.controller;

import com.pingan.monitor.service.vo.Monitor;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * @Description: 监控控制层
 * @Author: Janson
 * @Date: 2020/11/4 18:32
 **/
@RestController
public class MonitorController {

    private static final int MOD = 3;

    @GetMapping("/monitor")
    public Monitor monitor() {
        int nextInt = RandomUtils.nextInt(0, 18);
        long currentTimeMillis = System.currentTimeMillis();
        // 0 订单服务
        Monitor monitorOrder = Monitor.builder()
                .time(currentTimeMillis)
                .xid("xid_" + currentTimeMillis)
                .branchId("branch_" + currentTimeMillis)
                .serviceName("order-service")
                .ip("192.168.11.11")
                .port("8081")
                .sqlList(Arrays.asList("update t_order set status='0' where id='1'", "update t_order set status='1' where id='2'", "update t_order set status='2' where id='3'"))
                .status(1)
                .startTime(currentTimeMillis)
                .endTime(currentTimeMillis + (1000 * nextInt) + nextInt)
                .build();

        // 1 商品服务
        Monitor monitorGoods = Monitor.builder()
                .time(currentTimeMillis)
                .xid("xid_" + currentTimeMillis)
                .branchId("branch_" + currentTimeMillis)
                .serviceName("goods-service")
                .ip("192.168.11.12")
                .port("8082")
                .sqlList(Arrays.asList("update t_goods set status='0' where id='10'", "update t_order set status='1' where id='12'", "update t_order set status='2' where id='13'"))
                .status(2)
                .startTime(currentTimeMillis)
                .endTime(currentTimeMillis + (1000 * nextInt) + nextInt)
                .build();

        // 2 库存服务
        Monitor monitorStock = Monitor.builder()
                .time(currentTimeMillis)
                .xid("xid_" + currentTimeMillis)
                .branchId("branch_" + currentTimeMillis)
                .serviceName("stock-service")
                .ip("192.168.11.13")
                .port("8083")
                .sqlList(Arrays.asList("update t_stock set status='0' where id='17'", "update t_stock set status='1' where id='19'", "update t_stock set status='2' where id='33'"))
                .status(3)
                .startTime(currentTimeMillis)
                .endTime(currentTimeMillis + (1000 * nextInt) + nextInt)
                .build();
        if (currentTimeMillis % MOD == 0) {
            return monitorOrder;
        } else if (currentTimeMillis % MOD == 1) {
            return monitorGoods;
        } else {
            return monitorStock;
        }
    }


}
