package cn.enn.mybatisgenerator.service;

import cn.enn.mybatisgenerator.model.Payment;

public interface PaymentService {
    // 1 增
        int create(Payment payment);
    // 2 删

    // 3 改

    // 4 查
    Payment queryById(Long id);
}
