package com.google.systemprocess;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AAAA2", String.format("result=%d ordered=%b", getResultCode(), isOrderedBroadcast()));
        setResultCode(1111);
    }
}

