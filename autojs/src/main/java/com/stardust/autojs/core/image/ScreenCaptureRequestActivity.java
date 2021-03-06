package com.stardust.autojs.core.image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.stardust.app.OnActivityResultDelegate;

/**
 * Created by Stardust on 2017/5/22.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenCaptureRequestActivity extends Activity {


    private static ScreenCaptureRequester.Callback sCallback;

    public static void request(Context context, ScreenCaptureRequester.Callback callback) {
        if (sCallback != null) {
            return;
        }
        sCallback = callback;
        context.startActivity(new Intent(context, ScreenCaptureRequestActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private OnActivityResultDelegate.Mediator mOnActivityResultDelegateMediator = new OnActivityResultDelegate.Mediator();
    private ScreenCaptureRequester mScreenCaptureRequester;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScreenCaptureRequester = new ScreenCaptureRequester.ActivityScreenCaptureRequester(mOnActivityResultDelegateMediator, this);
        mScreenCaptureRequester.setOnActivityResultCallback(sCallback);
        mScreenCaptureRequester.request();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScreenCaptureRequester == null)
            return;
        mScreenCaptureRequester.cancel();
        mScreenCaptureRequester = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mOnActivityResultDelegateMediator.onActivityResult(requestCode, resultCode, data);
        sCallback = null;
        finish();
    }

}
