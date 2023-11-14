package com.unknownn.doctorpatient.others;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.exifinterface.media.ExifInterface;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUploader {

    private final Activity activity;
    private final Uri uri;
    private final String exactPath; // including filename, face.jpg
    private final ImageCallBack listener;

    public ImageUploader(Activity activity, Uri uri, String exactPath, ImageCallBack listener) {
        this.activity = activity;
        this.uri = uri;
        this.exactPath = exactPath;
        this.listener = listener;
    }

    public void startUploading(){
        rotateImageAndSendForUpload();
    }

    private void rotateImageAndSendForUpload(){
        try {
            Uri photoURI = uri;
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), photoURI);
            Matrix matrix = new Matrix();

            InputStream inputStream = activity.getContentResolver().openInputStream(photoURI);

            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            float rotate = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            if(rotate != 0) matrix.postRotate(rotate);

            Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();
            uploadFileToStorage(data);
        } catch (IOException e) {
            uploadFileToStorage();
        }
    }

    private void uploadFileToStorage(){

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(exactPath);
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();

        ref.putFile(uri,metadata)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        listener.onImageUploaded(String.valueOf(uri)))
                                .addOnFailureListener(e ->
                                        listener.onImageUploaded(null))
                )
                .addOnFailureListener(e -> listener.onImageUploaded(null));
    }

    private void uploadFileToStorage(byte[] imageArray){

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(exactPath);
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();

        ref.putBytes(imageArray,metadata)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        listener.onImageUploaded(String.valueOf(uri)))
                                .addOnFailureListener(e ->
                                        listener.onImageUploaded(null))
                )
                .addOnFailureListener(e -> listener.onImageUploaded(null));

    }
}
