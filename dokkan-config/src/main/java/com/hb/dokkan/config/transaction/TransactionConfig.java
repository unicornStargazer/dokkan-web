package com.hb.dokkan.config.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class TransactionConfig {

    // Spring Boot 会自动配置好一个 PlatformTransactionManager，我们直接注入即可。
    // 如果你有多个数据源，这里可能需要指定具体的 TransactionManager。
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * 自定义一个事务模板，用于处理需要新开事务的场景。
     * @return TransactionTemplate
     */
    @Bean(name = "customerTransactionTemplate")
    public TransactionTemplate requiresNewTransactionTemplate() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        // 设置传播行为：总是开启一个新的事务。
        // 如果当前已存在一个事务，那么原事务会被挂起。
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        
        // 设置隔离级别 (可选, 例如：读已提交)
        // transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        
        // 设置超时时间，单位为秒 (可选)
        // transactionTemplate.setTimeout(30); 
        
        // 设置为只读事务 (可选)，可以帮助数据库进行性能优化
        // transactionTemplate.setReadOnly(true);
        
        return transactionTemplate;
    }

    /**
     * 自定义另一个事务模板，用于支持当前事务的场景（这也是默认行为）。
     * @return TransactionTemplate
     */
    @Bean(name = "defaultTransactionTemplate")
    public TransactionTemplate supportsCurrentTransactionTemplate() {
        // 直接使用注入的 transactionManager 创建，所有属性均为默认值
        // 默认传播行为是 PROPAGATION_REQUIRED
        return new TransactionTemplate(transactionManager);
    }
}