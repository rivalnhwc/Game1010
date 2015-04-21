package com.rival.game1010;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/*
* @author:Rival
* @time:2015.04.20
* */

public class MainActivity extends Activity {
    private boolean shouldEliminate = false;
    private boolean shouldReopen = false;
    private boolean firstMove = true;
    private int[][] cards = new int[10][10];
    private int[][] temps = new int[10][10];
    private int[][] eliminate = new int[10][10];
    private float modleX[] = {0, 0, 0};
    private float modleY[] = {0, 0, 0};
    private float modleXt[] = {0, 0, 0};
    private float modleYt[] = {0, 0, 0};
    private float textPositionX;
    private float textPositionY;
    private float refreshPositionX;
    private float refreshPositionY;
    private float length;
    private float startX;
    private float startY;
    private float preX;
    private float preY;
    private float width;
    private float height;
    private float narrowTimes = 0.5f;
    private final static float narrowTimesX = 0.04f;
    private final static float narrowTimesXY = 0.04f;
    private int modleMove = -1;
    private int count = 0;
    private int sourceX = 0;
    private int sourceY = 0;
    private int scoreHigh = 0;
    private int score;
    private int addscore=0;
    private Bitmap bitmapRefresh;
    private int[][] modle_one;
    private int[][] modle_two;
    private int[][] modle_three;
    private int[] modleFree = new int[]{1, 1, 1};
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
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        bitmapRefresh = BitmapFactory.decodeResource(getResources(), R.drawable.refresh);
        setContentView(new GameView2(this));
        sharedPreferences = this.getSharedPreferences("1010",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        scoreHigh = sharedPreferences.getInt("highScore",0);
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
            if (addscore!=0){
                score+=addscore;
                addscore=0;
            }
            if (shouldReopen){
                score=0;
            }
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
            paintCard.setTextSize(80);
            paintCard.setTextAlign(Paint.Align.CENTER);
            canvas.drawBitmap(bitmapRefresh, refreshPositionX, refreshPositionY, paintCard);
            if (score>scoreHigh){
                scoreHigh =score;
                editor = sharedPreferences.edit();
                editor.putInt("highScore",scoreHigh);
                editor.commit();
            }
            canvas.drawText(score+" | "+scoreHigh, textPositionX, textPositionY, paintCard);
            if (count == 0 || shouldReopen) {
                random[0] = ((int) (Math.random() * 20)) % 13 + 1;
                random[1] = ((int) (Math.random() * 21)) % 13 + 1;
                random[2] = ((int) (Math.random() * 22)) % 13 + 1;
                modle_one = getModleType(random[0], modle_one);
                modle_two = getModleType(random[1], modle_two);
                modle_three = getModleType(random[2], modle_three);
                modles[0] = modle_one;
                modles[1] = modle_two;
                modles[2] = modle_three;
                for (int i = 0; i < 3; i++) {
                    modleFree[i] = 1;
                }
                count = 3;
                if (!checkIfHavePlace()) {
                    Toast.makeText(getApplicationContext(), "失败了", Toast.LENGTH_SHORT).show();
                    shouldReopen = true;
                    invalidate();
                }
                shouldReopen = false;
            }
            for (int k = 0; k < 3; k++) {
                paintCard.setColor(findColor(random[k]));
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {

                        if (modles[k][i][j] > 0) {
                            RectF rectF = new RectF(modleX[k] + length * j, modleY[k] + length * i, modleX[k] + length * (j + 1) - 3, modleY[k] + length * (i + 1) - 3);
                            canvas.drawRoundRect(rectF, 10, 10, paintCard);
                        }

                    }
                }
            }
            if (shouldEliminate) {
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (eliminate[i][j] > 0) {
                            paintCard.setColor(findColor(eliminate[i][j]));
                            if (narrowTimes + narrowTimesXY * (Math.abs(sourceX - i) + Math.abs(sourceY - j)) < 0.5f) {
                                float narrowTimesM = narrowTimes + narrowTimesXY * (Math.abs(sourceX - i) + Math.abs(sourceY - j));
                                RectF rectF = new RectF(startX + length * (i - narrowTimesM + 0.5f), startY + length * (j - narrowTimesM + 0.5f), startX + length * (i + 0.5f + narrowTimesM) - 3, startY + length * (j + 0.5f + narrowTimesM) - 3);
                                canvas.drawRoundRect(rectF, 10, 10, paintCard);
                            } else {
                                RectF rectF = new RectF(startX + length * i, startY + length * j, startX + length * (i + 1) - 3, startY + length * (j + 1) - 3);
                                canvas.drawRoundRect(rectF, 10, 10, paintCard);
                            }
                        }
                    }
                }
                narrowTimes -= narrowTimesX;
                if (narrowTimes < 0) {
                    shouldEliminate = false;
                    narrowTimes = 0.5f;
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            eliminate[i][j] = 0;
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
                    if (modleMove != -1&&modleMove!=4) {
                        preX = x;
                        preY = y;
                    }else if (modleMove==4){
                        score =0;
                        shouldReopen = true;
                        modleMove=-1;
                        initData();
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (modleMove != -1) {
                        for (int i = 0; i < 10; i++) {
                            for (int j = 0; j < 10; j++) {
                                cards[i][j] = temps[i][j];
                            }
                        }
                        if (firstMove) {
                            modleX[modleMove] += x - preX;
                            modleY[modleMove] += y - preY - 200;
                            firstMove = false;
                        }
                        modleX[modleMove] += (x - preX);
                        modleY[modleMove] += (y - preY);
                        int changeTempX = (int) (modleX[modleMove] / length - 1 + 0.5f);
                        int changeTempY = (int) ((modleY[modleMove] - startY) / length + 0.5f);
                        boolean success = true;
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 3; j++) {
                                if (modles[modleMove][i][j] > 0) {
                                    if (changeTempX + j < 10 && changeTempY + i < 10 && changeTempY >= 0 && changeTempX >= 0) {
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
                                    if (modles[modleMove][i][j] > 0) {
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
                    if (modleMove != -1) {
                        int changeX = (int) (modleX[modleMove] / length - 1 + 0.5f);
                        int changeY = (int) ((modleY[modleMove] - startY) / length + 0.5f);
                        boolean success = true;
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 3; j++) {
                                if (modles[modleMove][i][j] > 0) {
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
                            sourceX = changeX;
                            sourceY = changeY;
                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {
                                    if (modles[modleMove][i][j] > 0) {
                                        if (changeX + j < 10 && changeY + i < 10) {
                                            cards[changeX + j][changeY + i] = random[modleMove];
                                            addscore++;
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
                            modleFree[modleMove] = 0;
                            modleX[modleMove] = width;
                            modleY[modleMove] = height;
                            if (count == 0) {
                                for (int i = 0; i < 3; i++) {
                                    modleX[i] = modleXt[i];
                                    modleY[i] = modleYt[i];
                                }
                            }
                            checkIfeliminate();
                            if (!checkIfHavePlace()) {
                                Toast.makeText(getApplicationContext(), "没有空位了", Toast.LENGTH_SHORT).show();

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

    private boolean checkIfHavePlace() {
        if (count == 0) {
            return true;
        }
        for (int i = 0; i < 3; i++) {
            if (modleFree[i] == 1) {
                for (int y = 0; y < 10; y++) {
                    for (int x = 0; x < 10; x++) {
                        //每个小方格检查一遍
                        boolean success = true;
                        for (int z = 0; z < 3; z++) {
                            for (int j = 0; j < 3; j++) {
                                if (modles[i][z][j] > 0) {
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
                        if (success) {
                            return true;
                        }
                    }
                }
            }
        }
        initData();
        shouldReopen = true;
        return false;
    }

    private boolean checkIfeliminate() {
        for (int i = 0; i < 10; i++) {
            boolean fill = true;
            for (int j = 0; j < 10; j++) {
                if (cards[i][j] == 0) {
                    fill = false;
                }
            }
            if (fill) {
                for (int j = 0; j < 10; j++) {
                    temps[i][j] = 0;
                }
            }
        }
        for (int j = 0; j < 10; j++) {
            boolean fill = true;
            for (int i = 0; i < 10; i++) {
                if (cards[i][j] == 0) {
                    fill = false;
                }
            }
            if (fill) {
                for (int i = 0; i < 10; i++) {
                    temps[i][j] = 0;
                }
            }
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (cards[i][j] != temps[i][j]) {
                    eliminate[i][j] = cards[i][j];
                    addscore++;
                    shouldEliminate = true;
                }
            }
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                cards[i][j] = temps[i][j];
            }
        }
    return  shouldEliminate;
    }

    private int checkInWhichModle(float x, float y) {
        int res = -1;
        for (int i = 0; i < 3; i++) {
            if (x - modleX[i] > 0 && x - modleX[i] < length * 3 && y - modleY[i] > 0 && y - modleY[i] < length * 3) {
                res = i;
                break;
            }
        }
        if (x > refreshPositionX && x < refreshPositionX + bitmapRefresh.getWidth() &&
                y > refreshPositionY && y < refreshPositionY + bitmapRefresh.getHeight()) {
            res=4;
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
            case 6:
                color = Color.rgb(0xD5,0x79,0xC6);
                break;
            case 7:
                color = Color.rgb(0x8C,0xC6,0xC4);
                break;
            case 8:
                color = Color.rgb(0xA1,0xFF,0x91);
                break;
            case 9:
                color = Color.rgb(0xA8,0xE8,0x25);
                break;
            case 10:
                color = Color.rgb(0x37,0x71,0xBA);
                break;
            case 11:
                color = Color.rgb(0x6F,0xF0,0xF9);
                break;
            case 12:
                color = Color.rgb(0xFF,0xC2,0xCC);
                break;
            case 13:
                color = Color.rgb(0x41,0xFF,0xDD);
                break;
        }

        return color;
    }

    private void initData() {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();//屏幕分辨率容器
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        length = mDisplayMetrics.widthPixels / 12;
        width = mDisplayMetrics.widthPixels;
        height = mDisplayMetrics.heightPixels;
        startX = length;
        startY = height / 6;
        textPositionX = width / 2;
        textPositionY = height / 8;
        refreshPositionX = width * 5 / 6;
        refreshPositionY = height / 13;
        modleX[0] = width / 7;
        modleX[1] = width * 2 / 5;
        modleX[2] = width * 2 / 3;
        modleY[0] = height * 3 / 4;
        modleY[1] = height * 3 / 4;
        modleY[2] = height * 3 / 4;
        score=0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                cards[i][j] = 0;
                temps[i][j] = 0;
                eliminate[i][j] = 0;
            }
        }

        for (int i = 0; i < 3; i++) {
            modleXt[i] = modleX[i];
            modleYt[i] = modleY[i];
        }


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
            case 6:
                typeModle = new int[][]{{random, random, 0}, {0, 0, 0}, {0, 0, 0}};
                break;
            case 7:
                typeModle = new int[][]{{random, 0, 0}, {random, 0, 0}, {0, 0, 0}};
                break;
            case 8:
                typeModle = new int[][]{{random, 0, 0}, {0, 0, 0}, {0, 0, 0}};
                break;
            case 9:
                typeModle = new int[][]{{random, random, 0}, {random, 0, 0}, {0, 0, 0}};
                break;
            case 10:
                typeModle = new int[][]{{random, random, random}, {random, 0, 0}, {random, 0, 0}};
                break;
            case 11:
                typeModle = new int[][]{{random, 0, 0}, {random, 0, 0}, {random, random, random}};
                break;
            case 12:
                typeModle = new int[][]{{random, random, 0}, {0, random, 0}, {0, 0, 0}};
                break;
            case 13:
                typeModle = new int[][]{{0, random, 0}, {random, random, 0}, {0, 0, 0}};
                break;

        }
        return typeModle;
    }


}
