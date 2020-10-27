package com.distribute.lock.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ZookeeperCurator {

    public Boolean getLock() {
        CuratorFramework client = null;
        InterProcessMutex lock = null;
        try {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
            client = CuratorFrameworkFactory.builder()
                    .connectString("localhost:2181")
                    .sessionTimeoutMs(3000)
                    .connectionTimeoutMs(5000)
                    .retryPolicy(retryPolicy)
                    .build();

            client.start();

            lock = new InterProcessMutex(client, "/order");

            if (lock.acquire(30, TimeUnit.SECONDS)) {
                log.info("已获取锁了....");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                lock.release();
                log.info("已释放锁了....");
            } catch (Exception e) {
                e.printStackTrace();
            }
            client.close();
        }
        return false;
    }
}
