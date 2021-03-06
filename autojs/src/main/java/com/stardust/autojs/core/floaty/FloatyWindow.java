package com.stardust.autojs.core.floaty;

import android.graphics.PixelFormat;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.stardust.autojs.R;
import com.stardust.enhancedfloaty.FloatyService;
import com.stardust.enhancedfloaty.ResizableFloaty;
import com.stardust.enhancedfloaty.ResizableFloatyWindow;
import com.stardust.enhancedfloaty.WindowBridge;
import com.stardust.enhancedfloaty.gesture.DragGesture;
import com.stardust.enhancedfloaty.gesture.ResizeGesture;

/**
 * Created by Stardust on 2017/12/5.
 */

public class FloatyWindow extends ResizableFloatyWindow {

    private final Object mLock = new Object();
    private boolean mCreated = false;
    private View mCloseButton;
    private static final String TAG = "ResizableFloatyWindow";
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private ViewGroup mWindowView;
    private View mRootView;
    private View mResizer;
    private View mMoveCursor;
    private WindowBridge mWindowBridge;
    private MyFloaty mFloaty;


    public FloatyWindow(View view) {
        this(new MyFloaty(view));
    }

    private FloatyWindow(MyFloaty floaty) {
        super(floaty);
        mFloaty = floaty;
    }

    public void waitFor() {
        synchronized (mLock) {
            if (mCreated) {
                return;
            }
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onCreate(FloatyService service, WindowManager manager) {
        this.mWindowManager = manager;
        this.mWindowLayoutParams = this.createWindowLayoutParams();
        if (this.mFloaty == null) {
            throw new IllegalStateException("Must start this service by static method ResizableExpandableFloatyWindow.startService");
        } else {
            this.initWindowView(service);
            this.mWindowBridge = new WindowBridge.DefaultImpl(this.mWindowLayoutParams, this.mWindowManager, this.mWindowView);
            this.initGesture();
        }
        synchronized (mLock) {
            mCreated = true;
            mLock.notify();
        }
    }

    public void setOnCloseButtonClickListener(View.OnClickListener listener) {
        mCloseButton.setOnClickListener(listener);
    }

    public void setAdjustEnabled(boolean enabled) {
        if (!enabled) {
            mMoveCursor.setVisibility(View.GONE);
            mResizer.setVisibility(View.GONE);
            mCloseButton.setVisibility(View.GONE);
        } else {
            mMoveCursor.setVisibility(View.VISIBLE);
            mResizer.setVisibility(View.VISIBLE);
            mCloseButton.setVisibility(View.VISIBLE);
        }
    }

    public boolean isAdjustEnabled() {
        return mMoveCursor.getVisibility() == View.VISIBLE;
    }

    public View getRootView() {
        return mRootView;
    }

    private void initWindowView(FloatyService service) {
        this.mWindowView = (ViewGroup) View.inflate(service, com.stardust.lib.R.layout.ef_floaty_container, (ViewGroup) null);
        this.mRootView = this.mFloaty.inflateView(service, this);
        this.mResizer = this.mFloaty.getResizerView(this.mRootView);
        this.mMoveCursor = this.mFloaty.getMoveCursorView(this.mRootView);
        this.mCloseButton = mRootView.findViewById(R.id.close);
        android.view.ViewGroup.LayoutParams params = new android.view.ViewGroup.LayoutParams(-2, -2);
        this.mWindowView.addView(this.mRootView, params);
        this.mWindowView.setFocusableInTouchMode(true);
        this.mWindowManager.addView(this.mWindowView, this.mWindowLayoutParams);
    }

    private WindowManager.LayoutParams createWindowLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        return layoutParams;
    }

    public void disableWindowFocus() {
        mWindowLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowManager.updateViewLayout(mWindowView, mWindowLayoutParams);
    }

    public void requestWindowFocus() {
        mWindowLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowManager.updateViewLayout(mWindowView, mWindowLayoutParams);
        mWindowView.requestFocus();
    }


    private void initGesture() {
        if (this.mResizer != null) {
            ResizeGesture.enableResize(this.mResizer, this.mRootView, this.mWindowBridge);
        }

        if (this.mMoveCursor != null) {
            DragGesture gesture = new DragGesture(this.mWindowBridge, this.mMoveCursor);
            gesture.setPressedAlpha(1.0F);
        }
    }

    public WindowBridge getWindowBridge() {
        return this.mWindowBridge;
    }

    public void onServiceDestroy(FloatyService service) {
        this.close();
    }

    public void close() {
        this.mWindowManager.removeView(this.mWindowView);
        FloatyService.removeWindow(this);
    }

    private static class MyFloaty implements ResizableFloaty {


        private View mContentView;
        private View mRootView;


        public MyFloaty(View view) {
            mContentView = view;
        }

        @Override
        public View inflateView(FloatyService floatyService, ResizableFloatyWindow resizableFloatyWindow) {
            mRootView = View.inflate(mContentView.getContext(), R.layout.floaty_window, null);
            ((FrameLayout) mRootView.findViewById(R.id.container)).addView(mContentView);
            return mRootView;
        }

        @Nullable
        @Override
        public View getResizerView(View view) {
            return view.findViewById(R.id.resizer);
        }

        @Nullable
        @Override
        public View getMoveCursorView(View view) {
            return view.findViewById(R.id.move_cursor);
        }
    }
}
