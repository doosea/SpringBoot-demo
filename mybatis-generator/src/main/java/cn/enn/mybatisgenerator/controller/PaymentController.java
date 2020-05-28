package cn.enn.mybatisgenerator.controller;

import cn.enn.mybatisgenerator.model.CommonResult;
import cn.enn.mybatisgenerator.model.Payment;
import cn.enn.mybatisgenerator.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @PostMapping("/creat")
    public CommonResult creat(Payment payment) {
        int result = paymentService.creat(payment);
        if (result > 0) {
            return new CommonResult(200, "success", result);
        } else {
            return new CommonResult(400, "failure", null);
        }
    }


    @GetMapping("/query/{id}")
    public CommonResult query(@PathVariable("id") Long id) {
        Payment payment = paymentService.queryById(id);
        if (payment != null) {
            return new CommonResult(200, "success", payment);
        } else {
            return new CommonResult(400, "failure", null);
        }
    }

}
