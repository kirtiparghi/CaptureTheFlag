package com.kirtiparghi.capturetheflag;

import android.content.Context;
import android.webkit.WebView;

/**
 * Created by kirtiparghi on 4/10/18.
 */

public class GifWebView extends WebView {

    public GifWebView(Context context, String path) {
        super(context);

        loadUrl(path);
    }
}