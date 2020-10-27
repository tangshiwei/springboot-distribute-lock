package com.distribute.lock.controller;

import com.distribute.lock.bean.DiscributeLock;
import com.distribute.lock.mapper.DiscributeLockMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class MySqlController {

    @Autowired
    private DiscributeLockMapper discributeLockMapper;
    @GetMapping("/mysqlLock")
    public List<DiscributeLock> mysqlLock() throws Exception {
        log.info("进入mysqlLock()方法.....");
        List<DiscributeLock> list = discributeLockMapper.findDiscributeLock();
        if(list==null){
            throw new Exception("分布式锁找不到");
        }
        log.info("已经进入锁了...");
        try{
            Thread.sleep(20000);
        }catch (Exception e){
            e.printStackTrace();
        }
        log.info("已经执行完成。。。");
       return list;
    }

}
