package com.myself.lock.lock;

/**
 * 类名称：DistributedLock<br>
 * 类描述：<br>
 * 创建时间：2018年12月26日<br>
 *
 * @author maopanpan
 * @version 1.0.0
 */
public interface DistributedLock {
    /**
     * 获得锁
     * <p>
     * 修改记录:
     *
     * @param key
     * @return boolean
     * @author maopanpan  2018/12/26
     */
    boolean tryLock(String key);

    /**
     * 释放锁
     * <p>
     * 修改记录:
     *
     * @param key
     * @return boolean
     * @author maopanpan  2018/12/26
     */
    boolean unLock(String key);

}
