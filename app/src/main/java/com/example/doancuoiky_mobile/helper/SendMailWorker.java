package com.example.doancuoiky_mobile.helper;

import android.content.Context;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.doancuoiky_mobile.ChangePasswordPage;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendMailWorker extends Worker {

    private String toEmail;
    private String subject;
    private String body;

    public SendMailWorker(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
        this.toEmail = getInputData().getString("toEmail");
        this.subject = getInputData().getString("subject");
        this.body = getInputData().getString("body");
    }

    @Override
    public Result doWork() {
        try {
            String fromEmail = "khuonggg2924@gmail.com";
            String password = "Khuong_2924"; // Đảm bảo bảo mật thông tin tài khoản

            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "587");

            // Tạo phiên làm việc
            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

            // Tạo email và gửi
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            // Gửi email
            Transport.send(message);

            return Result.success();  // Gửi thành công
        } catch (Exception e) {
            Log.e("SendMailWorker", "Error sending email", e);
            return Result.failure();  // Gửi thất bại
        }
    }
}
