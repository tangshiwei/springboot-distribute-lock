package com.distribute.lock.service;

import com.distribute.lock.bean.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

public class ProductService {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    private TransactionDefinition transactionDefinition;

    //分布式锁

    //@Transactional(rollbackFor = Exception.class)
    public synchronized void updateProduct(Product product){
        //手动事务
        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
       //回滚
        if(product==null||product.getCount()<1){
            platformTransactionManager.rollback(transaction);
            throw new RuntimeException();
        }
        //更新数量
        product.setCount(product.getCount()-1);
        update(product);
        //提交
        platformTransactionManager.commit(transaction);
    }

    private void update(Product product){

    }

}
