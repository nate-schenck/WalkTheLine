package com.example.walktheline;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.walktheline.views.Line;
import com.example.walktheline.views.Platform;
import com.example.walktheline.views.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    List<Platform> platforms = new ArrayList<Platform>();

    private Line line;
    private static MainActivity main;

    int score; //score to be kept
    int state; //0 - in game; 1 - game over; 2 - in animation;
    int highscore; // high score

    int startPlatDist;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startPlatDist = 0;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        spawnNewPlatform(); // spawn the first "goal" platform

        line = findViewById(R.id.walk_line);
        ConstraintLayout layout = findViewById(R.id.main);

        score = 0;
        state = 0;
        main = this;

        highscore = 0;
        try {
             highscore = getHighScore();
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateHighScore();

        Player player = findViewById(R.id.player); //The player canvas
        TextView gameOverText = findViewById(R.id.game_over_text); //'Game Over'
        TextView touchText = findViewById(R.id.touch_text); //"Touch anywhere..."
        Handler handler = new Handler(); // to run the runnables
        final boolean[] pressed = {false};
        Runnable growRunnable = new Runnable() { //Grows the line by recursion of this runnable
            @Override
            public void run() {
                if (pressed[0] == true) {
                    line.setHeight(line.getLineHeight() + 13);
                    line.invalidate();
                    line.requestLayout();
                    handler.post(this); // loop the runnable
                }
            }
        };

        Runnable fallRunnable = new Runnable() { //Changes and the angle of the line by recursion of this runnable
            @Override
            public void run() {
                if (line.getAngle() > 0) {
                    line.setAngle(line.getAngle() - 5);
                    line.invalidate();
                    line.requestLayout();
                    if (line.getAngle() < 0) {
                        line.setAngle(0);
                    }
                    handler.post(this); // loop the runnable
                }
            }
        };

        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    pressed[0] = true; // Sets the screen to pressed so the line will grow in its recursion
                    if (state == 2) return true; // if we are animating, dont do anything
                    if (state == 1) { // if the game is over...
                        player.setVisibility(View.GONE);
                        line.setVisibility(View.GONE);
                        line.setAngle(90);
                        line.setHeight(0);
                        gameOverText.setVisibility(View.GONE);
                        touchText.setVisibility(View.GONE);
                        for (Platform p : platforms) { //remove every platform
                            layout.removeView(p);
                        }
                        platforms.clear(); // and delete them from the arraylist
                        Animation a = new TranslateAnimation(0, startPlatDist, 0, 0);
                        a.setDuration(0);
                        findViewById(R.id.starting_platform).startAnimation(a); // move the starting platform back into view
                        findViewById(R.id.starting_platform).setVisibility(View.VISIBLE);
                        player.setVisibility(View.VISIBLE);
                        line.setVisibility(View.VISIBLE);
                        spawnNewPlatform();
                        if (score > highscore) {
                            try {
                                setHighScore(score);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            updateHighScore();
                            try {
                                highscore = getHighScore();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        score = 0;
                        ((TextView)findViewById(R.id.score)).setText(score + "");
                        return true;
                    }
                    handler.postDelayed(growRunnable, 10); // start the grow loop
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    pressed[0] = false; // Sets the screen to unpressed for the grow recursion
                    if (state == 2) return true; // if we're in an animation don't do anything
                    if (state == 1) { // if the game is waiting to be started, set the curr state to playing. everything else should already be done
                        state = 0;
                        return true;
                    }
                    if (state == 0 && line.getLineHeight() == 0) return true; // we dont want to drop a line of zero height, so here's this
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    state = 2; // set the game state to "in animation"
                    handler.post(fallRunnable); // start the falling line animation
                    Platform curr = platforms.get(platforms.size() - 1); // the right most platform
                    int minDist = curr.getDistance(); // left side of goal platform
                    int maxDist = curr.getDistance() + curr.getPlatformWidth(); // riht side of goal platform
                    int landingDist = (int)line.getLineHeight() + 300; // where the line landed
                    if (landingDist <= maxDist && landingDist >= minDist) { // if it landed on the platform
                        //Next stage
                        int distanceToMove = (curr.getDistance() + curr.getPlatformWidth()) - 300; // distance to move all objects for the animation
                        Animation a = new TranslateAnimation(0, -distanceToMove,0,0);
                        a.setDuration(100 * ((distanceToMove / 100) + 1)); // this keeps the speeds of different distances relatively the same
                        a.setInterpolator(new LinearInterpolator()); // animations move at a constant speed
                        a.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) { // this is what runs after all animations are done
                                curr.clearAnimation(); // prevents flickering after animation
                                if (score != 0) { // prevents flickering on the other platform
                                    platforms.get(platforms.size() - 2).clearAnimation();
                                }
                                else {
                                    findViewById(R.id.starting_platform).clearAnimation();
                                }
                                line.setVisibility(View.VISIBLE);
                                platforms.get(platforms.size() - 1).setTranslationX(distanceToMove); // so the platform knows where to be drawn
                                if (score == 0) findViewById(R.id.starting_platform).setVisibility(View.GONE); // if it's the first time, get rid of the starting platform
                                startPlatDist = distanceToMove; // we need to keep track of this, because this is how far the starting platform must move when we reset after a game over
                                if (platforms.size() > 1) { // get rid of the unnecessary platform if there is one
                                    layout.removeView(platforms.get(platforms.size() - 2));
                                    platforms.remove(platforms.get(platforms.size() - 2));
                                }
                                spawnNewPlatform(); // spawn the new "goal" platform
                                score++; // increase the score
                                TextView scoreView = findViewById(R.id.score);
                                scoreView.setText(score + ""); // show the score
                                if (score > highscore)  {
                                    try {
                                        setHighScore(score);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    updateHighScore();
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        //This is the same as Animation "a" but with a semi-empty "onAnimationEnd()"
                        Animation a2 = new TranslateAnimation(0, -distanceToMove,0,0);
                        a2.setDuration(100 * ((distanceToMove / 100) + 1));
                        a2.setInterpolator(new LinearInterpolator());
                        a2.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                state = 0;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        //Animation for the player's forward movement after the bridge drops, similar to the previous animations
                        Animation playerAnimFor = new TranslateAnimation(0, maxDist - 300, 0, 0);
                        playerAnimFor.setDuration(100 * ((distanceToMove / 100) + 1));
                        playerAnimFor.setStartOffset(500); // Waits for the bridge to fall
                        playerAnimFor.setInterpolator(new LinearInterpolator());
                        //Moves the player right back, at the same time as the platforms.
                        Animation playerAnimBack = new TranslateAnimation(0, -(maxDist - 300),0,0);
                        playerAnimBack.setInterpolator(new LinearInterpolator());
                        playerAnimBack.setDuration(100 * ((distanceToMove / 100) + 1));

                        playerAnimBack.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                player.clearAnimation();
                                player.setX(300);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        playerAnimFor.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) { // This plays directly after the player walks the bridge
                                player.setX(maxDist);
                                line.setVisibility(View.GONE); // reset the line to its starting position
                                line.setAngle(90);
                                line.setHeight(0);
                                player.startAnimation(playerAnimBack); // animate the player going back
                                curr.startAnimation(a); // animate the landing platform back to the starting position
                                if (score != 0) { // animate the old starting platform off the screen
                                    platforms.get(platforms.size() - 2).startAnimation(a2);
                                }
                                else {
                                    findViewById(R.id.starting_platform).startAnimation(a2);
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        player.startAnimation(playerAnimFor); // start the first animation, and subsequently the rest of them
                    }
                    else { // if the line did not land on the platform
                        //End game
                        state = 1; // set the state to "game over"
                        gameOverText.setVisibility(View.VISIBLE); //show the game over text
                        touchText.setVisibility(View.VISIBLE);
                    }
                }
                return true;
            }

        });

    }

    public void updateHighScore() {
        TextView highScoreView = findViewById(R.id.high_score);
        try {
            highScoreView.setText("High Score: " + getHighScore());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void spawnNewPlatform() { // This is called to spawn the new "goal" platform
        Random rand = new Random();
        int platformMinSize = 30;
        int platformMaxSize = 200;
        int platformSize = rand.nextInt(platformMaxSize - platformMinSize) + platformMinSize; // random platform size between the min and max
        int minDistance = 350;
        int maxDistance = Resources.getSystem().getDisplayMetrics().widthPixels - platformSize - 20;
        int platformDistance = rand.nextInt(maxDistance - minDistance) + minDistance; // random platform disatnce between the min and max
        ConstraintLayout layout = findViewById(R.id.main);
        Platform p = new Platform(this, platformSize, platformDistance, Color.rgb(rand.nextInt(125) + 100, rand.nextInt(125) + 100, rand.nextInt(125) + 100)); // create a new platform instance with random light color
        p.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int id = rand.nextInt(); // random ID so we can set the constraints
        p.setId(id);
        layout.addView(p);
        platforms.add(p); // add it to our group of platforms on the screen
        p.setStaticDistance(platformDistance); // must set this so it can properly do its distance calculations
        ConstraintSet set = new ConstraintSet();
        set.clone(layout); // load the current constraints
        set.connect(id, ConstraintSet.START, R.id.main, ConstraintSet.START); // constraint our platform
        set.connect(id, ConstraintSet.BOTTOM, R.id.main, ConstraintSet.BOTTOM);
        set.applyTo(layout);// set the layouts new constraints
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) { // puts the app in full screen
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public int getHighScore() throws IOException {
        File f  = new File(getFilesDir(), "highscore.txt"); // high score files contains only the high score
        if (f.createNewFile()) {
            return 0;
        }
        Scanner s = new Scanner(f);
        if (!s.hasNextInt()) return 0;
        int score = s.nextInt();
        s.close();
        return score;
    }

    public void setHighScore(int score) throws IOException{
        File f  = new File(getFilesDir(), "highscore.txt");
        f.createNewFile();
        OutputStreamWriter streamWriter = new OutputStreamWriter(openFileOutput("highscore.txt", Context.MODE_PRIVATE));
        streamWriter.write(score + ""); //rewrite the highscore file with the new high score
        streamWriter.close();
    }

    @Override
    public void onBackPressed() {
        //TODO: Pause Game, for now we go back to main menu
        finish();
        startActivity(new Intent(this, HomeScreen.class));
    }

}