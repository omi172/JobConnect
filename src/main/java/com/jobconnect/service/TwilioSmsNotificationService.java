package com.jobconnect.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@Primary
public class TwilioSmsNotificationService implements SmsNotificationService {

    @Value("${jobconnect.sms.provider:MOCK}")
    private String provider;

    @Value("${jobconnect.sms.twilio.account-sid:}")
    private String accountSid;

    @Value("${jobconnect.sms.twilio.auth-token:}")
    private String authToken;

    @Value("${jobconnect.sms.twilio.from-number:}")
    private String fromNumber;

    private boolean twilioReady = false;

    @PostConstruct
    public void init() {
        if ("TWILIO".equalsIgnoreCase(provider)
                && StringUtils.hasText(accountSid)
                && StringUtils.hasText(authToken)
                && StringUtils.hasText(fromNumber)) {
            Twilio.init(accountSid, authToken);
            twilioReady = true;
            log.info("Twilio SMS provider initialized.");
        } else {
            log.info("SMS provider running in MOCK mode (no Twilio credentials configured). " +
                    "Messages will be logged instead of sent.");
        }
    }

    @Override
    public boolean sendSms(String toPhoneNumber, String message) {
        if (!StringUtils.hasText(toPhoneNumber)) {
            log.warn("Skipping SMS send: no phone number on file.");
            return false;
        }

        if (!twilioReady) {
            // MOCK mode: log so the notification flow is fully exercised end-to-end
            // without needing real Twilio credentials during development/testing.
            log.info("[MOCK SMS] to={} message=\"{}\"", toPhoneNumber, message);
            return true;
        }

        try {
            Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromNumber),
                    message
            ).create();
            return true;
        } catch (Exception ex) {
            log.error("Failed to send SMS to {}: {}", toPhoneNumber, ex.getMessage());
            return false;
        }
    }
}
