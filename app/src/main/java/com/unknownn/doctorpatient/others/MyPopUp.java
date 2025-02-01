package com.unknownn.doctorpatient.others;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.unknownn.doctorpatient.R;

public class MyPopUp extends Dialog {
    private TextView tvTitle, tvMessage, tvMoreInfo;
    private ImageView ivInfoIcon;
    private LinearLayout llOptions;
    private Button buttonClose;
    private DismissListener dismissListener = null;
    private ButtonClickListener clickListener = null;

    public MyPopUp(@NonNull Context context){
        super(context);
        init();
    }

    public MyPopUp(@NonNull Context context, String title, String message) {
        super(context);
        init();
        setUpData(title,message);
    }

    public MyPopUp(@NonNull Context context, String title, String message, String moreInfo, String[] allInfo) {
        super(context);
        init();
        setUpData(context,title,message,moreInfo,allInfo);
    }

    private void setUpData(@NonNull Context context, String title, String message, String moreInfo, String[] allInfo){
        tvTitle.setText(title);
        tvMessage.setText(message);
        tvMoreInfo.setText(moreInfo);

        if(allInfo != null){
            for(String info : allInfo){
                View view = LayoutInflater.from(context).inflate(R.layout.single_tv_only,llOptions,false);
                TextView textView = view.findViewById(R.id.tvSingle);
                textView.setText(info);

                llOptions.addView(view);
            }
        }
    }

    public void showTheseData(String title, String message, String buttonText){
        setUpData(title,message);
        if(buttonClose != null) buttonClose.setText(buttonText);
    }

    public void showTheseData(@NonNull Context context, String title, String message, String moreInfo, String[] allInfo,String buttonText){
        setUpData(context,title,message,moreInfo,allInfo);
        if(buttonClose != null) buttonClose.setText(buttonText);
    }

    private void setUpData(String title,String message){
        if(title == null){
            tvTitle.setVisibility(View.INVISIBLE);
            ivInfoIcon.setVisibility(View.VISIBLE);
        }
        else{
            tvTitle.setVisibility(View.VISIBLE);
            ivInfoIcon.setVisibility(View.INVISIBLE);
        }

        tvTitle.setText(title);
        tvMessage.setText(message);
        tvMoreInfo.setVisibility(View.GONE);
        llOptions.setVisibility(View.GONE);
    }


    private void setClickListener(){
        buttonClose.setOnClickListener(view-> {
            if(clickListener != null){
                dismissListener = null; // to prevent being called on dismissed listener
                clickListener.onButtonClicked();
            }
            super.dismiss();
        });
    }

    private void init(){
        setContentView(R.layout.pop_up_message_layout);
        tvTitle = findViewById(R.id.tvPopUpTitle);
        ivInfoIcon = findViewById(R.id.ivInfoIcon);
        tvMessage = findViewById(R.id.tvPopUpMessage);
        tvMoreInfo = findViewById(R.id.tvPopUpMoreInfo);
        llOptions = findViewById(R.id.llPopUp);
        buttonClose = findViewById(R.id.buttonClosePopUp);

        setClickListener();

        Window window = getWindow();
        if(window != null){
            window.setBackgroundDrawable(new ColorDrawable(0));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setWindowAnimations(R.style.dialogAnimation);
        }

        setOnDismissListener(dialogInterface -> {
            if(dismissListener != null)
                dismissListener.onDismissed();
        });

    }

    public void setCancelable(boolean cancelable){
        super.setCancelable(cancelable);
    }

    @SuppressWarnings("unused")
    public void setDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public void setClickListener(String buttonText,ButtonClickListener clickListener){
        if(buttonClose != null) buttonClose.setText(buttonText);
        this.clickListener = clickListener;
    }


    public interface DismissListener{
        void onDismissed();
    }

    public interface ButtonClickListener{
        void onButtonClicked();
    }

}
