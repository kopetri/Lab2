package muc_15_01_14.lab2;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;


public class GameOverviewActivity extends ActionBarActivity {

    // Intent for every single round
    private Intent gameRound;
    // Bool whether master or client
    private boolean master;
    // Bool for game finished
    private boolean gameFinished;

    // Game data
    private int scoreMaster, scoreClient, currentRound;
    private String nameMaster, nameClient;

    // Countdown fpr Get Ready
    private CountDownTimer countDownTimer;

    // Request ID for Win or Lost
    public static int REQUEST_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameoverview);

        // Get data from previous activity
        if (getIntent().hasExtra(MainActivity.MASTER_KEY)) {
            master = getIntent().getBooleanExtra(MainActivity.MASTER_KEY, false);
        }
        if (getIntent().hasExtra(MainActivity.MASTER_USER_KEY)) {
            nameMaster = getIntent().getStringExtra(MainActivity.MASTER_USER_KEY);
        }
        if (getIntent().hasExtra(MainActivity.CLIENT_USER_KEY)) {
            nameClient = getIntent().getStringExtra(MainActivity.CLIENT_USER_KEY);
        }
        scoreClient = scoreMaster = 0;
        currentRound = 1;

        // Update data on screen
        updateDataOnScreen();
    }


    @Override
    protected void onResume() {
        super.onResume();

        ((TextView) findViewById(R.id.txt_countdownInt)).setText("Wait for challenger");

        // TODO on challenger available
        // Set countdown if game not finished yet
        if (!gameFinished) {
            ((TextView) findViewById(R.id.txt_countdownInt)).setText(Integer.toString(3));
            Countdown(3);
        }
    }


    // Countdown for Get Ready
    private void Countdown(final int seconds) {
        ((TextView) findViewById(R.id.txt_countdownInt)).setText(Integer.toString(seconds));
        countDownTimer = new CountDownTimer(seconds * 1000, 100) {
            @Override
            public void onTick(long l) {
                ((TextView) findViewById(R.id.txt_countdownInt)).setText(Integer.toString((int) Math.ceil(l / 1000f)));
            }

            @Override
            public void onFinish() {
                ((TextView) findViewById(R.id.txt_countdownInt)).setText("0");
                startRound();
            }
        };
        countDownTimer.start();
    }

    // Start new activity for game
    public void startRound() {
        gameRound = new Intent(getApplicationContext(), GameActivity.class);
        gameRound.putExtra(MainActivity.MASTER_KEY, master);
        startActivityForResult(gameRound, REQUEST_ID);
        //TODO send message to client to start game
    }


    // Listener for Back-Button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitGame();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Action before exit the game
    private void exitGame() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        this.finish();

    }

    // Update Data on Screen
    private void updateDataOnScreen() {
        ((TextView) findViewById(R.id.txt_nameMaster)).setText(nameMaster);
        ((TextView) findViewById(R.id.txt_nameClient)).setText(nameClient);

        ((TextView) findViewById(R.id.txt_roundInt)).setText(Integer.toString(currentRound));
        ((TextView) findViewById(R.id.txt_scoreMaster)).setText(Integer.toString(scoreMaster));
        ((TextView) findViewById(R.id.txt_scoreClient)).setText(Integer.toString(scoreClient));
    }


    // Get data from GameActivity to decide kind of view and winner or looser
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ID) {
            if (resultCode == RESULT_OK) {
                String retValue = data.getStringExtra(GameActivity.STATUS_KEY);
                Log.i("Result GameActivity", retValue);
                if (retValue != null) {

                    switch (retValue) {
                        case "exitRound":

                            break;
                        case "finishGame":
                            gameFinished = true;
                            roundWinner(data);
                            ((TextView) findViewById(R.id.txt_headline1)).setText("Result");
                            if (master) {
                                if (scoreMaster > scoreClient) {
                                    ((TextView) findViewById(R.id.txt_countdownInt)).setText("Winner");
                                } else if (scoreMaster < scoreClient) {
                                    ((TextView) findViewById(R.id.txt_countdownInt)).setText("Looser");
                                } else {
                                    ((TextView) findViewById(R.id.txt_countdownInt)).setText("Draw");
                                }
                            } else {
                                if (scoreMaster < scoreClient) {
                                    ((TextView) findViewById(R.id.txt_countdownInt)).setText("Winner");
                                } else if (scoreMaster > scoreClient) {
                                    ((TextView) findViewById(R.id.txt_countdownInt)).setText("Looser");
                                } else {
                                    ((TextView) findViewById(R.id.txt_countdownInt)).setText("Draw");
                                }
                            }
                            updateDataOnScreen();
                            break;
                        case "nextRound":
                            currentRound++;
                            roundWinner(data);
                            updateDataOnScreen();
                            break;
                    }
                }
                gameRound = null;
            }
        }
    }

    // Count the winner score
    private void roundWinner(Intent data) {
        String winner = data.getStringExtra(GameActivity.WINNER_KEY);
        if (winner.equals("master")) {
            scoreMaster++;
        } else if (winner.equals("client")) {
            scoreClient++;
        }
    }
}

