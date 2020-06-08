package cn.enn.websocket.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 模拟产生日志
 */
public class LogWriter {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void LogMsg(File logFile, String mesInfo) throws IOException {
        if (logFile == null) {
            throw new IllegalStateException("logFile can not be null!");
        }
        Writer txtWriter = new FileWriter(logFile, true);
        txtWriter.write(dateFormat.format(new Date()) + "\t" + mesInfo + "\n");
        txtWriter.flush();
    }


    public static void main(String[] args) throws IOException {
        LogWriter logWriter = new LogWriter();
        // 获取当前项目路径
        File directory = new File("");// 参数为空
        String courseFile = directory.getCanonicalPath();
        File fileLog = new File(courseFile + "/websocket/logs/test.log");
        if (!fileLog.exists()) {
            fileLog.createNewFile();
        }

        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    logWriter.LogMsg(fileLog, "德玛西亚！");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

}