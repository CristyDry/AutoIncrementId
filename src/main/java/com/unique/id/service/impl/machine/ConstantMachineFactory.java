package com.unique.id.service.impl.machine;

import com.unique.id.service.IMachineService;

/**
 * 创建时间：2017/3/2
 * 创建人： by LeWis
 */
public class ConstantMachineFactory implements IMachineService {

    @Override
    public Long machineId() {
        return 1L;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init() {

    }
}
