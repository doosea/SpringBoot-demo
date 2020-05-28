package cn.enn.xxljob.job;

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
