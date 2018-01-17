package com.unique.id.service.impl.unique;

import com.unique.id.service.base.AbstractUniqueNumberService;
import com.unique.id.util.IdWorker;

/**
 * 非连续
 * Created by LeWis on 2018/1/17.
 */
public class WorkerSerialNumberImpl extends AbstractUniqueNumberService {

    private final IdWorker idWorker;

    public WorkerSerialNumberImpl(Long workerId){
        idWorker = new IdWorker(workerId);
    }

    @Override
    public long serialNumber(String prefix) {
        return idWorker.nextId();
    }

}
