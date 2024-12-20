package com.example.projectmobile;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class Cus_Toast extends android.app.Dialog {
    public Cus_Toast(@NonNull Context context, String message, String cancel) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cus_toast);

        TextView messagetxt = findViewById(R.id.message);
        TextView cancelbutton = findViewById(R.id.cancel);

        messagetxt.setText(message);
        cancelbutton.setText(cancel);

        cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }
}
