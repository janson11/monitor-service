package com.pingan.monitor.service.utils;

import com.pingan.monitor.service.vo.Monitor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * @Description: 监控工具类
 * @Author: Janson
 * @Date: 2020/11/7 15:03
 **/
@UtilityClass
public class MonitorUtils {

    /**
     * 生成监控对象
     *
     * @param xid
     * @param serviceName
     * @param ip
     * @param port
     * @param sqlList
     * @param status
     * @return
     */
    public Monitor buildMonitor(long xid, String serviceName, String ip, String port, List<String> sqlList, int status) {
        int nextInt = RandomUtils.nextInt(0, 18);
        long currentTimeMillis = System.currentTimeMillis();
        return Monitor.builder()
                .time(currentTimeMillis)
                .xid("xid_" + xid)
                .branchId("branch_" + currentTimeMillis)
                .serviceName(serviceName)
                .ip(ip)
                .port(port)
                .sqlList(sqlList)
                .status(status)
                .startTime(currentTimeMillis)
                .endTime(currentTimeMillis + (1000 * nextInt) + nextInt)
                .build();

    }


}
