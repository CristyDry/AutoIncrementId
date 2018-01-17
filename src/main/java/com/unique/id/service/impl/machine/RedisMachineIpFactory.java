package com.unique.id.service.impl.machine;

import com.unique.id.service.IMachineService;
import org.springframework.data.redis.core.RedisOperations;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建时间：2017/3/17
 * 创建人： by LeWis
 */
public class RedisMachineIpFactory implements IMachineService {
    private String redisKey = "MACHINE_IP_NS";//命名空间
    private RedisOperations<String, String> redis;//缓存服务器
    private Map<String, Long> machines;//已有机器码集合
    private Long mMachineId;//机器码
    private static final Long MAX_VAL = 1024L;
    private Long maxVal = MAX_VAL;

    @Override
    public Long machineId() {
        if (mMachineId != null) return mMachineId;
        //获取ip
        String localIp = getLocalIp();
        //判断是否已经生成机器码
        if (!machines.isEmpty()) {
            //如果存在直接返回
            if ((mMachineId = machines.get(localIp)) != null) {
                //返回已有机器码
                return mMachineId;
            }
        }
        //生成机器码
        Long machineId = -1L;
        boolean isOk = false;
        //已有机器码数量不能最大机器码数
        do {
            //增加机器码
            machineId++;
            //本地判断机器码是否存在
            if (!machines.values().contains(machineId)) {
                //保存设备
                isOk = save(machineId, localIp);
                if (isOk) {
                    mMachineId = machineId;
                }
            } else if (machineId >= maxVal) {
                throw new RuntimeException("机器码已用完！");
            }
        } while (machines.values().contains(machineId) && !isOk);
        machines = null;
        return machineId;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init() {
        //获取已有机器码
        machines = loadMachine();
    }

    //获取设备
    private Map<String, Long> loadMachine() {
        Map<Object, Object> kv = redis.opsForHash().entries(redisKey);
        if (kv == null || kv.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Long> machineList = new HashMap<>();
        for (Object key : kv.keySet()) {
            machineList.put(String.valueOf(key)
                    , Long.parseLong(String.valueOf(kv.get(key))));
        }
        return machineList;
    }

    //获取本机ip
    private String getLocalIp() {
        Enumeration allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return "";
        }
        InetAddress ip = null;
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            Enumeration addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                Object element = addresses.nextElement();
                if (element != null && element instanceof Inet4Address) {
                    InetAddress inetAddress = (InetAddress) element;
                    if (!"127.0.0.1".equals(inetAddress.getHostAddress())) {
                        ip = inetAddress;
                        break;
                    }
                }
            }
            if (ip != null) {
                break;
            }
        }
        if (ip != null) {
            return ip.getHostAddress();
        }
        return "";
    }

    //保存设备号
    private boolean save(final Long machineId, final String ip) {
        //启用分布式锁
        return redis.opsForHash().putIfAbsent(redisKey, String.valueOf(ip), String.valueOf(machineId));
    }

    public RedisOperations getRedis() {
        return redis;
    }

    public void setRedis(RedisOperations redis) {
        this.redis = redis;
    }

    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    public Long getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(Long maxVal) {
        this.maxVal = maxVal;
    }
}
