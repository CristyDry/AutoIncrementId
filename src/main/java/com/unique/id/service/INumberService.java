package com.unique.id.service;

/**
 * Created by LeWis on 2017/12/13.
 * startSerialNumber()方法用于修正序号,默认返回null,使用修正号后第一次启动会以修正号开始(不包含修正号)
 *
 */
public interface INumberService {

    /**获取下个序号*/
    long serialNumber(String prefix);

}
