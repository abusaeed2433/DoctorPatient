package com.unknownn.doctorpatient.book_appointment.model;

import android.text.SpannableString;

public class SpanItem {

    private int index;
    private SpannableString spannableString;

    private String dateOrTime;

    private boolean isHighlighted;

    public SpanItem(int index, SpannableString spannableString, String dateOrTime, boolean isHighlighted) {
        this.index = index;
        this.spannableString = spannableString;
        this.dateOrTime = dateOrTime;
        this.isHighlighted = isHighlighted;
    }

    public void switchIsHighlighted(){
        isHighlighted = !isHighlighted;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public String getDateOrTime() {
        return dateOrTime;
    }

    public void setDateOrTime(String dateOrTime) {
        this.dateOrTime = dateOrTime;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public SpannableString getSpannableString() {
        return spannableString;
    }

    public void setSpannableString(SpannableString spannableString) {
        this.spannableString = spannableString;
    }
}
