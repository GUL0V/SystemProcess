package com.google.systemprocess;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button button;
    boolean a = false;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (a) {
                    startService();
                    a = false;
                } else {
                    stopService();
                    a = true;
                }
            }
        });
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.putExtra("inputExtra", "Mama ya v dubaye");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
    }

    public abstract static class PhonecallReceiver extends BroadcastReceiver {

        //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations


        private static int lastState = TelephonyManager.CALL_STATE_IDLE;
        private static Date callStartTime;
        private static boolean isIncoming;
        private static String savedNumber;  //because the passed incoming is only valid in ringing


        @Override
        public void onReceive(Context context, Intent intent) {

            //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            }
            else{
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                int state = 0;
                if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                    state = TelephonyManager.CALL_STATE_IDLE;
                }
                else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                }
                else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                    state = TelephonyManager.CALL_STATE_RINGING;
                }


                onCallStateChanged(context, state, number);
            }
        }

        //Derived classes should override these to respond to specific events of interest
        protected abstract void onIncomingCallReceived(Context ctx, String number, Date start);
        protected abstract void onIncomingCallAnswered(Context ctx, String number, Date start);
        protected abstract void onIncomingCallEnded(Context ctx, String number, Date start, Date end);

        protected abstract void onOutgoingCallStarted(Context ctx, String number, Date start);
        protected abstract void onOutgoingCallEnded(Context ctx, String number, Date start, Date end);

        protected abstract void onMissedCall(Context ctx, String number, Date start);

        //Deals with actual events

        //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
        //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
        public void onCallStateChanged(Context context, int state, String number) {
            if(lastState == state){
                Log.d("TAGusiloj", "onCallStateChanged2: "+number+"  "+state);
                if (state==1){
                    savedNumber = number;
                    onIncomingCallReceived(context, number, callStartTime);
                }
                return;
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    isIncoming = true;
                    callStartTime = new Date();
                    savedNumber = number;
                    Log.d("TAGusilo", "onCallStateChanged: "+number);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                    if(lastState != TelephonyManager.CALL_STATE_RINGING){
                        isIncoming = false;
                        callStartTime = new Date();
                        onOutgoingCallStarted(context, savedNumber, callStartTime);
                    }
                    else
                    {
                        isIncoming = true;
                        callStartTime = new Date();
                        onIncomingCallAnswered(context, savedNumber, callStartTime);
                    }

                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    if(lastState == TelephonyManager.CALL_STATE_RINGING){
                        //Ring but no pickup-  a miss
                        onMissedCall(context, savedNumber, callStartTime);
                    }
                    else if(isIncoming){
                        onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                    }
                    else{
                        onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                    }
                    break;
            }
            lastState = state;
        }
    }


}