package com.interview.notification;
// 1. Define the Domain Model using Sealed Interfaces and Records
public sealed interface Notification permits Email, Sms, Push {}
record Email(String emailAddress, String subject, String body) implements Notification {}
record Sms(String phoneNumber, String message) implements Notification {}
record Push(String deviceToken, String alertTitle) implements Notification {}

