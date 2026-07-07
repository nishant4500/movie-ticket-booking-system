package com.moviebooking.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.backend.entity.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from-email}")
    private String fromEmail;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private void sendEmail(
            List<String> recipients,
            String subject,
            String content,
            boolean html
    ) {
        try {
            Map<String, Object> payload = new HashMap<>();

            payload.put("from", fromEmail);
            payload.put("to", recipients);
            payload.put("subject", subject);

            if (html) {
                payload.put("html", content);
            } else {
                payload.put("text", content);
            }

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 200
                    && response.statusCode() < 300) {

                System.out.println(
                        "Email sent successfully: "
                                + response.body()
                );

            } else {

                System.err.println(
                        "Failed to send email. Status: "
                                + response.statusCode()
                                + " Response: "
                                + response.body()
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Email API error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    @Async
  
public void sendOtpEmail(
        String adminEmail,
        String superAdminEmail,
        String otp
) {

    String html =
            "<h2>Aurora Cinemas Admin Login</h2>"
            + "<p>Your OTP is:</p>"
            + "<h1>" + otp + "</h1>"
            + "<p>This OTP is valid for 5 minutes.</p>";

    sendEmail(
            List.of("idwivedi1204@gmail.com"),
            "Aurora Cinemas Admin Login OTP",
            html,
            true
    );
}

    public void sendBookingConfirmation(
            String userEmail,
            Booking booking
    ) {

        String qrData =
                "Booking: "
                        + booking.getBookingNumber()
                        + " | Movie: "
                        + booking.getShow()
                        .getMovie()
                        .getMovieName()
                        + " | Date: "
                        + booking.getShow().getShowDate()
                        + " | Seats: "
                        + booking.getSelectedSeats();

        String encodedQrData = URLEncoder.encode(
                qrData,
                StandardCharsets.UTF_8
        );

        String qrUrl =
                "https://api.qrserver.com/v1/create-qr-code/"
                        + "?size=200x200&data="
                        + encodedQrData;

        String htmlContent =
                "<div style='font-family: Arial, sans-serif;"
                        + "max-width: 600px;"
                        + "margin: 0 auto;"
                        + "background-color: #1a1a2e;"
                        + "color: #fff;"
                        + "padding: 20px;"
                        + "border-radius: 10px;'>"

                        + "<div style='text-align:center;"
                        + "border-bottom:1px solid #444;"
                        + "padding-bottom:20px;"
                        + "margin-bottom:20px;'>"

                        + "<h1 style='color:#e50914;'>"
                        + "Booking Confirmed!"
                        + "</h1>"

                        + "<p style='color:#aaa;'>"
                        + "Your ticket is ready."
                        + "</p>"

                        + "</div>"

                        + "<div style='background-color:#16213e;"
                        + "padding:20px;"
                        + "border-radius:8px;'>"

                        + "<h2>"
                        + booking.getShow()
                        .getMovie()
                        .getMovieName()
                        + "</h2>"

                        + "<p><strong>Booking Number:</strong> "
                        + booking.getBookingNumber()
                        + "</p>"

                        + "<p><strong>Date & Time:</strong> "
                        + booking.getShow().getShowDate()
                        + " at "
                        + booking.getShow().getShowTime()
                        + "</p>"

                        + "<p><strong>Theatre:</strong> "
                        + booking.getShow()
                        .getScreen()
                        .getTheatre()
                        .getTheatreName()
                        + " ("
                        + booking.getShow()
                        .getScreen()
                        .getScreenName()
                        + ")</p>"

                        + "<p><strong>Seats:</strong> "
                        + booking.getSelectedSeats()
                        + "</p>"

                        + "<p><strong>Total Amount:</strong> Rs. "
                        + booking.getTotalAmount()
                        + "</p>"

                        + "</div>"

                        + "<div style='text-align:center;"
                        + "margin-top:20px;'>"

                        + "<p style='color:#aaa;'>"
                        + "Scan this QR Code at the theatre"
                        + "</p>"

                        + "<img src='"
                        + qrUrl
                        + "' "
                        + "alt='Ticket QR Code' "
                        + "style='border:4px solid #fff;"
                        + "border-radius:8px;' />"

                        + "</div>"

                        + "<div style='text-align:center;"
                        + "margin-top:30px;"
                        + "font-size:12px;"
                        + "color:#666;'>"

                        + "<p>"
                        + "Thank you for choosing our service. "
                        + "Enjoy the movie!"
                        + "</p>"

                        + "</div>"

                        + "</div>";

        sendEmail(
                List.of(userEmail),
                "Booking Confirmed - "
                        + booking.getBookingNumber(),
                htmlContent,
                true
        );
    }

    @Async
    public void sendCancellationEmail(
            String userEmail,
            Booking booking
    ) {

        String text =
                "Your booking has been cancelled."
                        + "\n\nBooking Number: "
                        + booking.getBookingNumber()
                        + "\nMovie: "
                        + booking.getShow()
                        .getMovie()
                        .getMovieName()
                        + "\nSeats: "
                        + booking.getSelectedSeats()
                        + "\nRefund of Rs. "
                        + booking.getTotalAmount()
                        + " will be processed.";

        sendEmail(
                List.of(userEmail),
                "Booking Cancelled - "
                        + booking.getBookingNumber(),
                text,
                false
        );
    }

    @Async
    public void sendNewAdminRegistrationNotification(
            String superAdminEmail,
            String adminName,
            String adminEmail
    ) {

        String htmlContent =
                "<div style='font-family:Arial,sans-serif;"
                        + "max-width:600px;"
                        + "margin:0 auto;"
                        + "background-color:#1a1a2e;"
                        + "color:#fff;"
                        + "padding:24px;"
                        + "border-radius:10px;'>"

                        + "<h1 style='color:#E5B769;'>"
                        + "New Admin Registration"
                        + "</h1>"

                        + "<p style='color:#aaa;'>"
                        + "A new theatre admin has registered "
                        + "and is waiting for your approval."
                        + "</p>"

                        + "<div style='background-color:#16213e;"
                        + "padding:20px;"
                        + "border-radius:8px;"
                        + "margin:20px 0;"
                        + "border-left:4px solid #E5B769;'>"

                        + "<p><strong>Name:</strong> "
                        + adminName
                        + "</p>"

                        + "<p><strong>Email:</strong> "
                        + adminEmail
                        + "</p>"

                        + "</div>"

                        + "<p style='color:#aaa;'>"
                        + "Please login to the Admin Dashboard "
                        + "and visit the "
                        + "<strong style='color:#E5B769;'>"
                        + "Approve Admins"
                        + "</strong> tab."
                        + "</p>"

                        + "<p style='font-size:12px;"
                        + "color:#555;"
                        + "margin-top:30px;'>"
                        + "Aurora Cinema Management System"
                        + "</p>"

                        + "</div>";

        sendEmail(
                List.of(superAdminEmail),
                "New Admin Registration Request - Action Required",
                htmlContent,
                true
        );
    }
}
