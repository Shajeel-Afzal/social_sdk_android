package com.sumatodev.firebasesocial;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;

import sumatodev.com.social.managers.DatabaseHelper;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.ui.activities.BaseActivity;
import sumatodev.com.social.ui.activities.CreateProfileActivity;
import sumatodev.com.social.ui.activities.Login;
import sumatodev.com.social.utils.LogUtil;
import sumatodev.com.social.utils.PreferencesUtil;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
