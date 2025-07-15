package com.proyecto.appclinica.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    @Value("${twilio.phone-from}")
    private String from;

    /**
     * Envía un SMS a un número destino.
     *
     * @param to   número destino con formato E.164, e.g. +51987654321
     * @param body cuerpo del mensaje
     */
    public void sendSms(String to, String body) {
        Message.creator(new PhoneNumber(to), new PhoneNumber(from), body)
                .create();
    }
}
