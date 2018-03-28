package com.sumatodev.firebasesocial;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;

import sumatodev.com.social.managers.DatabaseHelper;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.model.Profile;
import sumatodev.com.social.ui.activities.CreateProfileActivity;
import sumatodev.com.social.utils.PreferencesUtil;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;
    private static final String TAG = "SignInActivity";
    private ProgressDialog mProgressDialog;

    public static void start(Context context) {
        Intent starter = new Intent(context, SignInActivity.class);
        starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
    }

    private void checkIsProfileExist(final String userId) {
        ProfileManager.getInstance(this).isProfileExist(userId, new OnObjectExistListener<Profile>() {
            @Override
            public void onDataChanged(boolean exist) {
                if (!exist) {
                    startCreateProfileActivity();
                } else {
                    PreferencesUtil.setProfileCreated(SignInActivity.this, true);
                    sumatodev.com.social.ui.activities.MainActivity.start(SignInActivity.this);
                    DatabaseHelper.getInstance(SignInActivity.this.getApplicationContext())
                            .addRegistrationToken(FirebaseInstanceId.getInstance().getToken(), userId);
                }
                hideProgress();
                finish();
            }
        });
    }

    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void startCreateProfileActivity() {
        Intent intent = new Intent(SignInActivity.this, CreateProfileActivity.class);
        intent.putExtra(CreateProfileActivity.LARGE_IMAGE_URL_EXTRA_KEY, FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl());
        startActivity(intent);
    }

    public void loginLaterClick(View view) {

    }

    public void loginNowClick(View view) {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false, true)
                        .setPrivacyPolicyUrl("https://www.psl99.com/privacy-policy/")
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build())
                        ).build(),
                RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            super.onActivityResult(requestCode, resultCode, data);
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {

                checkIsProfileExist(FirebaseAuth.getInstance().getUid());

                return;
            } else {
                int errorCode = response.getErrorCode();

                switch (errorCode) {
                    case ErrorCodes.NO_NETWORK:
                        Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                        break;
                    case ErrorCodes.UNKNOWN_ERROR:
                        Toast.makeText(this, "Unknow Error", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(this, "Unknow Error", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            Toast.makeText(this, "Unknow Error SignIn Response!", Toast.LENGTH_SHORT).show();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

//    public void saveUserInformation() {
//        if (mProgressDialog != null && !mProgressDialog.isShowing()) {
//            mProgressDialog = ProgressDialog.show(this, getString(R.string.authenticating), getString(R.string.please_wait_for_a_moment));
//            mProgressDialog.show();
//        }
//
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        final UserModel userModel = new UserModel();
//        if (firebaseUser != null) {
//            userModel.setEmail(firebaseUser.getEmail());
//            userModel.setName(firebaseUser.getDisplayName());
//            userModel.setUid(firebaseUser.getUid());
//
//            if (!firebaseUser.isAnonymous()) {
//                Uri photUrl = firebaseUser.getProviderData().get(1).getPhotoUrl();
//                if (photUrl != null) {
//                    try {
//                        userModel.setProfileImageLink(firebaseUser.getProviderData().get(1).getPhotoUrl().toString());
//                    } catch (NullPointerException ignored) {
//
//                    }
//                }
//
//                userModel.setProvider(firebaseUser.getProviderData().get(1).getProviderId());
//            } else {
//                userModel.setProvider(CommonConsts.ANONYMOUS_PROVIDER);
//            }
//
//            HashMap<String, Object> userTaskMap = (HashMap<String, Object>) new ObjectMapper().convertValue(userModel, Map.class);
//
//            // INSERT USER IN THE DATABASE
//            FirebaseDatabase.getInstance()
//                    .getReference()
//                    .child(CommonConsts.FIREBASE_LOCATION_USERS)
//                    .child(firebaseUser.getUid())
//                    .updateChildren(userTaskMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//
//                    if (mProgressDialog != null)
//                        mProgressDialog.dismiss();
//
//                    if (task.isSuccessful()) {
//                        MainActivity.start(SignInActivity.this);
//                        finish();
//                    } else {
//                        MainActivity.start(SignInActivity.this);
//                        finish();
//                    }
//                }
//            });
//        } else {
//            mProgressDialog.dismiss();
//            MainActivity.start(this);
//            finish();
//        }
//    }


}