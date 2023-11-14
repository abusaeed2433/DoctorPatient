package com.unknownn.doctorpatient.others;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;

import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class FileUploader {

    private final long MAXIMUM_FILE_SIZE = 15; // 15MB
    private final Activity activity;
    private final FileListener fileListener;
    private final String pathToFolder;
    private String fName;
    private boolean wasIndicatorShowing = false;

    public FileUploader(Activity activity, String pathToFolder, FileListener fileListener){
        this.activity = activity;
        this.pathToFolder = pathToFolder;
        this.fileListener = fileListener;
    }

    public void uploadFile(@NonNull Uri uri,String ext){
        fName = getFileNameWithX(uri);
        long size = getFileSize(uri);

        if(size > MAXIMUM_FILE_SIZE){
            if(wasIndicatorShowing){
                wasIndicatorShowing = false;
                fileListener.hideIndicator();
            }
            fileListener.onErrorOccurred("File size can be maximum "+MAXIMUM_FILE_SIZE+"MB");
        }
        else {
            if(size > 1) {
                fileListener.showIndicator();
                wasIndicatorShowing = true;
            }

            StorageReference ref = FirebaseStorage.getInstance().getReference().child(pathToFolder).child(ext).child(fName);
            ref.putFile(uri)
                    .addOnSuccessListener(taskSnapshot ->
                            ref.getDownloadUrl()
                                    .addOnSuccessListener(uri2 -> {
                                        if(wasIndicatorShowing){
                                            wasIndicatorShowing = false;
                                            fileListener.hideIndicator();
                                        }
                                        fileListener.onUploadCompleted(String.valueOf(uri2), fName);
                                    })
                                    .addOnFailureListener(e -> {
                                        if(wasIndicatorShowing){
                                            wasIndicatorShowing = false;
                                            fileListener.hideIndicator();
                                        }
                                        fileListener.onErrorOccurred(e.getMessage());
                                    })
                    )

                    .addOnFailureListener(e -> {
                        if(wasIndicatorShowing){
                            wasIndicatorShowing = false;
                            fileListener.hideIndicator();
                        }
                        fileListener.onErrorOccurred(e.getMessage());
                    })
                    .addOnProgressListener(snapshot -> {

                        long tran = snapshot.getBytesTransferred();
                        long total = snapshot.getTotalByteCount();
                        int percent = (int) (100*tran / total);
                        fileListener.onProgressUpdated(percent);

                    });
        }

    }

    private long getFileSize(Uri uri){
        long size = -1;
        try {
            AssetFileDescriptor afd = activity.getContentResolver().openAssetFileDescriptor(uri,"r");
            if(afd != null) {
                long totalSize = afd.getLength() / 1024;// in KB
                size = totalSize / 1024;//in MB
                afd.close();
            }
        }catch (Exception ignored){}
        return size;
    }

    private String getFileNameWithX(Uri uri){//X = extension
        String name = null;
        if(uri.getScheme()!=null){
            if(uri.getScheme().equals("content")){
                Cursor cursor = activity.getContentResolver().query(uri,null,null,null,null);
                try {
                    if(cursor!=null && cursor.moveToFirst()){
                        name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                        cursor.close();
                    }
                }catch (Exception ignored){}
            }
        }
        if (name == null && uri.getPath()!=null) {
            name = new File(uri.getPath()).getName();
        }

        if(name != null) return name;

        return System.currentTimeMillis()+"";
    }

}
