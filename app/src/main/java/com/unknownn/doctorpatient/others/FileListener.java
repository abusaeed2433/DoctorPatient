package com.unknownn.doctorpatient.others;

public interface FileListener {
    void onUploadCompleted(String url,String fileName);
    void onProgressUpdated(int progress);
    void onErrorOccurred(String error);
    void showIndicator();
    void hideIndicator();
}
