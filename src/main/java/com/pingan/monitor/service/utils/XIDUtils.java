package com.pingan.monitor.service.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: XID工具类
 * @Author: Janson
 * @Date: 2020/11/7 10:53
 **/
@UtilityClass
@Slf4j
public class XIDUtils {

    private List<Long> xids = new ArrayList<>(1024);

    private static int xidSize = 10;

    /**
     * 生成xid;
     *
     * @return
     */
    public Long generateXID() {
        int size = xids.size();
        if (size > xidSize) {
            int nextInt = RandomUtils.nextInt(1, size - 1);
            return xids.get(nextInt);
        }
        long currentTimeMillis = System.currentTimeMillis();
        xids.add(currentTimeMillis);
        log.info("xids:{} size:{}", xids, size);
        return currentTimeMillis;
    }


}
