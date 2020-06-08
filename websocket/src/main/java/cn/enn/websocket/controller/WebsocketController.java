package cn.enn.websocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class WebsocketController {

    @GetMapping("/test_websocket")
    public String testWebSocket() {
        return "websocketTest";
    }
}
