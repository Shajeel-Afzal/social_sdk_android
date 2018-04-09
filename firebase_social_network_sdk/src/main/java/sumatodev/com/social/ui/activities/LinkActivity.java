package sumatodev.com.social.ui.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import sumatodev.com.social.R;

public class LinkActivity extends BaseActivity {

    public static final String URL_REF = "LinkActivity.Url";
    private ProgressBar superProgressBar;
    private WebView superWebView;
    private String urlLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        superProgressBar = findViewById(R.id.myProgressBar);
        superWebView = findViewById(R.id.myWebView);
        superProgressBar.setMax(100);

        if (getIntent() != null) {
            urlLink = getIntent().getStringExtra(URL_REF);
            if (!urlLink.startsWith("http://") && !urlLink.startsWith("https://")) {
                urlLink = "https://" + urlLink;
                initWebView(urlLink);
            } else {
                initWebView(urlLink);
            }

            if (actionBar != null) {
                actionBar.setTitle(urlLink);
            }
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(String urlLink) {
        superWebView.loadUrl(urlLink);
        superWebView.getSettings().setJavaScriptEnabled(true);
        superWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                superProgressBar.setVisibility(View.GONE);
            }
        });
        superWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                superProgressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (actionBar != null) {
                    actionBar.setTitle(title);
                }
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);

            }
        });

        superWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                        long contentLength) {
                Uri myUri = Uri.parse(url);
                Intent superIntent = new Intent(Intent.ACTION_VIEW);
                superIntent.setData(myUri);
                startActivity(superIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.link_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.actionBack) {
            onBackPressed();
            return true;
        } else if (id == R.id.actionForward) {
            GoForward();
            return true;
        } else if (id == R.id.openExternal) {
            if (urlLink != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlLink));
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(menuItem);

    }

    private void GoForward() {
        if (superWebView.canGoForward()) {
            superWebView.goForward();
        } else {
            Toast.makeText(this, "Can't go further!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (superWebView.canGoBack()) {
            superWebView.goBack();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Exit");
            dialog.setMessage("Browser has nothing to go back, so what next?");
            dialog.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.setCancelable(false);
            dialog.setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();

        }
    }
}
