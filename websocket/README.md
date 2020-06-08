# websocket 使用demo, 解决实时传输后台日志文件，在网页显示


>参考资料：[springboot+websocket实现服务端向客户端实时推送日志](https://blog.csdn.net/u014627099/article/details/87912039)

## 环境配置
*  新建springboot 工程， 引入 `lombok`、`web` 、`websocket` 、`thymeleaf`依赖；

## websocket 工程
1. 配置类
    ```java
         
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.web.socket.server.standard.ServerEndpointExporter;
         
        @Configuration
        public class WebSocketConfig {
            @Bean
            public ServerEndpointExporter serverEndpointExporter() {
                return new ServerEndpointExporter();
            }
        }
    ``` 

2. 模拟一个产生实时日志的工具类
     ```java
        import java.io.File;
        import java.io.FileWriter;
        import java.io.IOException;
        import java.io.Writer;
        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.concurrent.Executors;
        import java.util.concurrent.ScheduledExecutorService;
        import java.util.concurrent.TimeUnit;
    
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
    ```

3. `websocketServer.java`
    ```java
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
                final int MAX_COUNT = 60; // 60秒之后刷新页面，重新显示
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
                            while ((tmp = randomFile.readLine()) != null && i++ <= 10) { // 每秒显示10行
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
    ```

4. 前端`websocketTest.html`
    ```html
       <!DOCTYPE HTML>
       <html>
       <head>
           <title>My WebSocket</title>
       </head>
       
       <body>
       Welcome<br/>
       
       <div id="message">
       </div>
       </body>
       
       <script type="text/javascript">
           var websocket = null;
       
           //判断当前浏览器是否支持WebSocket
           if('WebSocket' in window){
               // 最后的参数为modelVersionId, 变参
               websocket = new WebSocket("ws://localhost:8080/websocket/777");
           }
           else{
               alert('Not support websocket')
           }
       
           //连接发生错误的回调方法
           websocket.onerror = function(){
               setMessageInnerHTML("error");
           };
       
           //连接成功建立的回调方法
           websocket.onopen = function(event){
               setMessageInnerHTML("open");
           }
       
           //接收到消息的回调方法
           websocket.onmessage = function(event){
               setMessageInnerHTML(event.data);
           }
       
           //连接关闭的回调方法
           websocket.onclose = function(){
               setMessageInnerHTML("close");
           }
       
           //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
           window.onbeforeunload = function(){
               websocket.close();
           }
       
           //将消息显示在网页上
           function setMessageInnerHTML(innerHTML){
               document.getElementById('message').innerHTML += innerHTML + '<br/>';
           }
       
           //关闭连接
           function closeWebSocket(){
               websocket.close();
           }
       
       </script>
       </html>
    ```

5. 启动`LogWriter.main`模拟产生日志，启动springboot服务，网页输入`http://localhost:8080/test/test_websocket`, 跳转到`websocketTest.html`，看到页面上，成功建立`websocket`链接，实时读取log日志 
    * 日志每秒刷新一次，有最长连接限制，可自定义更改。















