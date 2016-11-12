package com.example.shahzaib.social_integration;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.concurrent.Callable;
public class MainActivity extends AppCompatActivity  implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 007;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    private SignInButton btnSignIn;
    private Button btnSignOut, btnRevokeAccess;
    private LinearLayout llProfileLayout;
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail;


    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "ViCGpRy4eihjNtHSPVHOM7Jo5";
    private static final String TWITTER_SECRET = "4ci6WyW0Gfk1eAcKjLrgZlEVpEB0mPSsBpJEWUAok8QE22OL7H";

    TwitterLoginButton twitterLoginButton;

    private CallbackManager mFacebookCallbackManager;


    protected void onCreate(Bundle savedInstanceState) {
        // (...)
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        btnSignIn = (SignInButton) findViewById( R.id.btn_sign_in );
        btnSignOut = (Button) findViewById( R.id.btn_sign_out );
        btnRevokeAccess = (Button) findViewById( R.id.btn_revoke_access );
        llProfileLayout = (LinearLayout) findViewById( R.id.llProfile );
        imgProfilePic = (ImageView) findViewById( R.id.imgProfilePic );
        txtName = (TextView) findViewById( R.id.txtName );
        txtEmail = (TextView) findViewById( R.id.txtEmail );

        btnSignIn.setOnClickListener( this );
        btnSignOut.setOnClickListener( this );
        btnRevokeAccess.setOnClickListener( this );

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder( GoogleSignInOptions.DEFAULT_SIGN_IN )
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .enableAutoManage( this, this )
                .addApi( Auth.GOOGLE_SIGN_IN_API, gso )
                .build();

        // Customizing G+ button
        btnSignIn.setSize( SignInButton.SIZE_STANDARD );
        btnSignIn.setScopes( gso.getScopeArray() );
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent( mGoogleApiClient );
        startActivityForResult( signInIntent, RC_SIGN_IN );
    }


    private void signOut() {
        Auth.GoogleSignInApi.signOut( mGoogleApiClient ).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI( false );
                    }
                } );
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess( mGoogleApiClient ).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI( false );
                    }
                } );
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d( TAG, "handleSignInResult:" + result.isSuccess() );
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.e( TAG, "display name: " + acct.getDisplayName() );

            String personName = acct.getDisplayName();
            String personPhotoUrl = acct.getPhotoUrl().toString();
            String email = acct.getEmail();

            Log.e( TAG, "Name: " + personName + ", email: " + email
                    + ", Image: " + personPhotoUrl );

            txtName.setText( personName );
            txtEmail.setText( email );
            Glide.with( getApplicationContext() ).load( personPhotoUrl )
                    .thumbnail( 0.5f )
                    .crossFade()
                    .diskCacheStrategy( DiskCacheStrategy.ALL )
                    .into( imgProfilePic );

            updateUI( true );
        } else {
            // Signed out, show unauthenticated UI.
            updateUI( false );
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btn_sign_in:
                signIn();
                break;

            case R.id.btn_sign_out:
                signOut();
                break;

            case R.id.btn_revoke_access:
                revokeAccess();
                break;
        }


        twitterLoginButton = (TwitterLoginButton) findViewById( R.id.twitter_login_button );
        twitterLoginButton.setCallback( new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                String UserName = result.data.getUserName();
                Toast.makeText( MainActivity.this, UserName, Toast.LENGTH_LONG ).show();
            }

            @Override
            public void failure(TwitterException e) {

            }
        } );


        FacebookCallback<LoginResult> mCallBack = new FacebookCallback<LoginResult>() {


            @Override
            public void onSuccess(LoginResult loginResult) {
                handleSignInResult( new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        LoginManager.getInstance().logOut();
                        return null;
                    }

                } );
            }

            private void handleSignInResult(Callable<Void> callable) {

            }


            @Override
            public void onCancel() {
                handleSignInResult( null );
            }

            @Override
            public void onError(FacebookException error) {
                Log.d( MainActivity.class.getCanonicalName(), error.getMessage() );
                handleSignInResult( null );
            }
        };
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        // (...)

        mFacebookCallbackManager.onActivityResult( requestCode, resultCode, data );
        twitterLoginButton.onActivityResult( requestCode, resultCode, data );
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }


    }
    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnRevokeAccess.setVisibility(View.VISIBLE);
            llProfileLayout.setVisibility(View.VISIBLE);
        } else {
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevokeAccess.setVisibility(View.GONE);
            llProfileLayout.setVisibility(View.GONE);
        }
    }


}























































