package com.example.sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Integer gridRows;
    Integer gridColumn;
    Integer screenWidth;
    Integer screenHeight;
    ArrayList<ArrayList<Cell>> gridArray;
    Integer numOfBombs;
    Set<Pair<Integer, Integer>> bombSet;
    TextView timer;
    TextView bombCount;
    Integer countDown;
    Integer totalTimeOut;
    Integer bomb_blast;
    SharedPreferences sharedPrefs;
    Boolean isSoundOn;
    String difficulty;
    PopupWindow popupWindow;
    CountDownTimer countTimer;
    long completedTime;
    MediaPlayer mplayer;

    Set<String> previousOpenedButtons;

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences.Editor editor = sharedPrefs.edit();

        Iterator<Pair<Integer, Integer>> iter = bombSet.iterator();
        int i=0;
        while(iter.hasNext())
        {
            i++;
            Pair<Integer, Integer> bombPos = iter.next();
            editor.putInt("bombX"+i, bombPos.first);
            editor.putInt("bombY"+i, bombPos.second);
        }
        editor.putStringSet("openedButtons", previousOpenedButtons);
        editor.putBoolean("previousStateAvailable", true);
        editor.putLong("remainingTime", totalTimeOut-completedTime);
        editor.commit();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        Boolean isPreviousStatePresent = false;

        sharedPrefs = getSharedPreferences("minesweeper_data", MODE_PRIVATE);
        isSoundOn = sharedPrefs.getBoolean("Sound", true);
        difficulty = sharedPrefs.getString("Difficulty", "Easy");
        long remainingTime = sharedPrefs.getLong("remainingTime",0);
        isPreviousStatePresent = sharedPrefs.getBoolean("previousStateAvailable", false);

        // Initializations
        gridRows = 11;
        gridColumn = 7;
        numOfBombs = 6;
        if (difficulty == "Easy")
        {
            countDown = 420;
        }
        else if(difficulty == "Medium")
        {
            countDown = 300;
        }
        else{
            countDown = 180;
        }
        totalTimeOut = countDown * 1000;
        bomb_blast = 1;
        bombSet = new HashSet<Pair<Integer, Integer>>();
        gridArray = new ArrayList<ArrayList<Cell>>();
        previousOpenedButtons = new HashSet<String>();

        bombCount = (TextView)findViewById(R.id.bombCount);
        bombCount.setText(numOfBombs.toString());
        bombCount.setTextColor(getResources().getColor(R.color.grey));
        bombCount.setTextSize(40);

        timer = (TextView)findViewById(R.id.timer);
        timer.setText("12:34:56");
        timer.setTextColor(getResources().getColor(R.color.grey));
        timer.setTextSize(40);

        Point point1 = new Point();
        getWindowManager().getDefaultDisplay().getSize(point1);
        screenHeight = point1.y;
        screenWidth = point1.x;

        if (isPreviousStatePresent && (remainingTime < totalTimeOut))
        {
            int prevInt = (int)(remainingTime/1000);
            countDown = prevInt;
            totalTimeOut = (prevInt * 1000);
        }
        countTimer = new CountDownTimer(totalTimeOut, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                completedTime = totalTimeOut - millisUntilFinished;
                timer.setText(getTime(countDown));
                countDown-=1;
            }

            @Override
            public void onFinish() {
                loadNextScreen("Timed Out!!","TimeOut");
            }
        }.start();


        Random random = new Random();
        if (isPreviousStatePresent)
        {
            String name = "bomb";
            for(int i = 1; i <= numOfBombs; i++)
            {
                bombSet.add(new Pair<Integer,Integer>(sharedPrefs.getInt(name+"X"+i, 0), sharedPrefs.getInt(name+"Y"+i, 0)));
            }

        }
        else {
            for (int iter = 0; iter < numOfBombs; iter++) {
                Pair<Integer, Integer> num = new Pair<Integer, Integer>(iter, 0);
                if (bombSet.contains(num)) {
                    iter--;
                } else {
                    bombSet.add(num);
                }
            }
        }

        for(int row = 0; row < gridRows; row++)
        {
            ArrayList<Cell> rowArray = new ArrayList<Cell>();
            rowArray.clear();
            for(int col = 0; col < gridColumn; col++)
            {
                Integer num = 0;
                if (bombSet.contains(new Pair(row, col)))
                {
                    Cell cellObj = new Cell(99, false);
                    rowArray.add(cellObj);
                    continue;
                }

                // previous row check
                if ((row - 1) >= 0)
                {
                    for(int tempCol = col-1; tempCol < col+2 ; tempCol++)
                    {
                        if(tempCol >= 0 && tempCol < gridColumn) {
                            if (bombSet.contains(new Pair(row - 1, tempCol))) num++;
                        }
                    }
                }

                //current row check
                for(int tempCol = col-1; tempCol < col+2 ; tempCol = tempCol+2)
                {
                    if(tempCol >= 0 && tempCol < gridColumn) {
                        if (bombSet.contains(new Pair(row, tempCol))) num++;
                    }
                }

                // next row check
                if ((row + 1) < gridRows)
                {
                    for(int tempCol = col-1; tempCol < col+2 ; tempCol++)
                    {
                        if(tempCol >= 0 && tempCol < gridColumn) {
                            if (bombSet.contains(new Pair(row + 1, tempCol))) num++;
                        }
                    }
                }
                Cell cellObj = new Cell(num, false);
                rowArray.add(cellObj);
            }
            gridArray.add(rowArray);
        }

        if (isPreviousStatePresent)
        {
            Set<String> clickedButtons = sharedPrefs.getStringSet("openedButtons", new HashSet<>());
            Iterator<String> iter = clickedButtons.iterator();
            while(iter.hasNext())
            {
                String temp = iter.next();
                int row, column;
                int buttonIndex = temp.lastIndexOf("on");
                int buttonId = Integer.parseInt(temp.substring(buttonIndex+2));

                row = buttonId / gridColumn;
                column = buttonId % gridColumn;
                checkPressed(findViewById(getResources().getIdentifier(temp,"id", getPackageName())), false, row, column );
            }
        }

        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putBoolean("previousStateAvailable", false);
        edit.commit();

    }

    private void changePressed(Button button) {
        bomb_blast++;
        if (bomb_blast>5) bomb_blast = 5;
        String temp = "bomb_blast" + bomb_blast;
        button.setBackgroundResource(getResources().getIdentifier(temp, "drawable", getPackageName()));
    }

    private void blastOtherBombs(Button button, Integer rowNum, Integer colNum)
    {
        bomb_blast = 1;
        CountDownTimer blast_timer1 = new CountDownTimer(1000, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
                changePressed(button);
            }

            @Override
            public void onFinish() {
                bombSet.remove(new Pair(rowNum, colNum));
                otherBlasts(0,0);
            }
        }.start();
    }

    public void checkPressed(View view)
    {
        checkPressed(view, false, -1, -1);
    }

    private String getTime(Integer seconds)
    {
        String result = "";
        Integer time = seconds/(60*60);
        if(time<10)
        {
            result+="0";
        }
        result+=time.toString();
        seconds=seconds%(60*60);

        result+=":";

        time = seconds/60;
        if(time<10){
            result+="0";
        }
        result+=time.toString();
        seconds=seconds%60;
        result+=":";

        if(seconds<10){
            result+="0";
        }
        result+=seconds;

        return result;
    }

    @SuppressLint("ResourceType")
    public void checkPressed(View view, Boolean isInternalCall, Integer rowNum, Integer colNum)
    {
        Integer pressedRowNum;
        Integer pressedColNum;

        if (!isInternalCall)
        {
            Integer pressedCellNum = Integer.parseInt(view.getTag().toString());
            pressedCellNum--;
            pressedRowNum = pressedCellNum / gridColumn;
            pressedColNum = pressedCellNum % gridColumn;
            if (isSoundOn) {
                mplayer = MediaPlayer.create(getApplicationContext(), R.raw.button_press);
                mplayer.start();
            }
            previousOpenedButtons.add("button" + (pressedCellNum+1));
        }
        else
        {
            pressedRowNum = rowNum;
            pressedColNum = colNum;
        }

        Cell cell = null;
        if ((pressedRowNum >= 0)&&(pressedRowNum<=gridRows)&&(pressedColNum>=0)&&(pressedColNum<=gridColumn)) {
            cell = gridArray.get(pressedRowNum).get(pressedColNum);
        }

        if (isInternalCall && (cell.getCellContent()!=0))return;

        Button pressedButton = null;
        if (isInternalCall==false || (isInternalCall && (cell.getCellContent()==0)))
        {
            cell.isOpen = true;
            pressedButton = (Button) view;
            pressedButton.setBackgroundResource(R.drawable.button_press);
            pressedButton.setTextColor(getResources().getColor(R.color.black));
            pressedButton.setTextSize(25);
            if (cell.getCellContent() != 99) {
                pressedButton.setText(cell.getCellContent().toString());
            }
        }

        //First check if bomb exists.
        if (!isInternalCall)
        {
            checkIfBombPresent(cell.getCellContent(), pressedRowNum, pressedColNum, pressedButton);
        }

        if (checkForGameOver())
        {
            String hs1 = sharedPrefs.getString("highScore1", "");
            String hs2 = sharedPrefs.getString("highScore2", "");
            String hs3 = sharedPrefs.getString("highScore3", "");
            String status = "Won";

            Integer highScore1 = 0;
            Integer highScore2 = 0;
            Integer highScore3 = 0;

            if (hs1 != "") {
                highScore1 = convertStringTimeToSeconds(hs1);
            }

            if(hs2 != "") {
                highScore2 = convertStringTimeToSeconds(hs2);
            }
            if(hs3 != "") {
                highScore3 = convertStringTimeToSeconds(hs3);
            }

            SharedPreferences.Editor editor = sharedPrefs.edit();

            if (hs1 == "")
            {
                editor.putString("highScore1", getTime((int) completedTime/1000));
                status = "highScore1";
            }
            else if(completedTime/1000 < highScore1)
            {
                editor.putString("highScore1", getTime((int) completedTime/1000));
                editor.putString("highScore2", hs1);
                editor.putString("highScore3", hs2);
                status = "highScore1";
            }
            else
            {
                if (hs2 == "")
                {
                    editor.putString("highScore2", getTime((int) completedTime/1000));
                    status = "highScore2";
                }
                else if((completedTime/1000 < highScore2) && (completedTime/1000 > highScore1))
                {
                    editor.putString("highScore2", getTime((int) completedTime/1000));
                    editor.putString("highScore3", hs2);
                    status = "highScore2";
                }
                else
                    {
                    if (hs3 == "")
                    {
                        editor.putString("highScore3", getTime((int) completedTime/1000));
                        status = "highScore3";
                    }
                    else if((completedTime/1000 < highScore3) && (completedTime/1000 > highScore2))
                    {
                        editor.putString("highScore3", getTime((int) completedTime/1000));
                        status = "highScore3";
                    }
                }
            }
            editor.commit();
            loadNextScreen("You won!!", status);
        }

        if (cell.getCellContent() == 0)
        {
            String temp;
            Integer cal = 0;
            if ((pressedRowNum+1 < gridRows) && (gridArray.get(pressedRowNum+1).get(pressedColNum).isOpen == false)) {
                cal = (pressedRowNum+1) * gridColumn + pressedColNum + 1;
                temp = "button" + cal;
                View view1 = findViewById(getResources().getIdentifier(temp, "id", getPackageName()));
                checkPressed(view1, true, pressedRowNum + 1, pressedColNum);
            }

            if ((pressedRowNum-1 >= 0) && (gridArray.get(pressedRowNum-1).get(pressedColNum).isOpen == false)) {
                cal = (pressedRowNum-1) * gridColumn + pressedColNum + 1;
                temp = "button" + cal;
                View view1 = findViewById(getResources().getIdentifier(temp, "id", getPackageName()));
                checkPressed(view1, true, pressedRowNum - 1, pressedColNum);
            }

            if ((pressedColNum+1 < gridColumn) && (gridArray.get(pressedRowNum).get(pressedColNum+1).isOpen == false)) {
                cal = (pressedRowNum) * gridColumn + pressedColNum + 2;
                temp = "button" + cal;
                View view1 = findViewById(getResources().getIdentifier(temp, "id", getPackageName()));
                checkPressed(view1, true, pressedRowNum, pressedColNum + 1);
            }

            if ((pressedColNum-1 >= 0) && (gridArray.get(pressedRowNum).get(pressedColNum-1).isOpen == false)) {
                cal = (pressedRowNum) * gridColumn + pressedColNum;
                temp = "button" + cal;
                View view1 = findViewById(getResources().getIdentifier(temp, "id", getPackageName()));
                checkPressed(view1, true, pressedRowNum, pressedColNum - 1);
            }
        }
    }

    private Integer convertStringTimeToSeconds(String time)
    {
        String[] nums = time.split(":");
        Integer totalSecs = 0;
        if (nums.length == 3) {
            for (int i = 0; i < nums.length; i++) {
                if (i == 0) {
                    totalSecs = totalSecs + Integer.parseInt(nums[i]) * 60 * 60;
                } else if (i == 1) {
                    totalSecs = totalSecs + Integer.parseInt(nums[i]) * 60;
                } else {
                    totalSecs += Integer.parseInt(nums[i]);
                }
            }
        }
        return totalSecs;
    }

    private void loadNextScreen(String message, String status)
    {
        if(isSoundOn) {
            if ((status == "Failed") || (status == "TimeOut")) {
                mplayer = MediaPlayer.create(getApplicationContext(), R.raw.sad_end);
                mplayer.start();
            } else if ((status == "Won") || (status == "highScore1") || (status == "highScore2") || (status == "hignScore3")) {
                mplayer = MediaPlayer.create(getApplicationContext(), R.raw.happy_end);
                mplayer.start();
            }
        }
        Intent end_intent = new Intent(this, FinishActivity.class);
        end_intent.putExtra("message", message);
        end_intent.putExtra("status", status);
        startActivity(end_intent);
        finish();
    }

    private void checkIfBombPresent(Integer cellContent, Integer pressedRowNumber, Integer pressedColNum, Button pressedButton)
    {
        //First change that cell color.
        if (cellContent == 99)
        {
            pressedButton.setBackgroundResource(R.drawable.bomb_blast1);
            bomb_blast = 1;
            mplayer = MediaPlayer.create(getApplicationContext(), R.raw.bomb_blast);
            mplayer.start();
            CountDownTimer blast_timer = new CountDownTimer(500, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    changePressed(pressedButton);
                }

                @Override
                public void onFinish() {

                    bombSet.remove(new Pair(pressedRowNumber, pressedColNum));
                    otherBlasts(pressedRowNumber, pressedColNum);
                }
            }.start();
        }
    }

    public void otherBlasts(Integer pressedRowNumber, Integer pressedColNum) {
        //Second change remaining bombs color.
        Iterator<Pair<Integer, Integer>> iter = bombSet.iterator();
        if (bombSet.isEmpty())
        {
            loadNextScreen("Try Again!!", "Failed");
        }
        else
        {
            if(isSoundOn) {
                mplayer = MediaPlayer.create(getApplicationContext(), R.raw.bomb_blast);
                mplayer.start();
            }
            Pair<Integer, Integer> value = iter.next();
            Integer rowNum = value.first;
            Integer colNum = value.second;

            Integer cal = rowNum * gridColumn + colNum + 1;
            String temp = "button" + cal;
            Button but = (Button) findViewById(getResources().getIdentifier(temp, "id", getPackageName()));
            blastOtherBombs(but,rowNum,colNum);
        }
    }

    //Returns true if game completed,else false.
    private Boolean checkForGameOver()
    {
        for( int i = 0; i < gridArray.size(); i++ )
        {
            for(int j=0; j < gridArray.get(i).size(); j++)
            {
                if (!(((gridArray.get(i).get(j).getCellContent() == 99) && (gridArray.get(i).get(j).isOpen == false))|| (gridArray.get(i).get(j).isOpen == true)))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public void mainOptionsPressed(View view)
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.options, null);

        popupWindow = new PopupWindow(popUpView, screenWidth*3/5, screenHeight*1/5 , true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0,0);
        countTimer.cancel();

    }

    public void resumeMe(View view)
    {
        countTimer = new CountDownTimer(totalTimeOut - completedTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                completedTime = millisUntilFinished;
                timer.setText(getTime(countDown));
                countDown-=1;
            }

            @Override
            public void onFinish() {
                loadNextScreen("Timed Out!!", "TimeOut");
            }
        }.start();

        popupWindow.dismiss();
    }

    public void homePressed(View view)
    {
        Intent homeIntent = new Intent(this, EntryActivity.class);
        startActivity(homeIntent);
        finish();
    }

    public void restartPressed(View view)
    {
        Intent restartIntent = new Intent(this, MainActivity.class);
        startActivity(restartIntent);
        finish();
    }
}