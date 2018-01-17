package com.unique.id.service.impl.serial;

import com.google.common.io.Files;
import com.google.common.util.concurrent.AtomicLongMap;
import com.unique.id.service.base.AbstractSerialNumberService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LeWis on 2017/12/13.
 */
public class LocalSerialNumberImpl extends AbstractSerialNumberService {
    private String keyPrefix = "serial-";
    private String pattern = "yyyyMMdd";
    private SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
    private String filePath;//序号保存路径
    private boolean autoDelete = true;//自动删除
    private Long startSerialNumber = null;//开始序号
    private Map<String, String> prefixMap = new ConcurrentHashMap<>();
    private AtomicLongMap<String> atomicIntegerMap = AtomicLongMap.create();

    public LocalSerialNumberImpl() {
    }

    public LocalSerialNumberImpl(Long startSerialNumber) {
        this.startSerialNumber = startSerialNumber;
    }

    @Override
    public long serialNumber(String prefix) {
        String currentKey = null;
        if (prefixMap.containsKey(prefix)) {
            currentKey = prefixMap.get(prefix);
        }
        if (currentKey == null) {
            currentKey = getKey(prefix);//键名
            prefixMap.put(prefix, currentKey);
            atomicIntegerMap.put(currentKey, 0);//放入自增列表
            if (startSerialNumber == null) {
                Long number = getNumber(currentKey, ".number");
                if (number != null) {
                    deleteNumber(currentKey); //删除旧序号
                    atomicIntegerMap.put(currentKey, number);//自增重置
                }
            } else {
                //判断是否修正过
                if (!isRepair(getRepair(prefix), startSerialNumber)) {
                    //保存修复记录
                    saveData(getRepair(prefix), ".repair", startSerialNumber);
                    //使用修正号
                    atomicIntegerMap.put(currentKey, startSerialNumber);
                }

            }

        } else {
            String key = getKey(prefix);//键名
            if (!currentKey.equals(key)) {
                deleteNumber(currentKey); //删除旧序号
                atomicIntegerMap.put(currentKey, 0);
                currentKey = key;
                prefixMap.put(prefix, currentKey);
            }
        }
        long number = atomicIntegerMap.incrementAndGet(currentKey);//获取下个序号
        saveData(currentKey, ".number", number);//保存序号
        return number;
    }

    /**
     * 删除旧key
     */
    private void deleteNumber(String key) {
        if (!autoDelete) {
            return;
        }
        File file = new File(filePath, key + ".number");
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 键名
     */
    private String getKey(String prefix) {
        return keyPrefix + prefix + "-" + dateFormat.format(new Date());
    }

    /**
     * 修复名
     */
    private String getRepair(String prefix) {
        return keyPrefix + prefix;
    }

    /*获取已保存序号*/
    private Long getNumber(String key, String name) {
        File file = new File(filePath, key + name);
        if (file.exists()) {
            try {
                return new Long(Files.readFirstLine(file, Charset.forName("utf-8")));
            } catch (IOException e) {
            }
        }
        return null;
    }

    /**
     * 写入文件
     */
    private void saveData(String key, String name, long val) {
        try {
            File file = new File(filePath, key + name);
            Files.createParentDirs(file);
            Files.write(String.valueOf(val), file, Charset.forName("utf-8"));
        } catch (IOException e) {
        }
    }

    /**
     * 判断是否修复过
     */
    private boolean isRepair(String key, Long val) {
        File file = new File(filePath, key + ".repair");
        if (file.exists()) {
            Long preNumber = getNumber(key, ".repair");
            return preNumber != null && preNumber.compareTo(val) == 0;
        } else {
            return false;
        }
    }

    public void setStartSerialNumber(Long startSerialNumber) {
        this.startSerialNumber = startSerialNumber;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
        this.dateFormat = new SimpleDateFormat(pattern);
    }
}
