package com.example.uclab3;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MotionActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion);

        String time = getIntent().getStringExtra("time");
        TextView detailsText = findViewById(R.id.detailsText);
        detailsText.setText(getString(R.string.motion_details, time));
    }
}
