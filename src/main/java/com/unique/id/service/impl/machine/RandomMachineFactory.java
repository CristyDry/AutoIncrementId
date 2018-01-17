package com.unique.id.service.impl.machine;

import com.unique.id.service.IMachineService;

import java.util.Random;

/**
 * 创建时间：2017/3/2
 * 创建人： by LeWis
 */
public class RandomMachineFactory implements IMachineService {
    @Override
    public Long machineId() {
        return (long) new Random().nextInt(1024);
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init() {

    }
}
