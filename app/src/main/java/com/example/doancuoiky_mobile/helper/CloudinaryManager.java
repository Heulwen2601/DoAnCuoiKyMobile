package com.example.doancuoiky_mobile.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CloudinaryManager {

    private static final String CLOUD_NAME = "dhp7ylyvh";
    private static final String API_KEY = "243273335899587";
    private static final String API_SECRET = "mw1tC0PLGBtkv_9Ypg2o5x7fD6A";

    private static Cloudinary cloudinary;

    public static Cloudinary getCloudinary() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", CLOUD_NAME,
                    "api_key", API_KEY,
                    "api_secret", API_SECRET));
        }
        return cloudinary;
    }

    public static void uploadImage(Uri imageUri, Context context, final OnImageUploadedListener listener) {
        new Thread(() -> {
            try {
                // Convert Uri to File
                File imageFile = getFileFromUri(imageUri, context);
                if (imageFile != null) {
                    // Upload image to Cloudinary
                    Map uploadResult = getCloudinary().uploader().upload(imageFile, ObjectUtils.emptyMap());

                    // Check result and send back the URL
                    if (uploadResult != null && uploadResult.containsKey("url")) {
                        String imageUrl = uploadResult.get("url").toString();
                        ((AppCompatActivity) context).runOnUiThread(() -> listener.onImageUploaded(imageUrl));
                    } else {
                        ((AppCompatActivity) context).runOnUiThread(() ->
                                listener.onImageUploadFailed(new Exception("Upload failed, no URL in response")));
                    }
                } else {
                    ((AppCompatActivity) context).runOnUiThread(() ->
                            listener.onImageUploadFailed(new Exception("Failed to convert Uri to File")));
                }
            } catch (Exception e) {
                ((AppCompatActivity) context).runOnUiThread(() -> listener.onImageUploadFailed(e));
            }
        }).start();
    }


    private static File getFileFromUri(Uri uri, Context context) throws IOException {
        String filePath = getRealPathFromURI(uri, context);
        Log.d("CloudinaryManager", "File path: " + filePath); // Ghi log kiểm tra
        if (filePath != null) {
            return new File(filePath);
        }
        return null;
    }


    private static String getRealPathFromURI(Uri uri, Context context) {
        String filePath = null;
        if (uri.getScheme().equals("content")) {
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(projection[0]);
                    filePath = cursor.getString(columnIndex);
                }
            }
        }
        return filePath;
    }

    public interface OnImageUploadedListener {
        void onImageUploaded(String imageUrl);
        void onImageUploadFailed(Exception e);
    }


    // Chuyển đổi URI thành đường dẫn file
    private static String getPathFromUri(Context context, Uri uri) {
        // Cách đơn giản để chuyển URI thành đường dẫn file, tuỳ thuộc vào trường hợp của bạn
        return uri.getPath(); // Chỉ là ví dụ, bạn có thể cần phương thức khác để lấy đường dẫn thực tế
    }
}
