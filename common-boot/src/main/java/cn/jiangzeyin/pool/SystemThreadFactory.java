package cn.jiangzeyin.pool;

import cn.jiangzeyin.StringUtil;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池工厂
 *
 * @author jiangzeyin
 * create 2016-11-21
 */
class SystemThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    /**
     * @return the threadNumber
     */
    public int getThreadNumber() {
        return threadNumber.get();
    }

    SystemThreadFactory(String poolName) {
        if (StringUtil.isEmpty(poolName))
            poolName = "pool";
        //SecurityManager s = System.getSecurityManager();
        group = new ThreadGroup(poolName);//s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = poolNumber.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

}