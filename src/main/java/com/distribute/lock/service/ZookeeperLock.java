package com.distribute.lock.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ZookeeperLock implements AutoCloseable, Watcher {

    private ZooKeeper zooKeeper;
    private String znode;

    public ZookeeperLock() throws IOException {
        zooKeeper = new ZooKeeper("localhost:2181", 10000, this);
    }

    @Override
    public void close() throws Exception {
        zooKeeper.delete(znode,-1);
        zooKeeper.close();
        log.info("zookeeper已经释放锁了。");
    }

    @Override
    public void process(WatchedEvent event) {
        if(event.getType()==Event.EventType.NodeDeleted){
            synchronized (this){
                notify();
            }
        }

    }


    public Boolean getLock(String code) {

        try {
            Stat stat = zooKeeper.exists("/" + code, false);
            if (stat == null) {
                //创建业务根节点
                zooKeeper.create("/" + code, code.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            //创建瞬时有序节点: /order/order_000001
            znode = zooKeeper.create("/" + code + "/" + code + "_", code.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            //获取业务节点下的所有子节点
            List<String> childrenNodes = zooKeeper.getChildren("/" + code, false);
            Collections.sort(childrenNodes);
            String firstNode = childrenNodes.get(0);
            //如果获取的节点是第一个子节点，则获取锁
            if (znode.endsWith(firstNode)) {
                return true;
            }

            //如果不是第一个子节点，则监听前一个节点
            String lastNode = firstNode;
            for (String node : childrenNodes) {
                if (znode.endsWith(node)) {
                    zooKeeper.exists("/" + code + "/" + lastNode, true);
                    break;
                } else {
                    lastNode = node;
                }
            }
            synchronized (this) {
                wait();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
