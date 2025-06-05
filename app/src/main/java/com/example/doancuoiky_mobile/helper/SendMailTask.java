package com.example.doancuoiky_mobile.helper;

import android.content.Context;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.doancuoiky_mobile.ChangePasswordPage;

public class SendMailTask {

    private Context context;
    private String toEmail;
    private String subject;
    private String body;
    private ChangePasswordPage changePasswordPage;

    public SendMailTask(Context context, String toEmail, String subject, String body, ChangePasswordPage changePasswordPage) {
        this.context = context;
        this.toEmail = toEmail;
        this.subject = subject;
        this.body = body;
        this.changePasswordPage = changePasswordPage;
    }

    public void execute() {
        // Tạo WorkRequest gửi email
        WorkRequest sendMailRequest = new OneTimeWorkRequest.Builder(SendMailWorker.class)
                .setInputData(new androidx.work.Data.Builder()
                        .putString("toEmail", toEmail)
                        .putString("subject", subject)
                        .putString("body", body)
                        .build())
                .build();

        // Enqueue công việc gửi email
        WorkManager.getInstance(context).enqueue(sendMailRequest);
    }
}
