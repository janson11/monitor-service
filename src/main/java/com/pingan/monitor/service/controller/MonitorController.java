package com.pingan.monitor.service.controller;

import com.pingan.monitor.service.dto.MonitorBranchDTO;
import com.pingan.monitor.service.dto.MonitorDTO;
import com.pingan.monitor.service.dto.ServiceNameDTO;
import com.pingan.monitor.service.utils.MonitorUtils;
import com.pingan.monitor.service.utils.XIDUtils;
import com.pingan.monitor.service.vo.Monitor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: 监控控制层
 * @Author: Janson
 * @Date: 2020/11/7 10:32
 **/
@RestController
public class MonitorController {

    private static final int MOD = 24;

    private static final List<Monitor> MONITOR_LIST = new ArrayList<>(10000);

    private static final String ORDER_SERVICE = "order-service";
    private static final String GOODS_SERVICE = "goods-service";
    private static final String STOCK_SERVICE = "stock-service";
    private static final String MEMBER_SERVICE = "member-service";
    private static final String POINT_SERVICE = "point-service";
    private static final String PAY_SERVICE = "pay-service";
    private static final String GIFT_SERVICE = "gift-service";
    private static final String COUPON_SERVICE = "coupon-service";

    /**
     * 获取全局事务明细
     *
     * @return
     */
    @GetMapping("/xid")
    public List<MonitorDTO> getXidList() {
        List<MonitorDTO> result = new ArrayList<>();
        Map<String, List<Monitor>> map = MONITOR_LIST.stream().collect(Collectors.groupingBy(Monitor::getXid));
        Set<String> keySet = map.keySet();
        keySet.forEach(
                key -> {
                    List<Monitor> monitors = map.get(key);
                    Optional<Monitor> minMonitor = monitors.stream().min(Comparator.comparing(Monitor::getStartTime));
                    Optional<Monitor> maxMonitor = monitors.stream().max(Comparator.comparing(Monitor::getEndTime));
                    MonitorDTO monitorDTO = new MonitorDTO();
                    Monitor monitor = minMonitor.get();
                    monitorDTO.setTime(monitor.getTime());
                    monitorDTO.setXid(key);
                    monitorDTO.setStartTime(monitor.getStartTime());
                    monitorDTO.setStatus(monitor.getStatus());
                    monitorDTO.setEndTime(maxMonitor.get().getEndTime());
                    monitorDTO.setElapsedTime(monitorDTO.getEndTime() - monitorDTO.getStartTime());
                    result.add(monitorDTO);
                }
        );
        return result;
    }


    /**
     * 获取分支事务明细
     *
     * @return
     */
    @GetMapping("/branchId")
    public List<MonitorBranchDTO> getBranchIdList(@RequestParam(value = "xid") String xid) {
        List<MonitorBranchDTO> result = new ArrayList<>();
        List<Monitor> branchIdList = MONITOR_LIST.stream().filter(item -> xid.equals(item.getXid())).collect(Collectors.toList());
        branchIdList.stream().forEach(monitor -> {
            MonitorBranchDTO monitorBranchDTO = MonitorBranchDTO.builder()
                    .time(monitor.getTime())
                    .branchId(monitor.getBranchId())
                    .status(monitor.getStatus())
                    .serviceName(monitor.getServiceName())
                    .ip(monitor.getIp())
                    .port(monitor.getPort())
                    .startTime(monitor.getStartTime())
                    .endTime(monitor.getEndTime())
                    .sqlList(monitor.getSqlList())
                    .build();
            result.add(monitorBranchDTO);
        });
        return result;
    }


    /**
     * 获取全局事务数量
     *
     * @return
     */
    @GetMapping("/count")
    public Map<String, Integer> getCount() {
        Map<String, Integer> result = new HashMap<>(3);
        Map<Integer, List<Monitor>> collect = MONITOR_LIST.stream().collect(Collectors.groupingBy(Monitor::getStatus));
        Integer commitSize = CollectionUtils.isEmpty(collect.get(0)) ? 0 : collect.get(0).size();
        Integer rollbackSize = CollectionUtils.isEmpty(collect.get(1)) ? 0 : collect.get(1).size();
        Integer errorSize = CollectionUtils.isEmpty(collect.get(2)) ? 0 : collect.get(2).size();
        result.put("totalCount", commitSize + rollbackSize + errorSize);
        result.put("successCount", commitSize + rollbackSize);
        result.put("errorCount", errorSize);
        return result;
    }


    /**
     * 耗时排行
     *
     * @return
     */
    @GetMapping("xidTop")
    public List<Map<String, String>> elapsedTimeTop() {
        List<Map<String, String>> result = new ArrayList<>();
        List<MonitorDTO> xidList = getXidList();
        xidList.sort(Comparator.comparingLong(MonitorDTO::getElapsedTime).reversed());
        xidList.stream().forEach(item -> {
            String xid = item.getXid();
            Long elapsedTime = item.getElapsedTime();
            Map<String, String> map = new HashMap<>(2);
            map.put("xid", xid);
            map.put("elapsedTime", String.valueOf(elapsedTime));
            result.add(map);
        });
        return result;
    }

    /**
     * 服务排行
     *
     * @return
     */
    @GetMapping("serviceNameTop")
    public List<ServiceNameDTO> serviceNameTop() {
        List<ServiceNameDTO> result = new ArrayList<>();
        Map<String, List<Monitor>> serviceNames = MONITOR_LIST.stream().collect(Collectors.groupingBy(Monitor::getServiceName));
        Set<String> strings = serviceNames.keySet();
        strings.stream().forEach(serviceName -> {
            List<Monitor> monitors = serviceNames.get(serviceName);
            ServiceNameDTO serviceNameDTO = ServiceNameDTO.builder()
                    .serviceName(serviceName)
                    .count(monitors.size())
                    .build();
            result.add(serviceNameDTO);
        });
        result.sort(Comparator.comparingInt(ServiceNameDTO::getCount).reversed());
        return result;
    }


    /**
     * 根据服务名查询服务列表
     *
     * @return
     */
    @GetMapping("serviceName")
    public List<Monitor> serviceName(@RequestParam(value = "serviceName") String serviceName) {
        Map<String, List<Monitor>> serviceNames = MONITOR_LIST.stream().collect(Collectors.groupingBy(Monitor::getServiceName));
        return serviceNames.get(serviceName);
    }

    @PostMapping("/monitor")
    public Monitor monitor() {
        Long xid = XIDUtils.generateXID();
        long currentTimeMillis = System.currentTimeMillis();
        // 0 订单服务
        List<String> sqlOrderList = Arrays.asList("update t_order set status='0' where id='10'", "update t_order set status='1' where id='12'", "update t_order set status='2' where id='13' ");
        Monitor monitorOrder = MonitorUtils.buildMonitor(xid, ORDER_SERVICE, "192.168.11.11", "8081", sqlOrderList, 0);

        List<String> sqlOrderList1 = Arrays.asList("update t_order set status='0' where id='15'", "update t_order set status='1' where id='16'", "update t_order set status='2' where id='17' ");
        Monitor monitorOrder1 = MonitorUtils.buildMonitor(xid, ORDER_SERVICE, "192.168.11.21", "8081", sqlOrderList1, 0);

        List<String> sqlOrderList2 = Arrays.asList("update t_order set status='0' where id='21'", "update t_order set status='1' where id='22'", "update t_order set status='2' where id='23' ");
        Monitor monitorOrder2 = MonitorUtils.buildMonitor(xid, ORDER_SERVICE, "192.168.11.31", "8081", sqlOrderList2, 0);

        // 1 商品服务
        List<String> sqlGoodsList = Arrays.asList("update t_goods set status='0' where id='10'", "update t_goods set status='1' where id='12'", "update t_goods set status='2' where id='13' ");
        Monitor monitorGoods = MonitorUtils.buildMonitor(xid, GOODS_SERVICE, "192.168.11.12", "8082", sqlGoodsList, 1);

        List<String> sqlGoodsList1 = Arrays.asList("update t_goods set status='0' where id='15'", "update t_goods set status='1' where id='16'", "update t_goods set status='2' where id='17' ");
        Monitor monitorGoods1 = MonitorUtils.buildMonitor(xid, GOODS_SERVICE, "192.168.11.22", "8082", sqlGoodsList1, 1);

        List<String> sqlGoodsList2 = Arrays.asList("update t_goods set status='0' where id='21'", "update t_goods set status='1' where id='22'", "update t_goods set status='2' where id='23' ");
        Monitor monitorGoods2 = MonitorUtils.buildMonitor(xid, GOODS_SERVICE, "192.168.11.32", "8082", sqlGoodsList2, 1);

        // 2 库存服务
        List<String> sqlStockList = Arrays.asList("update t_stock set status='0' where id='17'", "update t_stock set status='1' where id='19'", "update t_stock set status='2' where id='33'");
        Monitor monitorStock = MonitorUtils.buildMonitor(xid, STOCK_SERVICE, "192.168.11.13", "8083", sqlStockList, 2);

        List<String> sqlStockList1 = Arrays.asList("update t_stock set status='0' where id='27'", "update t_stock set status='1' where id='28'", "update t_stock set status='2' where id='29'");
        Monitor monitorStock1 = MonitorUtils.buildMonitor(xid, STOCK_SERVICE, "192.168.11.23", "8083", sqlStockList1, 2);

        List<String> sqlStockList2 = Arrays.asList("update t_stock set status='0' where id='37'", "update t_stock set status='1' where id='38'", "update t_stock set status='2' where id='39'");
        Monitor monitorStock2 = MonitorUtils.buildMonitor(xid, STOCK_SERVICE, "192.168.11.33", "8083", sqlStockList2, 2);

        // 3 会员服务
        List<String> sqlMemberList = Arrays.asList("update t_member set status='0' where id='171'", "update t_member set status='1' where id='191'", "update t_member set status='2' where id='331'");
        Monitor monitorMember = MonitorUtils.buildMonitor(xid, MEMBER_SERVICE, "192.168.11.14", "8084", sqlMemberList, 0);

        List<String> sqlMemberList1 = Arrays.asList("update t_member set status='0' where id='271'", "update t_member set status='1' where id='281'", "update t_member set status='2' where id='291'");
        Monitor monitorMember1 = MonitorUtils.buildMonitor(xid, MEMBER_SERVICE, "192.168.11.24", "8084", sqlMemberList1, 0);

        List<String> sqlMemberList2 = Arrays.asList("update t_member set status='0' where id='371'", "update t_member set status='1' where id='381'", "update t_member set status='2' where id='391'");
        Monitor monitorMember2 = MonitorUtils.buildMonitor(xid, MEMBER_SERVICE, "192.168.11.34", "8084", sqlMemberList2, 0);


        // 4 积分服务
        List<String> sqlPointList = Arrays.asList("update t_coupon set status='0' where id='172'", "update t_coupon set status='1' where id='192'", "update t_coupon set status='2' where id='332'");
        Monitor monitorPoint = MonitorUtils.buildMonitor(xid, POINT_SERVICE, "192.168.11.15", "8085", sqlPointList, 1);

        List<String> sqlPointList1 = Arrays.asList("update t_coupon set status='0' where id='272'", "update t_coupon set status='1' where id='282'", "update t_coupon set status='2' where id='292'");
        Monitor monitorPoint1 = MonitorUtils.buildMonitor(xid, POINT_SERVICE, "192.168.11.25", "8085", sqlPointList1, 1);

        List<String> sqlPointList2 = Arrays.asList("update t_coupon set status='0' where id='372'", "update t_coupon set status='1' where id='382'", "update t_coupon set status='2' where id='392'");
        Monitor monitorPoint2 = MonitorUtils.buildMonitor(xid, POINT_SERVICE, "192.168.11.35", "8085", sqlPointList2, 1);

        // 5 支付服务
        List<String> sqlPayList = Arrays.asList("update t_pay set status='0' where id='173'", "update t_pay set status='1' where id='193'", "update t_pay set status='2' where id='333'");
        Monitor monitorPay = MonitorUtils.buildMonitor(xid, PAY_SERVICE, "192.168.11.16", "8086", sqlPayList, 2);

        List<String> sqlPayList1 = Arrays.asList("update t_pay set status='0' where id='273'", "update t_pay set status='1' where id='283'", "update t_pay set status='2' where id='293'");
        Monitor monitorPay1 = MonitorUtils.buildMonitor(xid, PAY_SERVICE, "192.168.11.26", "8086", sqlPayList1, 2);

        List<String> sqlPayList2 = Arrays.asList("update t_pay set status='0' where id='373'", "update t_pay set status='1' where id='383'", "update t_pay set status='2' where id='393'");
        Monitor monitorPay2 = MonitorUtils.buildMonitor(xid, PAY_SERVICE, "192.168.11.36", "8086", sqlPayList2, 2);

        // 6 礼包服务
        List<String> sqlGiftList = Arrays.asList("update t_gift set status='0' where id='174'", "update t_gift set status='1' where id='194'", "update t_gift set status='2' where id='334'");
        Monitor monitorGift = MonitorUtils.buildMonitor(xid, GIFT_SERVICE, "192.168.11.17", "8087", sqlGiftList, 1);

        List<String> sqlGiftList1 = Arrays.asList("update t_gift set status='0' where id='274'", "update t_gift set status='1' where id='284'", "update t_gift set status='2' where id='294'");
        Monitor monitorGift1 = MonitorUtils.buildMonitor(xid, GIFT_SERVICE, "192.168.11.27", "8087", sqlGiftList1, 1);

        List<String> sqlGiftList2 = Arrays.asList("update t_gift set status='0' where id='374'", "update t_gift set status='1' where id='384'", "update t_gift set status='2' where id='394'");
        Monitor monitorGift2 = MonitorUtils.buildMonitor(xid, GIFT_SERVICE, "192.168.11.37", "8087", sqlGiftList2, 1);

        // 7 卡券服务
        List<String> sqlCouponList = Arrays.asList("update t_coupon set status='0' where id='172'", "update t_coupon set status='1' where id='192'", "update t_coupon set status='2' where id='332'");
        Monitor monitorCoupon = MonitorUtils.buildMonitor(xid, COUPON_SERVICE, "192.168.11.18", "8089", sqlCouponList, 0);

        List<String> sqlCouponList1 = Arrays.asList("update t_coupon set status='0' where id='272'", "update t_coupon set status='1' where id='282'", "update t_coupon set status='2' where id='292'");
        Monitor monitorCoupon1 = MonitorUtils.buildMonitor(xid, COUPON_SERVICE, "192.168.11.28", "8089", sqlCouponList1, 0);

        List<String> sqlCouponList2 = Arrays.asList("update t_coupon set status='0' where id='372'", "update t_coupon set status='1' where id='382'", "update t_coupon set status='2' where id='392'");
        Monitor monitorCoupon2 = MonitorUtils.buildMonitor(xid, COUPON_SERVICE, "192.168.11.38", "8089", sqlCouponList2, 0);

        long value = currentTimeMillis % MOD;
        switch (String.valueOf(value)) {
            case "0":
                MONITOR_LIST.add(monitorOrder);
                return monitorOrder;

            case "1":
                MONITOR_LIST.add(monitorGoods);
                return monitorGoods;
            case "2":
                MONITOR_LIST.add(monitorStock);
                return monitorStock;
            case "3":
                MONITOR_LIST.add(monitorOrder1);
                return monitorOrder1;
            case "4":
                MONITOR_LIST.add(monitorGoods1);
                return monitorGoods1;
            case "5":
                MONITOR_LIST.add(monitorStock1);
                return monitorStock1;
            case "6":
                MONITOR_LIST.add(monitorOrder2);
                return monitorOrder2;
            case "7":
                MONITOR_LIST.add(monitorGoods2);
                return monitorGoods2;
            case "8":
                MONITOR_LIST.add(monitorStock2);
                return monitorStock2;
            case "9":
                MONITOR_LIST.add(monitorMember);
                return monitorMember;
            case "10":
                MONITOR_LIST.add(monitorMember1);
                return monitorMember1;
            case "11":
                MONITOR_LIST.add(monitorMember2);
                return monitorMember2;
            case "12":
                MONITOR_LIST.add(monitorPay);
                return monitorPay;
            case "13":
                MONITOR_LIST.add(monitorPay1);
                return monitorPay1;
            case "14":
                MONITOR_LIST.add(monitorPay2);
                return monitorPay2;
            case "15":
                MONITOR_LIST.add(monitorCoupon1);
                return monitorCoupon1;
            case "16":
                MONITOR_LIST.add(monitorCoupon2);
                return monitorCoupon2;
            case "17":
                MONITOR_LIST.add(monitorCoupon);
                return monitorCoupon;
            case "18":
                MONITOR_LIST.add(monitorGift);
                return monitorGift;
            case "19":
                MONITOR_LIST.add(monitorGift1);
                return monitorGift1;
            case "20":
                MONITOR_LIST.add(monitorGift2);
                return monitorGift2;
            case "21":
                MONITOR_LIST.add(monitorPoint);
                return monitorPoint;
            case "22":
                MONITOR_LIST.add(monitorPoint1);
                return monitorPoint1;
            case "23":
                MONITOR_LIST.add(monitorPoint2);
                return monitorPoint2;
            default:
                break;
        }
        return monitorOrder;
    }
}

