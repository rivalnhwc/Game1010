package com.rival.game1010;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/*
* @author:Rival
* @time:2015.04.20
* */

public class MainActivity extends Activity {
    private boolean shouldEliminate =false;
    private boolean shouldReopen=false;
    private boolean firstMove=true;
    private int[][] cards = new int[10][10];
    private int[][] temps = new int[10][10];
    private int[][] eliminate = new int[10][10];
    private float modleX[] = {0, 0, 0};
    private float modleY[] = {0, 0, 0};
    private float modleXt[] = {0, 0, 0};
    private float modleYt[] = {0, 0, 0};
    private float length;
    private float startX;
    private float startY;
    private float preX;
    private float preY;
    private float width;
    private float height;
    private float narrowTimes= 0;
    private final static float narrowTimesX=0.03f;
    private int modleMove = -1;
    private int count = 0;
    private int[][] modle_one;
    private int[][] modle_two;
    private int[][] modle_three;
    private int[] modleFree = new int[]{1,1,1};
    private int[][][] modles = new int[][][]{
            {
                    {1, 1, 1}, {1, 1, 1}, {1, 1, 1}
            },
            {
                    {1, 1, 1}, {1, 1, 1}, {1, 1, 1}
            },
            {
                    {1, 1, 1}, {1, 1, 1}, {1, 1, 1}
            }
    };
    private int random[] = new int[]{0, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(new GameView2(this));


    }

    class GameView2 extends View {
        Context context;

        public GameView2(Context context) {
            super(context);
            this.context = context;
            this.setWillNotDraw(false);
        }


        @Override
        protected void onDraw(Canvas canvas) {
            Paint paintCard = new Paint();
            paintCard.setAntiAlias(true);
            paintCard.setStyle(Paint.Style.FILL);
            paintCard.setColor(Color.GRAY);
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    paintCard.setColor(findColor(cards[j][i]));
                    RectF rectF = new RectF(startX + length * j, startY + length * i, startX + length * (j + 1) - 3, startY + length * (i + 1) - 3);
                    canvas.drawRoundRect(rectF, 10, 10, paintCard);
                    paintCard.setColor(Color.GRAY);

                }
            }
            if (count == 0||shouldReopen) {
                random[0] = ((int) (Math.random() * 11)) % 5 + 1;
                random[1] = ((int) (Math.random() * 12)) % 5 + 1;
                random[2] = ((int) (Math.random() * 13)) % 5 + 1;
                modle_one = getModleType(random[0], modle_one);
                modle_two = getModleType(random[1], modle_two);
                modle_three = getModleType(random[2], modle_three);
                modles[0] = modle_one;
                modles[1] = modle_two;
                modles[2] = modle_three;
                for (int i=0;i<3;i++){
                    modleFree[i]=1;
                }
                count = 3;
                if (!checkIfHavePlace()){
                    Toast.makeText(getApplicationContext(),"3个失败了",Toast.LENGTH_SHORT).show();
                    shouldReopen=true;
                    invalidate();
                 }
                shouldReopen=false;
            }
            for (int k = 0; k < 3; k++) {
                paintCard.setColor(findColor(random[k]));
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {

                        if (modles[k][i][j] >0) {
                            RectF rectF = new RectF(modleX[k] + length * j, modleY[k] + length * i, modleX[k] + length * (j + 1) - 3, modleY[k] + length * (i + 1) - 3);
                            canvas.drawRoundRect(rectF, 10, 10, paintCard);
                        }

                    }
                }
            }
            if (shouldEliminate){

                for (int i =0;i<10;i++){
                    for (int j =0;j<10;j++){
                        if (eliminate[i][j]>0){
                           paintCard.setColor(findColor(eliminate[i][j]));
                           RectF rectF = new RectF(startX + length * (i+narrowTimes), startY + length * (j+narrowTimes), startX + length * (i + 1-narrowTimes) - 3, startY + length * (j + 1-narrowTimes) - 3);
                           canvas.drawRoundRect(rectF, 10, 10, paintCard);
                        }
                    }
                }
                narrowTimes+=narrowTimesX;
                if (narrowTimes>0.5f){
                    shouldEliminate =false;
                    narrowTimes=0;
                    for (int i=0;i<10;i++){
                        for (int j=0;j<10;j++){
                            eliminate[i][j]=0;
                        }
                    }
                }
                invalidate();
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    modleMove = checkInWhichModle(x, y);
                    if (modleMove != -1) {
                        preX = x;
                        preY = y;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (modleMove != -1) {
                        for (int i = 0; i < 10; i++) {
                            for (int j = 0; j < 10; j++) {
                                cards[i][j] = temps[i][j];
                            }
                        }
                        if (firstMove){
                            modleX[modleMove] += x - preX;
                            modleY[modleMove] += y - preY-200;
                            firstMove = false;
                        }
                        modleX[modleMove] += (x - preX);
                        modleY[modleMove] += (y - preY);
                        int changeTempX = (int) (modleX[modleMove] / length - 1 + 0.5f);
                        int changeTempY = (int) ((modleY[modleMove] - startY) / length + 0.5f);
                        boolean success = true;
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 3; j++) {
                                if (modles[modleMove][i][j] >0) {
                                    if (changeTempX + j < 10 && changeTempY + i < 10&& changeTempY >= 0 && changeTempX >= 0) {
                                        if (cards[changeTempX + j][changeTempY + i] > 0) {
                                            success = false;
                                        }
                                    } else {
                                        success = false;
                                    }
                                }

                            }
                        }
                        if (success) {
                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {
                                    if (modles[modleMove][i][j] >0) {
                                        if (changeTempX + j < 10 && changeTempY + i < 10) {
                                            cards[changeTempX + j][changeTempY + i] = -1;
                                        }
                                    }

                                }
                            }
                        }
                        preX = x;
                        preY = y;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (modleMove!=-1) {
                        int changeX = (int) (modleX[modleMove] / length - 1 + 0.5f);
                        int changeY = (int) ((modleY[modleMove] - startY) / length + 0.5f);
                        boolean success = true;
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 3; j++) {
                                if (modles[modleMove][i][j] >0) {
                                    if (changeX + j < 10 && changeY + i < 10) {
                                        if (cards[changeX + j][changeY + i] > 0) {
                                            success = false;
                                        }
                                    } else {
                                        success = false;
                                    }
                                }

                            }
                        }
                        if (success) {
                            Log.e("position", "成功了");
                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {
                                    if (modles[modleMove][i][j] >0) {
                                        if (changeX + j < 10 && changeY + i < 10) {
                                            cards[changeX + j][changeY + i] = random[modleMove];
                                        }
                                    }

                                }
                            }
                            for (int i = 0; i < 10; i++) {
                                for (int j = 0; j < 10; j++) {
                                    temps[i][j] = cards[i][j];
                                }
                            }
                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {
                                    modles[modleMove][i][j] = 0;
                                }
                            }

                            count--;
                            modleFree[modleMove]=0;
                            modleX[modleMove]=width;
                            modleY[modleMove]=height;
                           if (count==0){
                               for (int i =0;i<3;i++){
                                   modleX[i]=modleXt[i];
                                   modleY[i]=modleYt[i];
                               }
                           }
                            checkIfeliminate();
                            if (!checkIfHavePlace()){
                                Toast.makeText(getApplicationContext(),"失败了",Toast.LENGTH_SHORT).show();

                            }

                        } else {
                            for (int i = 0; i < 10; i++) {
                                for (int j = 0; j < 10; j++) {
                                    cards[i][j] = temps[i][j];
                                }
                            }
                           modleX[modleMove] = modleXt[modleMove];
                           modleY[modleMove] = modleYt[modleMove];
                        }

                        firstMove = true;
                        modleMove = -1;
                    }
                    break;
            }
            invalidate();

            return true;

        }
    }
    private boolean checkIfHavePlace(){
        if (count==0){
            return true;
        }
        for (int i =0;i<3;i++){
           if (modleFree[i]==1){
               for (int y=0;y<10;y++){
                   for (int x =0;x<10;x++){
                       //每个小方格检查一遍
                       boolean success=true;
                       for (int z = 0; z < 3; z++) {
                           for (int j = 0; j < 3; j++) {
                               if (modles[i][z][j] >0) {
                                   if (x + j < 10 && y + z < 10) {
                                       if (cards[x + j][y + z] > 0) {
                                           success = false;
                                       }
                                   } else {
                                       success = false;
                                   }
                               }

                           }
                       }
                       if (success){
                           return true;
                       }
                   }
               }
           }
       }
        initData();
        shouldReopen=true;
       return false;
    }
    private void checkIfeliminate(){
        for (int i=0;i<10;i++){
            boolean fill = true;
            for (int j=0;j<10;j++){
               if (cards[i][j]==0){
                   fill=false;
               }
            }
            if (fill){
                for (int j=0;j<10;j++){
                    temps[i][j]=0;
                }
            }
        }
        for (int j=0;j<10;j++){
            boolean fill = true;
            for (int i=0;i<10;i++){
                if (cards[i][j]==0){
                    fill=false;
                }
            }
            if (fill){
                for (int i=0;i<10;i++){
                    temps[i][j]=0;
                }
            }
        }
        for (int i =0;i<10;i++){
            for (int j =0;j<10;j++){
                if (cards[i][j] != temps[i][j]){
                    eliminate[i][j]=cards[i][j];
                    shouldEliminate=true;
                }
            }
        }
        for (int i =0;i<10;i++){
            for (int j =0;j<10;j++){
                cards[i][j] = temps[i][j];
            }
        }

    }
    private int checkInWhichModle(float x, float y) {
        int res = -1;
        for (int i = 0; i < 3; i++) {
            if (x - modleX[i] > 0 && x - modleX[i] < length * 3 && y - modleY[i] > 0 && y - modleY[i] < length * 3) {
                res = i;
                break;
            }
        }
        return res;

    }
    private int findColor(int colorNum) {
        int color = Color.BLUE;
        switch (colorNum) {
            case -1:
                color = Color.rgb(0xCF, 0xC9, 0xCF);
                break;
            case 0:
                color = Color.GRAY;
                break;
            case 1:
                color = Color.rgb(0xFF, 0xD3, 0x9B);
                break;
            case 2:
                color = Color.rgb(0x7C, 0x64, 0xE0);
                break;
            case 3:
                color = Color.rgb(0x8E, 0xE9, 0xE9);
                break;
            case 4:
                color = Color.rgb(0xF3, 0xA7, 0x94);
                break;
            case 5:
                color = Color.rgb(0xA0, 0xE7, 0xA0);
                break;
        }

        return color;
    }
    private int[][] getModleType(int random, int[][] typeModle) {
        switch (random) {
            case 1:
                typeModle = new int[][]{{random, 0, 0}, {random, random, 0}, {0, 0, 0}};
                break;
            case 2:
                typeModle = new int[][]{{random, random, 0}, {random, random, 0}, {0, 0, 0}};
                break;
            case 3:
                typeModle = new int[][]{{random, random, random}, {random, random, random}, {random, random, random}};
                break;
            case 4:
                typeModle = new int[][]{{random, 0, 0}, {random, 0, 0}, {random, 0, 0}};
                break;
            case 5:
                typeModle = new int[][]{{random, random, random}, {0, 0, 0}, {0, 0, 0}};
                break;

        }
        return typeModle;
    }
    private void initData() {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();//屏幕分辨率容器
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        length = mDisplayMetrics.widthPixels / 12;
        width = mDisplayMetrics.widthPixels;
        height = mDisplayMetrics.heightPixels;
        startX = length;
        startY = height / 6;
        modleX[0] = width / 7;
        modleX[1] = width * 2/5;
        modleX[2] = width * 2/3;
        modleY[0] = height * 3/4;
        modleY[1] = height * 3/4;
        modleY[2] = height * 3/4;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                cards[i][j] = 0;
                temps[i][j] = 0;
                eliminate[i][j]=0;
            }
        }

        for (int i =0;i<3;i++){
            modleXt[i]=modleX[i];
            modleYt[i]=modleY[i];
        }


    }


}
