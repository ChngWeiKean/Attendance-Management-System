package com.example.attendancemanagementsystem;

import android.os.AsyncTask;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    public interface EmailCallback {
        void onEmailSent();
        void onError(Exception e);
    }

    private static final String SMTP_HOST = "sandbox.smtp.mailtrap.io";
    private static final String SMTP_PORT = "2525";
    private static final String SMTP_USERNAME = "020edeb03d297b";
    private static final String SMTP_PASSWORD = "e4ae13f1953959";

    public static void sendEmail(final String recipientEmail, final String subject, final String content, final EmailCallback callback) {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    // Set up JavaMail properties
                    Properties properties = new Properties();
                    properties.put("mail.smtp.host", SMTP_HOST);
                    properties.put("mail.smtp.port", SMTP_PORT);
                    properties.put("mail.smtp.auth", "true");

                    // Create a session
                    Session session = Session.getInstance(properties, new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                        }
                    });

                    // Create a MimeMessage
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress("no-reply-CheckMateAttendance@no-reply.com"));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
                    message.setSubject(subject);
                    message.setContent(content, "text/html");

                    // Send the message
                    Transport.send(message);

                    return null; // No error
                } catch (Exception e) {
                    return e; // Return the exception
                }
            }

            @Override
            protected void onPostExecute(Exception e) {
                if (e == null) {
                    callback.onEmailSent();
                } else {
                    callback.onError(e);
                }
            }
        }.execute();
    }
}
