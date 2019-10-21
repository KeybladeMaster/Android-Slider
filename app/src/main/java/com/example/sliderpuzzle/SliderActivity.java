package com.example.sliderpuzzle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

public class SliderActivity extends AppCompatActivity
{
    int[] gameArray;
    RelativeLayout layout;

    //Assorted variables used for defining game board and pieces.
    int boardSize = 4;
    int offset = 10;

    int screenWidth;
    int screenHeight;
    int initialX;
    int initialY;
    int boardWidth;
    int boardHeight;
    int tileWidth;
    int tileHeight;
    int mainBar;
    int returnHome;
    int statsDisplay;

    //Variable to keep track of game in progress
    boolean playing = true;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //Hides title bar
        setContentView(R.layout.activity_slider);

        //Get the layout
        layout = findViewById(R.id.gameLayout);

        //Get the size of the screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        //Defining various variables for size of game board and tiles, and starting points.
        mainBar = (int)(screenWidth * 0.2);

        initialX = (int)(screenWidth * 0.025);
        initialY = (int)(screenHeight * 0.025) + mainBar;

        boardWidth = (int)(screenWidth * 0.95);
        boardHeight = ((int)(screenHeight * 0.95)) - mainBar - 50;

        tileWidth = (boardWidth / boardSize);
        tileHeight = (boardHeight / boardSize);


        //Sizes for views on the Main Bar
        returnHome = screenWidth / 10;
        statsDisplay = screenWidth / 5;

        //Start a game
        newGame();
    }

    //Starts a new game.
    void newGame()
    {
        //Make playing variable true
        playing = true;
        boolean solvable = false;

        //Initiate game board array
        gameArray = new int[boardSize * boardSize];
        for(int i = 0; i < boardSize * boardSize; i++)
        {
            gameArray[i] = i;
        }

        //Check if shuffled board is solvable. If not, reshuffle.
        while(!solvable)
        {
            shuffleArray(gameArray);
            solvable = isSolvable(gameArray);
        }

        drawBoard();
    }

    //Function for determining if generated puzzle is solvable or not
    boolean isSolvable(int[] gameBoard)
    {
        boolean solvable = false;
        int inversions = 0;

        //When a larger number appears before a smaller number, increase the amount of  inversions.
        for(int i = 0; i < gameBoard.length - 1; i++)
        {
            for(int j = i + 1; j < gameBoard.length; j++)
            {
                if(gameBoard[i] != 0 && gameBoard[j] != 0 && gameBoard[i] > gameBoard[j])
                    inversions++;
            }
        }

        //For odd sized boards, puzzle is solvable if even number of inversions
        //For even sized boards, puzzle is solvable is inversions plus the row of the blank space is odd
        if(boardSize % 2 == 1)
        {
            if (inversions % 2 == 0)
                solvable = true;
            else
                solvable = false;
        }
        else if(boardSize % 2 == 0)
        {
            int blankLocation = findIndexOf(gameBoard, 0) / boardSize;

            if ((inversions + blankLocation) % 2 == 1)
                solvable = true;
            else
                solvable = false;
        }

        return solvable;
    }

    //Function to draw the game board
    private void drawBoard()
    {
        //Clears the screen layout before drawing
        //TODO Erase when tiles get animated, will no longer have to continuously redraw screen.
        layout.removeAllViews();

        //Draws the top bar
        drawHomeBar();

        //Create game board
        for(int i = 0; i < gameArray.length; i++)
        {
            if(gameArray[i] != 0)
            {
                TextView tv = new TextView(this);
                tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setId(gameArray[i]);
                tv.setText(String.valueOf(gameArray[i]));
                tv.setGravity(Gravity.CENTER);
                tv.setBackgroundColor(Color.BLUE);
                tv.setTextColor(Color.WHITE);

                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tileHeight / 3);


                tv.setWidth(tileWidth - (offset * 2));
                tv.setHeight(tileHeight - (offset * 2));

                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateMove(v);
                    }
                });

                float xCoord = (float) ((initialX + (tileWidth * (i % boardSize))) + offset);
                float yCoord = (float) ((initialY + (tileHeight * (i / boardSize))) + offset);

                tv.setX(xCoord);
                tv.setY(yCoord);

                layout.addView(tv);
            }
        }
    }

    //Draws the upper bar
    void drawHomeBar()
    {
        //Blank textview used for bar background
        TextView tvBackground = new TextView(this);
        tvBackground.setWidth(screenWidth);
        tvBackground.setHeight(mainBar);
        tvBackground.setX(0); tvBackground.setY(0);
        tvBackground.setBackgroundColor(Color.DKGRAY);

        //Return to menu button
        TextView tvGoHome = new TextView(this);
        tvGoHome.setText("X");
        tvGoHome.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainBar / 3);
        tvGoHome.setBackgroundColor(Color.WHITE);
        tvGoHome.setWidth(returnHome - (offset * 2));
        tvGoHome.setHeight(mainBar - (offset * 2));
        tvGoHome.setX(initialX + offset);
        tvGoHome.setY(offset);
        tvGoHome.setGravity(Gravity.CENTER);

        //Display for move counter
        TextView tvMoves = new TextView(this);
        tvMoves.setText("Moves: 00");
        tvMoves.setGravity(Gravity.CENTER);
        tvMoves.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainBar / 5);
        tvMoves.setBackgroundColor(Color.WHITE);
        tvMoves.setWidth(statsDisplay - (offset * 2));
        tvMoves.setHeight(mainBar - (offset * 2));
        tvMoves.setX((int)(screenWidth * 0.6) + offset  - (int)(screenWidth * 0.025));
        tvMoves.setY(offset);

        //Display for timer
        TextView tvTime = new TextView(this);
        tvTime.setText("00:00");
        tvTime.setGravity(Gravity.CENTER);
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainBar / 4);
        tvTime.setBackgroundColor(Color.WHITE);
        tvTime.setWidth(statsDisplay - (offset * 2));
        tvTime.setHeight(mainBar - (offset * 2));
        tvTime.setX((int)(screenWidth * 0.8) + offset - (int)(screenWidth * 0.025));
        tvTime.setY(offset);

        //Add views to the layout
        layout.addView(tvBackground);
        layout.addView(tvGoHome);
        layout.addView(tvMoves);
        layout.addView(tvTime);
    }

    //moves a tile and updates board if a possible move is done.
    public void updateMove(View tv)
    {
        if(playing)
        {
            int blankLocation = findIndexOf(gameArray, 0);
            int clickedLocation = findIndexOf(gameArray, tv.getId());
            int range = Math.abs(blankLocation - clickedLocation);

            if (range == boardSize || (range == 1 && (Math.abs((blankLocation % boardSize) - (clickedLocation % boardSize))) == 1)) {
                swap(gameArray, blankLocation, clickedLocation);
                drawBoard();
            }

            checkGameOver(gameArray);
        }
    }

    //Check if the game is over. If so, end it and display a message saying the puzzle is solved.
    void checkGameOver(int[] gameBoard)
    {
        boolean win = true;
        int check = 0;

        for (int i = 0; i < gameBoard.length; i++)
        {
            if (i + 1 == gameBoard[i])
                check += 1;
        }

        //If puzzle is solved
        if (check == gameBoard.length - 1)
        {
            playing = false;
            displayPopup();
        }
    }

    //Popup window once puzzle is solved. Tells user they finished, option to play again or go to main menu.
    void displayPopup()
    {
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupLayout = layoutInflater.inflate(R.layout.layout_popup, null);

        //Create and configure the popup window
        final PopupWindow popup = new PopupWindow(this);
        popup.setContentView(popupLayout);
        popup.setHeight((int)(screenHeight * 0.35));
        popup.setWidth((int)(screenWidth * 0.8));
        popup.setAnimationStyle(R.style.popupAnimation);
        popup.setOutsideTouchable(false);

        TextView popupText = popupLayout.findViewById(R.id.popupText);
        popupText.setText("Puzzle solved! Placeholder text. Will show ending stats (Move count and time taken to solve) as well as if the user got a high score or not");

        popup.showAtLocation(popupLayout, Gravity.CENTER, 0, 0);

        //Dims the background
        final View container = popup.getContentView().getRootView();
        Context context = popup.getContentView().getContext();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams param = (WindowManager.LayoutParams) container.getLayoutParams();
        param.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        param.dimAmount = 0.7f;
        manager.updateViewLayout(container, param);

        //Button OnClick listeners
        Button newGame = popupLayout.findViewById(R.id.popupButtonNewGame);
        newGame.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                //Dismiss the dimmed background and popup, and start a new game.
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                popup.dismiss();
                newGame();
            }
        });

    }

    //Helper function to get the index position of given number
    int findIndexOf(int[] array, int num)
    {
        for(int i = 0; i < array.length; i++)
        {
            if (array[i] == num)
                return i;
        }

        return -1;
    }

    //Shuffles the given array.
    void shuffleArray(int[] array)
    {
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < array.length; i++)
        {
            int change = i + random.nextInt(array.length - i);
            swap(array, i, change);
        }
    }

    //Helper function that swaps two values in an array
    void swap(int[] array, int a, int b)
    {
        int temp = array[a];
        array[a] = array[b];
        array[b] = temp;
    }
}
