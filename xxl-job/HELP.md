# Springboot 整合 XXL-job 使用备忘
SpringBoot 整合xxl-job 启动定时任务测试样例

## XXL-job 相关资料
* [XXL-JOB 中文文档](https://www.xuxueli.com/xxl-job/)
* [XXL-JOB github 地址](https://github.com/xuxueli/xxl-job)
* [参考资料: SpringBoot 整合XXL-JOB调度中心](http://www.oowoo.cn/detail.html?id=64)


## XXL-JOB 说明
xxl-job、xxl-job-admin 、 执行器executor 之间关系

* `xxl-job`是整个项目的名字，它包含了`xxl-job-admin`、`xxl-job-core`、`xxl-job-executor-sample-springboot`
    1. `xxl-job-admin`: 调度中心，作为执行器的注册中心和添加管理定时任务;
    2. `xxl-job-core`: 公共依赖;
    3. `xxl-job-executor-sample-springboot`: Springboot管理执行器，执行业务逻辑;


## 整合详细过程

1. **打包部署 xxl-job-admin** ( [参考连接](https://blog.csdn.net/xh_small_black/article/details/90695125))
    1. clone 原[xxl-job]((https://github.com/xuxueli/xxl-job))项目到本地
    2. Navicat 中 执行`./doc/db/tables_xxl_job.spl` 文件，生成xxl相应表
    3. 修改`xxl-job-admin`下的`application.properties`文件
        ```properties
            ### web
            server.port=8081
            server.servlet.context-path=/xxl-job-admin
        
            ### xxl-job, datasource
            spring.datasource.url=jdbc:mysql://127.0.0.1:3306/xxl_job?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai
            spring.datasource.username=root
            spring.datasource.password=root
            spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
        ``` 
    4. 打包`xxl-job-admin`
        * `xxl-job`的父工程先`clean`然后再`install`
        * `install xxl-job-admin`
    5. `copy` `./xxl-job-admin/target` 目录下的`jar`包 :`xxl-job-admin-2.2.1-SNAPSHOT.jar`
    6. 部署到你想要启动的springboot 工程中，并启动该服务  
        `java - jar xxxx.jar`
    7. 测试 ：`http://localhost:8081/xxl-job-admin`(该url根据自己定义的端口以及上下文，结合所在的IP使用) 
        > 账户：admin  (默认)  
         密码：123456  (默认)
    
    到此`xxl-job`独立服务端的初步部署已经完成，下面进入整合阶段。
 
 2. **SpringBoot 整合XXL-JOB**( [参考连接](http://www.oowoo.cn/detail.html?id=64))
    1. 配置文件
        ```properties
        server:
          port: 8080
        #调度中心
        xxl:
          job:
            accessToken: ''
            admin:
              addresses: http://127.0.0.1:8081/xxl-job-admin
            executor:
              appname: dosea
              ip:
              logpath: /data/applogs/xxl-job/dosea
              logretentiondays: 30
              port: 9999

        ``` 
    2. pom.xml 引入依赖
       ```xml
        <dependency>
          <groupId>com.xuxueli</groupId>
          <artifactId>xxl-job-core</artifactId>
          <version>2.2.0</version>
        </dependency>
       ```
    3. 编写 配置类 `XxlJobConfig`
        ```java
        import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        
        @Configuration
        public class XxlJobConfig {
        
            @Value("${xxl.job.admin.addresses}")
            private String adminAddresses;
        
            @Value("${xxl.job.executor.appname}")
            private String appName;
        
            @Value("${xxl.job.executor.ip}")
            private String ip;
        
            @Value("${xxl.job.executor.port}")
            private int port;
        
            @Value("${xxl.job.accessToken}")
            private String accessToken;
        
            @Value("${xxl.job.executor.logpath}")
            private String logPath;
        
            @Value("${xxl.job.executor.logretentiondays}")
            private int logRetentionDays;
        
        
            @Bean
            public XxlJobSpringExecutor xxlJobExecutor() {
                XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
                xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
                xxlJobSpringExecutor.setIp(ip);
                xxlJobSpringExecutor.setAppname(appName);
                xxlJobSpringExecutor.setPort(port);
                xxlJobSpringExecutor.setAccessToken(accessToken);
                xxlJobSpringExecutor.setLogPath(logPath);
                xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        
                return xxlJobSpringExecutor;
            }
        
        }
        ```

    4. 编写 测试定时任务 `TestTask`
       ```java
        import com.xxl.job.core.biz.model.ReturnT;
        import com.xxl.job.core.handler.annotation.XxlJob;
        import com.xxl.job.core.log.XxlJobLogger;
        import org.springframework.stereotype.Component;
        
        import java.time.LocalDateTime;
        
        @Component
        public class TestTask {
            /**
             * 1、简单任务示例（Bean模式）
             */
            @XxlJob("test")
            public ReturnT<String> test(String param) {
                System.out.println("SpringBoot2.x_exec执行器->"+ LocalDateTime.now());
                XxlJobLogger.log("test --》SpringBoot2.x_exec执行器->"+ LocalDateTime.now());
                return ReturnT.SUCCESS;
            }
        }
       ```
    5. `xxl-job-admin`任务调度中心web界面中添加执行器,并启动
    6. 启动 `Springboot`项目
   
## 注意事项 
* `@XxlJob`的值不能重复，用来标识一个定时任务，需要标记在方法上。在以前老的版本中是使用注解`@JobHandler`标记在类上来标识一个定时任务。
* `XxlJobLogger`可以用来收集xxl-job特定的日志，只有使用`XxlJobLogger.log()`才能将日志收集到`xxl-job-admin`调度中心。
* `ReturnT.SUCCESS`、`ReturnT.FAIL` 用来返回执行任务是否成功。
