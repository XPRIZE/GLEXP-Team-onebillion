package org.onebillion.xprz.utils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by alan on 06/07/16.
 */
public class OBConditionLock
{
    ReentrantLock lock = new ReentrantLock();
    int conditionValue;
    Condition condition;
    public OBConditionLock()
    {
        condition = lock.newCondition();
    }
    public OBConditionLock(int cond)
    {
        this();
        conditionValue = cond;
    }
    public void lock()
    {
        lock.lock();
    }
    public void unlock()
    {
        lock.unlock();
    }
    public void unlockWithCondition(int c)
    {
        setConditionValue(c);
        unlock();
    }
    public void setConditionValue(int c)
    {
        conditionValue = c;
        condition.signalAll();
    }
    public void lockWhenCondition(int c)
    {
        lock.lock();
        while (c != conditionValue)
        {
            try
            {
                condition.await();
            }
            catch (InterruptedException e)
            {
            }
        }
    }
}
