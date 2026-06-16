package com.interview.notification;

public class NotificationService {

    // 2. Process notifications using Java 17 Switch Pattern Matching
    public String processNotification(Notification notification) {
        return switch (notification) {
            case null -> "Invalid: Payload is missing";
            case Email e -> "Sending Email to " + e.emailAddress() + " with subject: " + e.subject();
            // Guarded Pattern: Using the Java 17 'when' clause for extra conditional checks
            //case Sms s when s.message().length() > 160 -> "Failed: SMS exceeds 160 character limit";
            case Sms s -> "Sending SMS to " + s.phoneNumber();
            case Push p -> "Triggering Push Notification to token: " + p.deviceToken();
            // Note: No 'default' case is required here! The compiler checks exhaustiveness via the sealed interface.
        };
    }

    public static void main(String[] args) {
        NotificationService service = new NotificationService();
        Notification validSms = new Sms("+123456789", "Hello World!");
        Notification longSms = new Sms("+123456789", "A".repeat(165));
        Notification email = new Email("test@example.com", "Interview Prep", "Body content");
        System.out.println(service.processNotification(validSms)); // Sending SMS to +123456789
        System.out.println(service.processNotification(longSms));  // Failed: SMS exceeds 160 character limit
        System.out.println(service.processNotification(email));    // Sending Email to test@example.com...
        System.out.println(service.processNotification(null));     // Invalid: Payload is missing
    }
}
