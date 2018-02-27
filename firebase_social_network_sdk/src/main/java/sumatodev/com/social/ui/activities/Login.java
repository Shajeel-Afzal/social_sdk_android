package sumatodev.com.social.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;

import sumatodev.com.social.BuildConfig;
import sumatodev.com.social.R;
import sumatodev.com.social.managers.DatabaseHelper;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.utils.GoogleApiHelper;
import sumatodev.com.social.utils.LogUtil;
import sumatodev.com.social.utils.LogoutHelper;
import sumatodev.com.social.utils.PreferencesUtil;

public class Login extends BaseActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 100;
    private static final String TAG = Login.class.getSimpleName();
    private Button login_button;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        findViews();

        // Configure Google Sign In
        mGoogleApiClient = GoogleApiHelper.createGoogleApiClient(this);
        // Configure firebase auth
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            LogoutHelper.signOut(mGoogleApiClient, this);
        }
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Profile is signed in
                    LogUtil.logDebug(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    checkIsProfileExist(user.getUid());
                } else {
                    // Profile is signed out
                    LogUtil.logDebug(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
    }


    private void findViews() {

        login_button = findViewById(R.id.login_button);
        login_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == login_button) {
            if (hasInternetConnection()) {
                onLoginClick();
            } else {
                showSnackBar(R.string.no_internet_connection);
            }
        }
    }

    private void onLoginClick() {

        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                new AuthUI.IdpConfig.PhoneBuilder().build()))
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }
        showSnackBar(R.string.unknown_error);
    }

    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == RESULT_OK) {
            if (response != null) {
                //successful
            }
        } else {
            if (response == null) {
                showSnackBar(R.string.sign_in_cancelled);
                return;
            }
            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackBar(R.string.no_internet_connection);
                return;
            }
            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackBar(R.string.unknown_error);
                return;
            }
        }
    }


    private void checkIsProfileExist(final String uid) {
        ProfileManager.getInstance(this).isProfileExist(uid, new OnObjectExistListener<Profile>() {
            @Override
            public void onDataChanged(boolean exist) {
                if (!exist) {
                    startCreateProfileActivity();
                } else {
                    PreferencesUtil.setProfileCreated(Login.this, true);
                    DatabaseHelper.getInstance(Login.this.getApplicationContext())
                            .addRegistrationToken(FirebaseInstanceId.getInstance().getToken(), uid);
                }
                hideProgress();
                finish();
            }
        });
    }

    private void startCreateProfileActivity() {
        Intent intent = new Intent(Login.this, CreateProfileActivity.class);
        //intent.putExtra(CreateProfileActivity.LARGE_IMAGE_URL_EXTRA_KEY, profilePhotoUrlLarge);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtil.logDebug(TAG, "onConnectionFailed:" + connectionResult);
        showSnackBar(R.string.error_google_play_services);
        hideProgress();
    }
}
