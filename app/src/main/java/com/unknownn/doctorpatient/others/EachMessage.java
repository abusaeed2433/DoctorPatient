package com.unknownn.doctorpatient.others;

import android.view.Gravity;

public class EachMessage {
    private final String pushId,text,url;
    private final boolean isDoctor, isFile;

    public EachMessage(String pushId,String text, String url, boolean isDoctor, boolean isFile) {
        this.pushId = pushId;
        this.text = text;
        this.url = url;
        this.isDoctor = isDoctor;
        this.isFile = isFile;
    }

    public boolean isFile() {
        return isFile;
    }

    public boolean isSame(EachMessage item){
        return pushId.equals(item.pushId);
    }

    public boolean fullySame(EachMessage item){
        return pushId.equals(item.pushId) &&
                ( text != null && text.equals(item.text) ) &&
                ( url != null && url.equals(item.url) ) &&
                (isDoctor == item.isDoctor);
    }

    public boolean isUrlValid(){
        return url != null && !url.isEmpty();
    }

    public String getText() {
        return text;
    }

    public boolean showText(){
        return url == null;
    }

    public int getGravity(boolean amIDoctor){
        if(isDoctor == amIDoctor) {
            return Gravity.END;
        }
        return Gravity.START;
    }

    public String getUrl() {
        return url;
    }

    public boolean isDoctor() {
        return isDoctor;
    }

}
