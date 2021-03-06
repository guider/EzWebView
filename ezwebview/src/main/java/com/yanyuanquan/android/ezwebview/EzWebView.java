package com.yanyuanquan.android.ezwebview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.net.http.SslError;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/**
 * Created by apple on 16/8/5.
 */

public class EzWebView extends FrameLayout implements View.OnClickListener {

    private WebView webView;
    private int progressWidth = 4;
    private int direction = 1;
    private int foregroundColor = 0xffff0000;
    private int backgroundColor = 0x00000000;
    private Paint progressPaint;
    private float currentProgress = 0;
    private View errorView;
    private boolean isError = false;

    public EzWebView(Context context) {
        this(context, null);
    }

    public EzWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public EzWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        webView = new WebView(getContext().getApplicationContext());
        this.addView(webView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        webView.setWebChromeClient(chromeClient);
        webView.setWebViewClient(client);
        initAttrs(attrs);
    }

    @SuppressWarnings("ResourceType")
    private void initAttrs(AttributeSet attrs) {
        TypedArray ta = getResources().obtainAttributes(attrs, R.styleable.EzWebView);

        try {
            int errorLayoutId = getErrorLayoutId() != 0 ? getErrorLayoutId()
                    : (ta.getResourceId(R.styleable.EzWebView_ErrorLayoutId, 0) != 0
                    ? (ta.getResourceId(R.styleable.EzWebView_ErrorLayoutId, 0)) : R.layout.empty_view);

            errorView = LayoutInflater.from(getContext()).inflate(errorLayoutId, null);
            errorView.setVisibility(GONE);
            this.addView(errorView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            foregroundColor = ta.getColor(R.styleable.EzWebView_ForegroundColor, foregroundColor);
            // TODO: 16/8/7  暂未绘制进度条背景
            backgroundColor = ta.getColor(R.styleable.EzWebView_BackgroundColor, backgroundColor);
            progressWidth = (int) ta.getDimension(R.styleable.EzWebView_ProgressWidth, progressWidth);
            direction = ta.getInt(R.styleable.EzWebView_Direction, 1);
            progressPaint = new Paint();
            progressPaint.setAntiAlias(true);
            progressPaint.setColor(foregroundColor);

        } finally {
            ta.recycle();
        }

        if (errorView != null && errorView.findViewById(R.id.retry) != null) {
            errorView.setOnClickListener(this);
        }
    }


    public void loadUrl(String url) {
        if (webView != null) {
            webView.loadUrl(url);
        }

    }

    WebChromeClient chromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress <= 100) {
                currentProgress = newProgress * 0.01f;
                Log.e("zjw", "  currentProgress   :  " + newProgress);
                invalidate(getReact());
            }
            if (newProgress >= 100 && isError) {
                showErrorView();
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }

    };

    WebViewClient client = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            currentProgress = 100;
            invalidate(getReact());
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    currentProgress = 0;
                    invalidate(getReact());
                }
            }, 200);


            if (isError) {
                showErrorView();
            } else {
                showSuccessView();
            }
            Log.e("zjw", "  onPageFinsh   ");
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.e("zjw", "  onPageStart   ");
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView v, String url) {
            v.loadUrl(url);
            showSuccessView();
            Log.e("zjw", "     shouldOverrideUrlLoading      ");
            return true;
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            isError = true;
            Log.e("zjw", "   error1   " + error.toString());
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            isError = true;
            Log.e("zjw", "   error 2  " + errorResponse.toString());

        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            Log.e("zjw", "   error3   " + error.toString());
            isError = true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            isError = true;
            Log.e("zjw", "   error4   " + errorCode);
        }


    };

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.save();
        drawPregress(canvas);
        canvas.restore();
    }


    private void showSuccessView() {
        Log.e("zjw", "   show success");
        isError = false;
        errorView.setVisibility(GONE);
        webView.setVisibility(VISIBLE);
    }

    @Override
    public void onClick(View v) {
        isError =false;
        reload();
    }

    private void showErrorView() {
        errorView.setVisibility(VISIBLE);
        webView.setVisibility(GONE);
    }

    private void drawPregress(Canvas canvas) {
        Rect rect = new Rect(getLX(), getLY(), getRX(), getRY());
        canvas.drawRect(rect, progressPaint);
    }

    private Rect getReact() {
//        Log.e("zjw", "FRX  :  " + getFRX());
//        Log.e("zjw", "FRY  :  " + getFRY());
        return new Rect(getLX(), getLY(), getFRX(), getFRY());
    }

    public int getRX() {
        return direction == 3 ? progressWidth : (int) (direction == 1 || direction == 2 ? getWidth() * currentProgress : getWidth());
    }

    public int getRY() {
        return direction == 3 || direction == 4 ? (int) (getHeight() * currentProgress) : (direction == 1 ? progressWidth : getHeight());
    }

    private int getLY() {
        return direction == 2 ? getHeight() - progressWidth : 0;
    }

    private int getLX() {
        return direction == 4 ? getWidth() - progressWidth : 0;
    }


    private int dp2px(int dip) {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        float density = dm.density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }


    public int getFRX() {
        return direction == 3 ? progressWidth : (direction == 1 || direction == 2 ? getWidth() : getWidth());
    }

    public int getFRY() {
        return direction == 3 || direction == 4 ? (getHeight()) : (direction == 1 ? progressWidth : getHeight());
    }

    public WebView getWebView() {
        return webView;
    }

    public void reload() {
        if (webView != null)
            webView.reload();
    }

    public void destory() {
        if (webView != null)
            webView.destroy();
    }

    public int getErrorLayoutId() {
        return 0;
    }
}
