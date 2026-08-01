package com.jobconnect.service;

public interface SmsNotificationService {

    /**
     * Sends an SMS message to the given phone number (E.164 format, e.g. +15551234567).
     * Implementations should never throw for a downstream provider failure; they
     * should log and return false so notification failures never break the calling flow.
     *
     * @return true if the message was accepted for delivery.
     */
    boolean sendSms(String toPhoneNumber, String message);

    default void notifyJobPosted(String toPhoneNumber, String jobTitle) {
        sendSms(toPhoneNumber, "JobConnect: your job posting \"" + jobTitle + "\" is now live.");
    }

    default void notifyApplicationReceived(String toPhoneNumber, String jobTitle) {
        sendSms(toPhoneNumber, "JobConnect: a new application was received for \"" + jobTitle + "\".");
    }

    default void notifyApplicationSubmitted(String toPhoneNumber, String jobTitle) {
        sendSms(toPhoneNumber, "JobConnect: your application for \"" + jobTitle + "\" was submitted successfully.");
    }

    default void notifyRegistrationConfirmed(String toPhoneNumber, String fullName) {
        sendSms(toPhoneNumber, "JobConnect: welcome " + fullName + "! Your account was created successfully.");
    }
}
