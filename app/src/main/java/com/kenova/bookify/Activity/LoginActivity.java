package com.kenova.bookify.Activity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kenova.bookify.Model.LoginRegister.LoginRegiModel;
import com.kenova.bookify.R;
import com.kenova.bookify.Utility.PrefManager;
import com.kenova.bookify.Webservice.AppAPI;
import com.kenova.bookify.Webservice.BaseURL;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.squareup.picasso.Picasso.Priority.HIGH;

public class LoginActivity extends AppCompatActivity {

    EditText et_fullname, et_email, et_password, et_phone;
    String str_fullname, str_email, str_password, str_phone;

    TextView txt_already_signup, txt_login, txt_skip, txt_forgot;

    ProgressDialog progressDialog;
    private PrefManager prefManager;

    ImageView iv_login_icon;
    InterstitialAd interstitial;

    LoginButton loginButton;
    ImageView fb, btn_google;

    CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private static final String EMAIL = "email";
    private static final String PROFILE = "public_profile";

    String fb_name, fb_email;

    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton sign_in_button;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.loginactivity);

        PrefManager.forceRTLIfSupported(getWindow(), LoginActivity.this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();

        Init();

        Log.e("interstital_ad", "" + prefManager.getValue("interstital_ad"));
        if (prefManager.getValue("interstital_ad").equalsIgnoreCase("yes")) {
            rewardAds();
        }

        Picasso.with(LoginActivity.this).load(BaseURL.Image_URL + "" + prefManager.getValue("app_logo")).priority(HIGH).into(iv_login_icon);

        txt_already_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, Registration.class));
            }
        });

        txt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str_email = et_email.getText().toString();
                str_password = et_password.getText().toString();

                if (TextUtils.isEmpty(str_email)) {
                    Toast.makeText(LoginActivity.this, "Enter Email Address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(str_password)) {
                    Toast.makeText(LoginActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                SignIn();
            }
        });
        txt_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prefManager.getValue("interstital_ad").equalsIgnoreCase("yes")) {
                    if (interstitial.isLoaded()) {
                        interstitial.show();
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                } else {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
        txt_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(LoginActivity.this, ForgotActivity.class));
            }
        });

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(PROFILE, EMAIL));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("loginResult1", "Token::" + loginResult.getAccessToken());
                Log.e("loginResult", "" + loginResult.getAccessToken().getToken());
                AccessToken accessToken = loginResult.getAccessToken();
                Log.e("loginResult3", "" + accessToken);
                useLoginInformation(accessToken);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("exception", "" + error.getMessage());
            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            // This method is invoked everytime access token changes
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                useLoginInformation(currentAccessToken);
            }
        };

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        sign_in_button = (SignInButton) findViewById(R.id.sign_in_button);
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
            }
        });

    }


    public void Init() {
        prefManager = new PrefManager(this);
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        txt_already_signup = findViewById(R.id.txt_already_signup);
        txt_login = findViewById(R.id.txt_login);
        txt_skip = findViewById(R.id.txt_skip);
        txt_forgot = findViewById(R.id.txt_forgot);

        iv_login_icon = findViewById(R.id.iv_login_icon);

        fb = findViewById(R.id.fb);
        btn_google = findViewById(R.id.btn_google);

    }

    public void SignIn() {
        progressDialog.show();
        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<LoginRegiModel> call = bookNPlayAPI.login(str_email, str_password);
        call.enqueue(new Callback<LoginRegiModel>() {
            @Override
            public void onResponse(Call<LoginRegiModel> call, Response<LoginRegiModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200) {
                    if (response.body().getStatus() == 200) {
                        prefManager.setLoginId("" + response.body().getUserid());

                        if (prefManager.getValue("interstital_ad").equalsIgnoreCase("yes")) {
                            if (interstitial.isLoaded()) {
                                interstitial.show();
                            } else {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        } else {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    } else if (response.body().getStatus() == 400) {
                        Toast.makeText(LoginActivity.this, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginRegiModel> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rewardAds() {
        interstitial = new InterstitialAd(LoginActivity.this);
        interstitial.setAdUnitId(prefManager.getValue("interstital_adid"));
        interstitial.loadAd(new AdRequest.Builder().build());
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {

            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e("onAdFailedToLoad2=>", "" + errorCode);
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    public void onClick(View v) {
        if (v == btn_google) {
            Log.e("gmail", "perform");
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 101);
        }
    }

    public void onClickFacebookButton(View view) {
        if (view == fb) {
            loginButton.performClick();
        }
    }

    private void useLoginInformation(AccessToken accessToken) {

        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                if (object != null) {

                    fb_email = object.optString("email");
                    fb_name = object.optString("name");

                    if (fb_email.length() == 0) {
                        fb_email = fb_name.trim() + "@facebook.com";
                    }
                    Log.e("name", "" + fb_name);
                    Log.e("email", "" + fb_email);


                    AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
                    Call<LoginRegiModel> call = bookNPlayAPI.login_fb(fb_email);
                    call.enqueue(new Callback<LoginRegiModel>() {
                        @Override
                        public void onResponse(Call<LoginRegiModel> call, Response<LoginRegiModel> response) {

                            progressDialog.dismiss();
                            if (response.code() == 200) {
                                if (response.body().getStatus() == 200) {
                                    Log.e("loginid", "" + response.body().getUserid());
                                    Log.e("loginid", "" + response.body().getMessage());
                                    prefManager.setLoginId("" + response.body().getUserid());
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else if (response.body().getStatus() == 400) {
                                    Log.e("namesss", "" + fb_name);
                                    Log.e("namesss", "" + fb_email);
                                    SignUp(fb_name, fb_email);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginRegiModel> call, Throwable t) {
                            progressDialog.dismiss();
                            LoginManager.getInstance().logOut();
                            Toast.makeText(LoginActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
    }

    public void SignUp(String strN, String StrE) {
        progressDialog.show();
        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Log.e("namesss1", "" + strN);
        Log.e("namesss1", "" + StrE);
        Call<LoginRegiModel> call = bookNPlayAPI.registration_fb(strN, StrE);
        call.enqueue(new Callback<LoginRegiModel>() {
            @Override
            public void onResponse(Call<LoginRegiModel> call, Response<LoginRegiModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200) {
                    if (response.body().getStatus() == 200) {
                        prefManager.setLoginId("" + response.body().getUserid());
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                }

            }

            @Override
            public void onFailure(Call<LoginRegiModel> call, Throwable t) {
                progressDialog.dismiss();
                LoginManager.getInstance().logOut();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            Log.e("getDisplayName", "" + account.getDisplayName());
            Log.e("getEmail", "" + account.getEmail());
            Log.e("getIdToken", "" + account.getIdToken());
            Log.e("getPhotoUrl", "" + account.getPhotoUrl());

            progressDialog.show();

            AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
            Call<LoginRegiModel> call = bookNPlayAPI.login(account.getEmail(), "");
            call.enqueue(new Callback<LoginRegiModel>() {
                @Override
                public void onResponse(Call<LoginRegiModel> call, Response<LoginRegiModel> response) {
                    progressDialog.dismiss();
                    if (response.code() == 200) {
                        if (response.body().getStatus() == 200) {
                            prefManager.setLoginId("" + response.body().getUserid());
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else if (response.body().getStatus() == 400) {
                            SignUp(account.getDisplayName(), account.getEmail());
                        }
                    }
                }

                @Override
                public void onFailure(Call<LoginRegiModel> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (ApiException e) {
            Log.e("ApiException", "signInResult:failed code=" + e.getStatusCode());
        }
    }


}
