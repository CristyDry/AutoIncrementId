package com.unique.id.service.impl.serial;

import com.unique.id.service.base.AbstractSerialNumberService;
import org.springframework.data.redis.core.RedisOperations;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by LeWis on 2017/12/13.
 */
public class RedisSerialNumberImpl extends AbstractSerialNumberService {

    private String keyPrefix = "serial:";
    private String pattern = "yyyyMMdd";
    private SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
    private RedisOperations<String, String> redisOperations;
    private Long startSerialNumber = null;//开始序号
    private Map<String, String> prefixMap = new HashMap<>();

    public RedisSerialNumberImpl(RedisOperations<String, String> redisOperations) {
        this.redisOperations = redisOperations;
    }

    public RedisSerialNumberImpl(RedisOperations<String, String> redisOperations, Long startSerialNumber) {
        this.redisOperations = redisOperations;
        this.startSerialNumber = startSerialNumber;
    }

    /**
     * 获取连续的序号
     *
     * @param prefix 前缀
     * @return
     */
    @Override
    public long serialNumber(String prefix) {
        String currentKey = null;
        if (prefixMap.containsKey(prefix)) {
            currentKey = prefixMap.get(prefix);
        }
        if (currentKey == null) {
            //第一次启动
            currentKey = getKey(prefix);
            prefixMap.put(prefix, currentKey);
            if (startSerialNumber != null) {
                if (!isRepair(getRepairKey(prefix), startSerialNumber)) {
                    //设置起始号
                    redisOperations.opsForValue().set(currentKey, startSerialNumber.toString());
                    //修复记录
                    redisOperations.opsForValue().set(getRepairKey(prefix), startSerialNumber.toString());
                    //设置有效期
                    deletePreData(currentKey);
                }
            }
        }else{
            String key = getKey(prefix);//键名
            if (!currentKey.equals(key)) {
                currentKey = key;
                prefixMap.put(prefix, currentKey);
            }
        }
        long number = redisOperations.opsForValue().increment(currentKey, 1L);//获取下个序号;
        if (number == 1) {
            //第一个序号
            deletePreData(currentKey);
        }
        return number;
    }

    /*删除上一天的数据*/
    private void deletePreData(String key) {
        redisOperations.expire(key, 2L, TimeUnit.DAYS);
    }

    /**
     * 判断是否修复过
     */
    private boolean isRepair(String key, Long val) {
        if (redisOperations.hasKey(key)) {
            String preNumber = redisOperations.opsForValue().get(key);
            if (preNumber == null || preNumber.length() == 0) {
                return false;
            } else {
                Long number = Long.valueOf(preNumber);
                return number.compareTo(val) == 0;
            }

        } else {
            return false;
        }
    }

    /**
     * 键名
     */
    private String getKey(String prefix) {
        return keyPrefix + dateFormat.format(new Date()) + ":" + prefix;//
    }

    /**
     * 修复键名
     */
    private String getRepairKey(String prefix) {
        return keyPrefix + prefix + ":repair";
    }

    /**
     * 键名前缀
     */
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public void setStartSerialNumber(Long startSerialNumber) {
        this.startSerialNumber = startSerialNumber;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
        this.dateFormat = new SimpleDateFormat(pattern);
    }
}
