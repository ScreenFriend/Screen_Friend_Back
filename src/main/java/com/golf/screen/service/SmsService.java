package com.golf.screen.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {
    
    public void sendSms(String to, String content) {
        // 향후 CoolSMS 등 상용 문자 발송 연동 설정 유입을 고려한 Mock 서비스 구조
        System.out.println("=================================================");
        System.out.println("[SMS MOCK SEND]");
        System.out.println("수신번호: " + to);
        System.out.println("발송내용: " + content);
        System.out.println("=================================================");
    }
}
