package muc_15_01_14.lab2;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;


public class GameOverviewActivity extends ActionBarActivity {

    private Intent gameRound;
    private boolean master;
    private boolean gameFinished;

    private int scoreMaster, scoreClient, currentRound;
    private String nameMaster, nameClient;

    private CountDownTimer countDownTimer;

    public static int REQUEST_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameoverview);
        if(getIntent().hasExtra(MainActivity.MASTER_KEY)){
           master = getIntent().getBooleanExtra(MainActivity.MASTER_KEY,false);
        }
        if(getIntent().hasExtra(MainActivity.MASTER_USER_KEY)){
            nameMaster =  getIntent().getStringExtra(MainActivity.MASTER_USER_KEY);
        }
        if(getIntent().hasExtra(MainActivity.CLIENT_USER_KEY)){
            nameClient =  getIntent().getStringExtra(MainActivity.CLIENT_USER_KEY);
        }
        updateDataOnScreen();
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.i("GameOverviewActivity", "Resume");
        if(!gameFinished) {
            ((TextView) findViewById(R.id.txt_countdownInt)).setText(Integer.toString(3));
            Countdown(3);
        }
    }


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

    public void startRound() {
        gameRound = new Intent(getApplicationContext(), GameActivity.class);
        gameRound.putExtra(MainActivity.MASTER_KEY,master);
        startActivityForResult(gameRound, REQUEST_ID);
    }


    public void click_leaveGame(View view) {
        leaveGame();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            leaveGame();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void leaveGame() {
        if(countDownTimer!=null){
            countDownTimer.cancel();
            countDownTimer=null;
        }
        this.finish();

    }

    private void updateDataOnScreen() {
        ((TextView) findViewById(R.id.txt_nameMaster)).setText(nameMaster);
        ((TextView) findViewById(R.id.txt_nameClient)).setText(nameClient);

        ((TextView) findViewById(R.id.txt_roundInt)).setText(Integer.toString(currentRound));
        ((TextView) findViewById(R.id.txt_scoreMaster)).setText(Integer.toString(scoreMaster));
        ((TextView) findViewById(R.id.txt_scoreClient)).setText(Integer.toString(scoreClient));
    }


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
                            if(master){
                                if(scoreMaster>scoreClient){
                                    ((TextView) findViewById(R.id.txt_countdownInt)).setText("Winner");
                                }else  if(scoreMaster<scoreClient){
                                    ((TextView) findViewById(R.id.txt_countdownInt)).setText("Looser");
                                }else{
                                    ((TextView) findViewById(R.id.txt_countdownInt)).setText("Draw");
                                }
                            }else{
                                if(scoreMaster<scoreClient){
                                    ((TextView) findViewById(R.id.txt_countdownInt)).setText("Winner");
                                }else  if(scoreMaster>scoreClient){
                                    ((TextView) findViewById(R.id.txt_countdownInt)).setText("Looser");
                                }else{
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

    private void roundWinner(Intent data){
        String winner = data.getStringExtra(GameActivity.WINNER_KEY);
        if (winner.equals("master")) {
            scoreMaster++;
        } else if (winner.equals("client")) {
            scoreClient++;
        }
    }
}

