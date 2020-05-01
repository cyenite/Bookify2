package com.kenova.bookify.Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kenova.bookify.Adapter.AuthorAdapter;
import com.kenova.bookify.Model.AuthorModel.AuthorModel;
import com.kenova.bookify.R;
import com.kenova.bookify.Utility.PrefManager;
import com.kenova.bookify.Webservice.AppAPI;
import com.kenova.bookify.Webservice.BaseURL;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorAllActivity extends AppCompatActivity {

    PrefManager prefManager;
    ProgressDialog progressDialog;

    List<com.kenova.bookify.Model.AuthorModel.Result> AuthorList;
    RecyclerView rv_author;
    AuthorAdapter authorAdapter;
    TextView toolbar_title, txt_back;
    RelativeLayout rl_adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            //switch_theme.setChecked(true);
            setTheme(R.style.darktheme);
        } else {
            setTheme(R.style.AppTheme);
            getSupportActionBar().hide();
        }
        setContentView(R.layout.authoractivity);
        PrefManager.forceRTLIfSupported(getWindow(), AuthorAllActivity.this);
        rv_author = (RecyclerView) findViewById(R.id.rv_author);

        prefManager = new PrefManager(AuthorAllActivity.this);

        progressDialog = new ProgressDialog(AuthorAllActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        rl_adView = findViewById(R.id.rl_adView);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("" + getResources().getString(R.string.Authors));
        txt_back = (TextView) findViewById(R.id.txt_back);
        txt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        AuthorList();

        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            Admob();
            rl_adView.setVisibility(View.VISIBLE);
        } else {
            rl_adView.setVisibility(View.GONE);
        }

    }

    private void AuthorList() {
        progressDialog.show();
        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<AuthorModel> call = bookNPlayAPI.autherlist();
        call.enqueue(new Callback<AuthorModel>() {
            @Override
            public void onResponse(Call<AuthorModel> call, Response<AuthorModel> response) {
                if (response.code() == 200) {

                    AuthorList = new ArrayList<>();
                    AuthorList = response.body().getResult();
                    Log.e("AuthorList", "" + AuthorList.size());

                    authorAdapter = new AuthorAdapter(AuthorAllActivity.this, AuthorList);
                    rv_author.setHasFixedSize(true);
                    RecyclerView.LayoutManager mLayoutManager3 = new LinearLayoutManager(AuthorAllActivity.this,
                            LinearLayoutManager.HORIZONTAL, false);
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(AuthorAllActivity.this, 3, LinearLayoutManager.VERTICAL, false);
                    rv_author.setLayoutManager(gridLayoutManager);
                    rv_author.setItemAnimator(new DefaultItemAnimator());
                    rv_author.setAdapter(authorAdapter);
                    authorAdapter.notifyDataSetChanged();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<AuthorModel> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }


    public void Admob() {

        try {
            AdView mAdView = new AdView(AuthorAllActivity.this);
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

