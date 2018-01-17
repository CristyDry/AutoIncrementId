package com.unique.id.service.impl.machine;

import com.unique.id.service.IMachineService;
import org.springframework.data.redis.core.RedisOperations;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 创建时间：2017/3/1
 * 创建人： by LeWis
 */
public class RedisMachineFactory implements IMachineService {

    private static final Long MAX_VAL = 1024L;
    private String redisKey = "MACHINE_NS";
    private RedisOperations<String, String> redis;
    private List<Long> machines;
    private Long mMachineId;
    private Long maxVal  = MAX_VAL;


    @Override
    public Long machineId() {
        if (mMachineId != null) {
            return mMachineId;
        }
        if (redis == null) {
            throw new RuntimeException("redis未配置");
        }
        Long machineId = -1L;
        boolean isOk = false;
        if (machines != null) {
            //已有机器码数量不能最大机器码数
            do {
                //增加机器码
                machineId++;
                //本地判断机器码是否存在
                if (!machines.contains(machineId)) {
                    //保存设备
                    isOk = save(machineId);
                    if (isOk) {
                        mMachineId = machineId;
                    }
                } else if (machineId >= maxVal) {
                    throw new RuntimeException("机器码已用完！");
                }
            } while (machines.contains(machineId) && !isOk);
        }
        machines = null;
        return machineId;
    }

    @Override
    public void destroy() {
        if (mMachineId != null) {
            remove(mMachineId);
        }
    }

    @Override
    public void init() {
        this.machines = loadMachine();
    }


    //保存设备号
    private boolean save(final Long machineId) {
        //启用分布式锁
        return redis.opsForHash().putIfAbsent(redisKey, String.valueOf(machineId), "");
    }

    //删除设备号
    private void remove(final Long machineId) {
        redis.opsForHash().delete(redisKey, String.valueOf(machineId));
    }

    //获取设备
    private List<Long> loadMachine() {
        Set<Object> set = redis.opsForHash().keys(redisKey);
        if (set == null || set.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> machineList = new ArrayList<Long>();
        for (Object key : set) {
            machineList.add(Long.parseLong(String.valueOf(key)));
        }
        return machineList;
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
