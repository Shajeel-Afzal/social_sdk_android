/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package sumatodev.com.social.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import sumatodev.com.social.ApplicationHelper;
import sumatodev.com.social.R;
import sumatodev.com.social.enums.ProfileStatus;

/**
 * Created by alexey on 05.12.16.
 */

public class BaseActivity extends AppCompatActivity {

    public ProgressDialog progressDialog;
    public ActionBar actionBar;
    private boolean mIsActive = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
    }

    protected boolean isActive() {
        return mIsActive;
    }

    public String getCurrentUser(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser!=null){
            return firebaseUser.getUid();
        }else {
            return null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsActive = false;
    }

    public void doAuthorization(ProfileStatus status) {
        if (status.equals(ProfileStatus.NOT_AUTHORIZED) || status.equals(ProfileStatus.NO_PROFILE)) {
            startLoginActivity();
        }
    }

    private void startLoginActivity() {
        if (ApplicationHelper.mLoginRequiredListener != null) {
            Toast.makeText(this, R.string.login_required_for_this_action, Toast.LENGTH_SHORT).show();
            ApplicationHelper.mLoginRequiredListener.loginRequired();
        }
    }

    public void showProgress() {
        showProgress(R.string.loading);
    }

    public void openYoutubeLink(String link){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(intent);
    }

    public void showProgress(int message) {
        hideProgress();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(message));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void showSnackBar(int messageId) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                messageId, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void showSnackBar(View view, int messageId) {
        Snackbar snackbar = Snackbar.make(view, messageId, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
    public void openUrlActivity(String linkUrl) {
        Intent intent = new Intent(BaseActivity.this, LinkActivity.class);
        intent.putExtra(LinkActivity.URL_REF, linkUrl);
        startActivity(intent);
    }


    public void showWarningDialog(int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.button_ok, null);
        builder.show();
    }

    public void showWarningDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.button_ok, null);
        builder.show();
    }

    public boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public boolean checkInternetConnection() {
        boolean hasInternetConnection = hasInternetConnection();
        if (!hasInternetConnection) {
            showSnackBar(R.string.internet_connection_failed);
        }

        return hasInternetConnection;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
