package com.unique.id.service;

/**
 * 创建时间：2017/3/1
 * 创建人： by LeWis
 */
public interface IMachineService {

    Long machineId();

    void destroy();

    void init();
}
