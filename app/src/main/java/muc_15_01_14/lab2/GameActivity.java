package muc_15_01_14.lab2;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import de.dfki.ccaal.gestures.Distribution;
import de.dfki.ccaal.gestures.IGestureRecognitionListener;
import de.dfki.ccaal.gestures.IGestureRecognitionService;


public class GameActivity extends ActionBarActivity {

    public static final String STATUS_KEY = "returnstatus";
    public static final String WINNER_KEY = "roundwinner";

    private CountDownTimer countDownTimer;
    private boolean master;
    private String chooseGesture;
    private InputStream in = null;
    private OutputStream out = null;

    // Create gestureListener
    IBinder mGestureListenerStub =
            new IGestureRecognitionListener.Stub() {
                @Override
                public void onGestureRecognized(Distribution distr) {
                    final String gesture = distr.getBestMatch();
                    final double distance = distr.getBestDistance();
                    Log.i("Gesture Recognized", gesture + " " + String.valueOf(distance));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            analyseGesture(gesture);
                        }
                    });

                }

                @Override
                public void onGestureLearned(String gestureName) throws RemoteException {
                    Log.i("Gesture Learned", gestureName);
                }

                @Override
                public void onTrainingSetDeleted(String trainingSet) throws RemoteException {
                    Log.i("Training Set Deleted", trainingSet);
                }
            };

    private IGestureRecognitionService mRecService;

    // Create a service connection to the recognition service
    private final ServiceConnection mGestureConn = new ServiceConnection() {
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

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try {
            in = MainActivity.mSocket.getInputStream();
            out = MainActivity.mSocket.getOutputStream();
            if(in == null || out == null){
                finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("BLUETOOTH","isConnected: "+MainActivity.mSocket.isConnected());
        // Get data from previous activity
        if (getIntent().hasExtra(MainActivity.MASTER_KEY)) {
            master = getIntent().getBooleanExtra(MainActivity.MASTER_KEY, false);
        }


        // Set GUI objects invisivle
        ((TextView) findViewById(R.id.txt_timeDifference)).setText("");
        ((Button) findViewById(R.id.btn_finishGame)).setVisibility(View.INVISIBLE);
        // ((Button) findViewById(R.id.btn_finishGame)).setEnabled(false);
        ((Button) findViewById(R.id.btn_nextRound)).setVisibility(View.INVISIBLE);
        // ((Button) findViewById(R.id.btn_nextRound)).setEnabled(false);


        // Start random countdown
        if (master) {
            int millisec = (int) Math.ceil(Math.random() * 4 + 2);
            String s = "time:"+String.valueOf(millisec);
            try {
                out.write(Byte.parseByte(s));
                out.flush();
                Log.i("BLUETOOTH", "bytes written: " + s);
                Countdown(millisec);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{

            // when client connected
            // ...
            // TODO read from inputstream

            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = in.read(buffer);
                    // Send the obtained bytes to the UI activity
                    /*mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();*/
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterGestureDetection();

    }

    // Countdown before executing the gesture
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

    // Start the gesture Listener and choose Gesture
    private void startGame() {
        ((ImageView) findViewById(R.id.img_gesture)).setImageResource(chooseGesture());
        // TODO commit chosen gesture to client


        Intent gestureBindIntent;
        Log.i("Version",Integer.toString(Build.VERSION.SDK_INT));
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP){
            //implicit intent ( < Android 5.0 )
           gestureBindIntent = new Intent("de.dfki.ccaal.gestures.GESTURE_RECOGNIZER");
        }else{
            //explicit intent ( > Android 5.0)
            gestureBindIntent = createExplicitFromImplicitIntent(this,new Intent("de.dfki.ccaal.gestures.GESTURE_RECOGNIZER"));
        }
            bindService(gestureBindIntent, mGestureConn, Context.BIND_AUTO_CREATE);
    }


    // Randomly choose a gesture
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

    // Button listener for start next round
    public void click_nextRound(View view) {

        getIntent().putExtra(STATUS_KEY, "nextRound");
        getIntent().putExtra(WINNER_KEY, "master");
        setResult(Activity.RESULT_OK, getIntent());
        this.finish();
    }

    // Button listener for finish the game
    public void click_finishGame(View view) {

        getIntent().putExtra(STATUS_KEY, "finishGame");
        getIntent().putExtra(WINNER_KEY, "master");
        setResult(Activity.RESULT_OK, getIntent());
        this.finish();
    }

    // Key listener for Back-Button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            getIntent().putExtra(STATUS_KEY, "exitRound");
            setResult(Activity.RESULT_OK, getIntent());
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    // Analyse incoming gesture whether it is correct or not
    private void analyseGesture(String gesture) {
        if (gesture.equals(chooseGesture)) {
            ((TextView) findViewById(R.id.txt_result)).setText("Wait...");
            ((ImageView) findViewById(R.id.img_feedback)).setImageResource(R.mipmap.succeed);
            if (master) {
                ((Button) findViewById(R.id.btn_finishGame)).setVisibility(View.VISIBLE);
                ((Button) findViewById(R.id.btn_nextRound)).setVisibility(View.VISIBLE);
                // TODO wait for client until response
            }
            unregisterGestureDetection();
        } else {
            ((ImageView) findViewById(R.id.img_feedback)).setImageResource(R.mipmap.failed);
            ((TextView) findViewById(R.id.txt_result)).setText("Try Again");
        }
    }

    // Unregister gesture detection
    private void unregisterGestureDetection() {
        try {
            if (mRecService != null) {
                mRecService.unregisterListener(IGestureRecognitionListener.
                        Stub.asInterface(mGestureListenerStub));
            }

            mRecService = null;
            unbindService(mGestureConn);
        } catch (android.os.RemoteException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    // Change implicit Intent to explicit Intent
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        //Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        //Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        //Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        //Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        //Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}
