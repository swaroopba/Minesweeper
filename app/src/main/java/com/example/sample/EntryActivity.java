package com.example.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EntryActivity extends AppCompatActivity {

    ImageView bomb;
    TextView text;
    Handler handler;
    Runnable runnable;
    Integer bomb_id;
    Integer text_size;
    Boolean increase;
    Integer screenHeight;
    Integer screenWidth;
    PopupWindow popupWindow;
    ImageView title;
    View popUpView;
    MediaPlayer mplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entry);

        bomb = (ImageView)findViewById(R.id.imageView);
        title = (ImageView)findViewById(R.id.title);
        text = (TextView)findViewById(R.id.play);
        bomb_id = 2;
        text_size = 18;
        increase = true;

        SharedPreferences sharedPrefs = getSharedPreferences("minesweeper_data", MODE_PRIVATE);
        Boolean soundOn = sharedPrefs.getBoolean("Sound", true);
        mplayer = MediaPlayer.create(getApplicationContext(), R.raw.entry_music);

        if (soundOn) {
            mplayer.setLooping(true);
            mplayer.start();
        }


        Point point1 = new Point();
        getWindowManager().getDefaultDisplay().getSize(point1);
        screenHeight = point1.y;
        screenWidth = point1.x;

        changeTitlePosition(screenHeight*3/5);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                changeMe();
                handler.postDelayed(this, 300);
            }
        };
        handler.postDelayed(runnable, 300);
    }

    private void changeMusic(Boolean value)
    {
        if((value == false) && (mplayer.isPlaying()))
        {
            mplayer.stop();
            mplayer.release();
        }
        else if((value == true) && (!mplayer.isPlaying()))
        {
            mplayer = MediaPlayer.create(getApplicationContext(), R.raw.entry_music);
            mplayer.setLooping(true);
            mplayer.start();
        }
    }

    private void changeTitlePosition(Integer height)
    {
        RelativeLayout.LayoutParams marginParam = new RelativeLayout.LayoutParams(screenWidth, screenHeight);
        marginParam.bottomMargin = height;
        marginParam.leftMargin = screenWidth/12;
        marginParam.rightMargin = screenWidth/12;
        title.setLayoutParams(marginParam);
    }

    private void changeMe()
    {
        bomb_id++;
        if(bomb_id<=4)
        {
            title.setImageResource(R.drawable.title_1);
        }
        else
        {
            title.setImageResource(R.drawable.title_2);
        }

        if (bomb_id > 7)
        {
            bomb_id = 2;
        }

        String temp = "bomb" + bomb_id;
        bomb.setImageResource(getResources().getIdentifier(temp, "drawable", getPackageName()));

        if (increase) {
            text_size += 2;
        }
        else
            {
            text_size-=2;
        }
        text.setTextSize(text_size);
        if (text_size > 24){
            increase = false;
        }
        else if(text_size < 18)
        {
            increase = true;
        }
    }
    public void startGame(View view)
    {
        Intent startIntent = new Intent(this, MainActivity.class);
        changeMusic(false);
        startActivity(startIntent);
        finish();
    }

    public void exitApp(View view)
    {
        finish();
        System.exit(0);
    }

    public void displaySettings(View view)
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popUpView = inflater.inflate(R.layout.settings, null);

        changeTitlePosition(screenHeight*3/4);

        SharedPreferences sharedPrefs = getSharedPreferences("minesweeper_data", MODE_PRIVATE);
        Boolean soundOn = sharedPrefs.getBoolean("Sound", true);
        String difficulty = sharedPrefs.getString("Difficulty", "Easy");
        String hs1 = sharedPrefs.getString("highScore1", "");
        String hs2 = sharedPrefs.getString("highScore2", "");
        String hs3 = sharedPrefs.getString("highScore3", "");

        LinearLayout kingLayout = (LinearLayout)popUpView.findViewById(R.id.kingLayout);
        LinearLayout queenLayout = (LinearLayout)popUpView.findViewById(R.id.queenLayout);
        LinearLayout princeLayout = (LinearLayout)popUpView.findViewById(R.id.princeLayout);
        kingLayout.setVisibility(View.INVISIBLE);
        queenLayout.setVisibility(View.INVISIBLE);
        princeLayout.setVisibility(View.INVISIBLE);

        if (hs1.equals("") && hs2.equals("") && hs3.equals(""))
        {
            LinearLayout hsLayout = (LinearLayout) popUpView.findViewById(R.id.highScore);
            hsLayout.setVisibility(View.GONE);
        }

        if (!hs1.equals(""))
        {
            kingLayout.setVisibility(View.VISIBLE);
            ImageView kingImage = (ImageView)popUpView.findViewById(R.id.kingImage);
            TextView kingText = (TextView)popUpView.findViewById(R.id.kingText);

            kingImage.setImageResource(R.drawable.crown1);
            kingText.setText(hs1);
        }

        if (!hs2.equals(""))
        {
            queenLayout.setVisibility(View.VISIBLE);
            ImageView queenImage = (ImageView)popUpView.findViewById(R.id.queenImage);
            TextView queenText = (TextView)popUpView.findViewById(R.id.queenText);

            queenImage.setImageResource(R.drawable.crown2);
            queenText.setText(hs2);
        }

        if (!hs3.equals(""))
        {
            princeLayout.setVisibility(View.VISIBLE);
            ImageView princeImage = (ImageView)popUpView.findViewById(R.id.princeImage);
            TextView princeText = (TextView)popUpView.findViewById(R.id.princeText);

            princeImage.setImageResource(R.drawable.crown3);
            princeText.setText(hs3);
        }

        RadioGroup soundSettings = (RadioGroup)popUpView.findViewById(R.id.sound);
        if (soundOn) {
            soundSettings.check(R.id.soundOn);
        }
        else{
            soundSettings.check(R.id.soundOff);
        }


        RadioGroup difficultySettings = (RadioGroup)popUpView.findViewById(R.id.difficulty);
        if (difficulty == "Easy")
        {
            difficultySettings.check(R.id.easy);
        }
        else if(difficulty == "Medium")
        {
            difficultySettings.check(R.id.medium);
        }
        else{
            difficultySettings.check(R.id.hard);
        }

        popupWindow = new PopupWindow(popUpView, screenWidth*3/4, screenHeight*3/5 , true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0,0);
    }

    public void saveSettings(View view)
    {
        SharedPreferences sharedPrefs = getSharedPreferences("minesweeper_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        RadioGroup sound = (RadioGroup) popUpView.findViewById(R.id.sound);
        Integer soundSelected = sound.getCheckedRadioButtonId();
        if (soundSelected == R.id.soundOn)
        {
            editor.putBoolean("Sound", true);
            changeMusic(true);
        }
        else
        {
            editor.putBoolean("Sound", false);
            changeMusic(false);
        }

        RadioGroup difficulty = (RadioGroup) popUpView.findViewById(R.id.difficulty);
        Integer difficultyLevel = difficulty.getCheckedRadioButtonId();
        String previous = sharedPrefs.getString("Difficulty", "");
        if (difficultyLevel == R.id.easy)
        {
            if (previous != "Easy")
            {
                editor.putBoolean("previousStateAvailable", false);
            }
            editor.putString("Difficulty", "Easy");
        }
        else if(difficultyLevel == R.id.medium)
        {
            if (previous != "Medium")
            {
                editor.putBoolean("previousStateAvailable", false);
            }
            editor.putString("Difficulty", "Medium");
        }
        else
        {
            if (previous != "Hard")
            {
                editor.putBoolean("previousStateAvailable", false);
            }
            editor.putString("Difficulty", "Hard");
        }
        editor.commit();
        exitSettings(view);
    }

    public void exitSettings(View view)
    {
        if (popupWindow.isShowing())
        {
            changeTitlePosition(screenHeight*3/5);
            popupWindow.dismiss();
        }
    }
}