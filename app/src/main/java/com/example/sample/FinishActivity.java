package com.example.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FinishActivity extends AppCompatActivity {

    Integer screenWidth;
    Integer screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        Point point1 = new Point();
        getWindowManager().getDefaultDisplay().getSize(point1);
        screenHeight = point1.y;
        screenWidth = point1.x;

        String passed_message = getIntent().getStringExtra("message");
        String status = getIntent().getStringExtra("status");

        TextView message = (TextView)findViewById(R.id.textView);
        ImageView finalImage = (ImageView) findViewById(R.id.finalImage);
        RelativeLayout centerBg = (RelativeLayout) findViewById(R.id.centerDisplay);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth,screenHeight);
        params.setMargins(screenWidth/8, screenHeight/8, screenWidth/8, screenHeight/8);
        centerBg.setLayoutParams(params);

        if (status.equals("highScore1"))
        {
            finalImage.setImageResource(R.drawable.crown1);
            message.setText("Ruler Arrived!");
        }
        else if(status.equals("highScore2"))
        {
            finalImage.setImageResource(R.drawable.crown2);
            message.setText("Super Second Spot..");
        }
        else if(status.equals("highScore3"))
        {
            finalImage.setImageResource(R.drawable.crown3);
            message.setText("Third from top...");
        }
        else if(status.equals("Won"))
        {
            finalImage.setImageResource(R.drawable.cake);
            message.setText(passed_message);
        }
        else if(status.equals("Failed"))
        {
            finalImage.setImageResource(R.drawable.target);
            message.setText(passed_message);
        }
        else{

            finalImage.setImageResource(R.drawable.timeout);
            message.setText("Improve time!!");
        }

        message.setTextColor(getResources().getColor(R.color.dominant_blue));
        message.setTextSize(40);

    }

    public void restartGame(View view) {
        Intent restartIntent = new Intent(this, MainActivity.class);
        startActivity(restartIntent);
        finish();
    }

    public void loadHome(View view) {
        Intent homeIntent = new Intent(this, EntryActivity.class);
        startActivity(homeIntent);
        finish();
    }
}