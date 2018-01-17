package com.unique.id;

import com.unique.id.service.IMachineService;
import com.unique.id.service.INumberService;
import com.unique.id.service.base.AbstractSerialNumberService;
import com.unique.id.service.base.AbstractUniqueNumberService;

/**
 * Created by LeWis on 2018/1/17.
 */
public class Unique {

    private INumberService uniqueNumberService;
    private INumberService serialNumber;
    private IMachineService machineFactory;

    public Unique(IMachineService machineFactory, AbstractSerialNumberService serialNumberService, AbstractUniqueNumberService uniqueNumberService) {
        this.serialNumber = serialNumberService;
        this.uniqueNumberService = uniqueNumberService;
        this.machineFactory = machineFactory;
    }

    public Unique(INumberService serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * 无序
     * @param prefix
     * @return
     */
    public Long nextUniqueId(String prefix){
        return uniqueNumberService.serialNumber(prefix);
    }

    /**
     * 有序
     * @param prefix
     * @return
     */
    public Long nextSerialId(String prefix){
        return serialNumber.serialNumber(prefix);
    }

    public Long machineId(){
        if(machineFactory == null){
            return 0L;
        }
        return machineFactory.machineId();
    }
}
