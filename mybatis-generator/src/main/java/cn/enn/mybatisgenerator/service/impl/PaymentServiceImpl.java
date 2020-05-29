package cn.enn.mybatisgenerator.service.impl;

import cn.enn.mybatisgenerator.mapper.PaymentMapper;
import cn.enn.mybatisgenerator.model.Payment;
import cn.enn.mybatisgenerator.service.PaymentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentMapper paymentMapper;


    @Override
    public int create(Payment payment) {
        int result = paymentMapper.insertSelective(payment);
        return result;
    }

    @Override
    public Payment queryById(Long id) {
        Payment payment = paymentMapper.selectByPrimaryKey(id);
        return payment;
    }
}
