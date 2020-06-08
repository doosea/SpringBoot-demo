package cn.enn.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@ServerEndpoint("/websocket/{modelVersionId}")
public class WebSocketServer {


    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;


    /**
     * 新的 websocket 请求开启, 连接成功调用方法
     *
     * @param modelVersionId
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("modelVersionId") String modelVersionId, Session session) throws IOException {
        this.session = session;
        webSocketSet.add(this);
        addOnlineCount();

        String lineSeparator = System.getProperty("line.separator");
        String path = "";
        int count = 0; // 控制最大循环数
        final int MAX_COUNT = 30; // 60秒之后刷新页面，重新显示
        long lastTimeFileSize = 0; //上次文件大小

        path = getPathById(modelVersionId);
        log.info("trainLogPath: " + path);
        File logFile = new File(path);
        if (logFile.exists()) {
            try {
                RandomAccessFile randomFile = new RandomAccessFile(logFile, "r");
                while (count++ <= MAX_COUNT) { // 最长显示一小时
                    int i = 0;
                    randomFile.seek(lastTimeFileSize);
                    String tmp = "";
                    while ((tmp = randomFile.readLine()) != null && i++ <= 100) { // 每秒显示10行
                        // 给客户端发送消息
                        sendMessage(new String(tmp.getBytes("ISO8859-1")) + lineSeparator);
                        lastTimeFileSize = randomFile.getFilePointer();
                    }
                    Thread.sleep(1000);
                }
                if (count >= MAX_COUNT) {
                    String msg = "连接时间超时，请重新刷新页面。";
                    sendMessage(msg);
                    log.info(msg);
                }

            } catch (Exception e) {
                log.error(String.format("read file [%s] error.%s", path, e));
            }
        } else {
            sendMessage("日志文件不存在");
            log.info("日志文件不存在");
        }
    }


    /**
     * WebSocket请求关闭
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this); //从set中删除
        subOnlineCount(); //在线数减1
        log.info("当前连接数量：" + getOnlineCount());
    }

    /**
     * 发送消息
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    @OnError
    public void onError(Throwable thr) {
        log.info("websocket error." + thr);
    }

    private String getPathById(String id) {
        String trainLogPath = null;
        try {
            // 获取当前项目路径
            File directory = new File("");// 参数为空
            String courseFile = directory.getCanonicalPath();
            // 这里就不再从数据库查询log 地址，直接指定
            trainLogPath = courseFile+ "/websocket/logs/test.log";
        } catch (Exception e) {
            e.printStackTrace();
            log.error("query trainLogPath error:" + e);
        }
        return trainLogPath;
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }


}
