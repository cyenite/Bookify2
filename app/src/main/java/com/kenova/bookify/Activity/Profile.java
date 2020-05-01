package com.kenova.bookify.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.kenova.bookify.Model.ProfileModel.ProfileModel;
import com.kenova.bookify.Model.SuccessModel.SuccessModel;
import com.kenova.bookify.R;
import com.kenova.bookify.Utility.PrefManager;
import com.kenova.bookify.Webservice.AppAPI;
import com.kenova.bookify.Webservice.BaseURL;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends AppCompatActivity {

    ProgressDialog progressDialog;
    PrefManager prefManager;

    EditText txt_name, txt_email, txt_Contact, txt_password;
    TextView btn_update;
    RelativeLayout rl_adView;

    String str_name, str_email, str_mobile, str_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.darktheme);
        } else {
            setTheme(R.style.AppTheme);
            getSupportActionBar().hide();
        }
        setContentView(R.layout.profile);
        PrefManager.forceRTLIfSupported(getWindow(), Profile.this);
        prefManager = new PrefManager(Profile.this);
        progressDialog = new ProgressDialog(Profile.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Log.e("ids", "" + prefManager.getLoginId());
        rl_adView = findViewById(R.id.rl_adView);
        txt_name = findViewById(R.id.txt_name);
        txt_email = findViewById(R.id.txt_email);
        txt_password = findViewById(R.id.txt_password);
        btn_update = findViewById(R.id.btn_update);
        txt_Contact = findViewById(R.id.txt_Contact);

        Get_Profile();

        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            Admob();
            rl_adView.setVisibility(View.VISIBLE);
        } else {
            rl_adView.setVisibility(View.GONE);
        }

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                str_name = txt_name.getText().toString();
                str_email = txt_email.getText().toString();
                str_mobile = txt_Contact.getText().toString();
                str_password = txt_password.getText().toString();
                if (str_name.length() == 0) {
                    Toast.makeText(Profile.this, "Enter FullName", Toast.LENGTH_SHORT).show();
                    return;
                } else if (str_password.length() == 0) {
                    Toast.makeText(Profile.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                } else if (str_email.length() == 0) {
                    Toast.makeText(Profile.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                } else if (str_mobile.length() == 0) {
                    Toast.makeText(Profile.this, "Enter Mobile numer", Toast.LENGTH_SHORT).show();
                    return;
                }

                Update_Profile();
            }
        });


    }


    private void Update_Profile() {

        progressDialog.show();
        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<SuccessModel> call = bookNPlayAPI.update_profile("" + prefManager.getLoginId(),
                str_name, str_email, str_password, str_mobile);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                if (response.code() == 200) {
                    new AlertDialog.Builder(Profile.this)
                            .setTitle("" + getResources().getString(R.string.app_name))
                            .setMessage("" + response.body().getMessage())
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    startActivity(new Intent(Profile.this, MainActivity.class));

                                   /* Intent i = getBaseContext().getPackageManager()
                                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
                                    startActivity(i);
                                    finish();*/
                                    Toast.makeText(Profile.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).show();

                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    private void Get_Profile() {
        progressDialog.show();
        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<ProfileModel> call = bookNPlayAPI.profile("" + prefManager.getLoginId());
        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                if (response.code() == 200) {
                    txt_name.setText(response.body().getResult().get(0).getFullname());
                    txt_email.setText(response.body().getResult().get(0).getEmail());
                    txt_Contact.setText(response.body().getResult().get(0).getMobileNumber());
                    txt_password.setText(response.body().getResult().get(0).getPassword());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }


    public void Admob() {

        try {
            AdView mAdView = new AdView(Profile.this);
            mAdView.setAdSize(AdSize.SMART_BANNER);
            mAdView.setAdUnitId(prefManager.getValue("banner_adid"));
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                }

                @Override
                public void onAdClosed() {
//                    Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    Log.e("errorcode", "" + errorCode);
//                    Toast.makeText(getApplicationContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdLeftApplication() {
//                    Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                }
            });
            mAdView.loadAd(adRequest);

            ((RelativeLayout) rl_adView).addView(mAdView);
        } catch (Exception e) {
            Log.e("Exception=>", "" + e.getMessage());
        }
    }

}

