package muc_15_01_14.lab2;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import de.dfki.ccaal.gestures.Distribution;
import de.dfki.ccaal.gestures.IGestureRecognitionListener;
import de.dfki.ccaal.gestures.IGestureRecognitionService;


public class GameActivity extends ActionBarActivity {

    public static final String STATUS_KEY = "returnstatus";
    public static final String WINNER_KEY = "roundwinner";

    private CountDownTimer countDownTimer;
    de.dfki.ccaal.gestures.IGestureRecognitionService mRecService;
    private boolean master;
    private String chooseGesture;

    //create gestureListener
    IBinder mGestureListenerStub =
            new IGestureRecognitionListener.Stub() {
                @Override
                public void onGestureRecognized(Distribution distr) {
                    final String gesture = distr.getBestMatch();
                    final double distance = distr.getBestDistance();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("Identified Gesture", gesture + " " + String.valueOf(distance));
                            if (gesture.equals(chooseGesture)) {
                                ((TextView) findViewById(R.id.txt_countdownInt)).setText("Wait...");
                                ((ImageView) findViewById(R.id.img_feedback)).setImageResource(R.mipmap.succeed);
                                if (master) {
                                    ((Button) findViewById(R.id.btn_finishGame)).setVisibility(View.VISIBLE);
                                    ((Button) findViewById(R.id.btn_nextRound)).setVisibility(View.VISIBLE);
                                }
                                unregisterGestureDetection();
                            } else {
                                ((ImageView) findViewById(R.id.img_feedback)).setImageResource(R.mipmap.failed);
                            }

                        }
                    });

                }

                @Override
                public void onGestureLearned(String gestureName) throws RemoteException {

                }

                @Override
                public void onTrainingSetDeleted(String trainingSet) throws RemoteException {

                }
            };

    //create a service connection to the recognition service
    private ServiceConnection mGestureConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mRecService =
                    IGestureRecognitionService.Stub.asInterface(service);
            try {
                mRecService.registerListener(
                        IGestureRecognitionListener.Stub.asInterface(
                                mGestureListenerStub));
                mRecService.startClassificationMode("muc");
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (getIntent().hasExtra(MainActivity.MASTER_KEY)) {
            master = getIntent().getBooleanExtra(MainActivity.MASTER_KEY, false);
        }

        ((TextView) findViewById(R.id.txt_timeDifference)).setText("");
        ((Button) findViewById(R.id.btn_finishGame)).setVisibility(View.INVISIBLE);
        // ((Button) findViewById(R.id.btn_finishGame)).setEnabled(false);
        ((Button) findViewById(R.id.btn_nextRound)).setVisibility(View.INVISIBLE);
        // ((Button) findViewById(R.id.btn_nextRound)).setEnabled(false);


        Countdown((int) Math.ceil(Math.random() * 9+1));
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterGestureDetection();

    }

    private void Countdown(final int seconds) {
        ((TextView) findViewById(R.id.txt_result)).setText(Integer.toString(seconds));
        countDownTimer = new CountDownTimer(seconds * 1000, 100) {
            @Override
            public void onTick(long l) {
                ((TextView) findViewById(R.id.txt_result)).setText(Integer.toString((int) Math.ceil(l / 1000f)));
            }

            @Override
            public void onFinish() {
                ((TextView) findViewById(R.id.txt_result)).setText("Go");
                startGame();
            }
        };
        countDownTimer.start();
    }

    private void startGame() {
        ((ImageView) findViewById(R.id.img_gesture)).setImageResource(chooseGesture());

        //implicit intent ( < Android 5.0 )
        //Intent gestureBindIntent = new Intent("de.dfki.ccaal.gestures.GESTURE_RECOGNIZER");

        //explicit intent ( > Android 5.0)
        Intent gestureBindIntent = new Intent(this, IGestureRecognitionService.class);
        bindService(gestureBindIntent, mGestureConn, Context.BIND_AUTO_CREATE);
        Log.i("GameActivity","Run Gesturedetector");
    }


    private int chooseGesture() {
        int gesture = (int) Math.ceil(Math.random() * 7);
        switch (gesture) {
            case 0:
                chooseGesture = "square_angle";
                return R.mipmap.square_angle;
            case 1:
                chooseGesture = "square";
                return R.mipmap.square;
            case 2:
                chooseGesture = "right";
                return R.mipmap.right;
            case 3:
                chooseGesture = "left";
                return R.mipmap.left;
            case 4:
                chooseGesture = "up";
                return R.mipmap.up;
            case 5:
                chooseGesture = "down";
                return R.mipmap.down;
            case 6:
                chooseGesture = "circle_right";
                return R.mipmap.circle_right;
            default:
                chooseGesture = "circle_left";
                return R.mipmap.circle_left;
        }

    }

    public void click_nextRound(View view) {

        getIntent().putExtra(STATUS_KEY, "nextRound");
        getIntent().putExtra(WINNER_KEY, "master");
        setResult(Activity.RESULT_OK, getIntent());
        this.finish();
    }

    public void click_finishGame(View view) {

        getIntent().putExtra(STATUS_KEY, "finishGame");
        getIntent().putExtra(WINNER_KEY, "master");
        setResult(Activity.RESULT_OK, getIntent());
        this.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            getIntent().putExtra(STATUS_KEY, "exitRound");
            setResult(Activity.RESULT_OK, getIntent());
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }






   private void  unregisterGestureDetection(){
        try {
            if (mRecService != null) {
                mRecService.unregisterListener(IGestureRecognitionListener.
                        Stub.asInterface(mGestureListenerStub));
            }

        mRecService = null;unbindService(mGestureConn);
        } catch (android.os.RemoteException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
