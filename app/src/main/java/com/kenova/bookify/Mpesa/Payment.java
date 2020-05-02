package com.kenova.bookify.Mpesa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.androidstudy.daraja.Daraja;
import com.androidstudy.daraja.DarajaListener;
import com.androidstudy.daraja.model.AccessToken;
import com.androidstudy.daraja.model.LNMExpress;
import com.androidstudy.daraja.model.LNMResult;
import com.androidstudy.daraja.util.TransactionType;
import com.kenova.bookify.Activity.AllPaymentActivity;
import com.kenova.bookify.R;


public class Payment extends AppCompatActivity implements View.OnClickListener {

    EditText mNumber;
    TextView mAmount, mTitle;
    Button mButton;
    ProgressBar mLoad;
    private Daraja daraja;
    int price;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stkpush);
        mNumber = findViewById(R.id.phone);
        mTitle = findViewById(R.id.BookTitle);
        mAmount = findViewById(R.id.BookPrice);
        mLoad = findViewById(R.id.progressBarStk);
        mButton = findViewById(R.id.btn_continue_stk);

        Intent intent = new Intent();
        price = intent.getIntExtra("price",0);
        title = intent.getStringExtra("title");

        mTitle.setText(title);
        mAmount.setText(price);

        mButton.setOnClickListener(this);
        daraja = Daraja.with("lpRnSFJyeMPnJ90yO0pOG8grwRuDm3il", "0OTXfudvr2xzLb1Y", new DarajaListener<AccessToken>() {
            @Override
            public void onResult(@NonNull AccessToken accessToken) {
                //Log.i(com.kenova.bookify.Mpesa.Payment.this.getClass().getSimpleName(), accessToken.getAccess_token());
            }

            @Override
            public void onError(String error) {
                //Log.e(com.kenova.bookify.Mpesa.Payment.this.getClass().getSimpleName(), error);

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == mButton) {
            String phonenumber = mNumber.getText().toString().trim(); //collect the number from the user's input
            String Amount = mAmount.getText().toString().trim(); // amount privided by the user

            //check validity of a number
            if (phonenumber.length() != 10) {
                mNumber.setError("Invalid number");
                return;
            }
            //check validity of a number
            else if (Integer.parseInt(Amount) <= 0) {
                mAmount.setError("Amount should be more than 0");
                return;
            }
            mLoad.setVisibility(View.VISIBLE);
            LNMExpress lnmExpress = new LNMExpress(
                    "174379",
                    "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919",  //https://developer.safaricom.co.ke/test_credentials
                    TransactionType.CustomerPayBillOnline,
                    Amount,
                    "0700000000",
                    "174379",
                    phonenumber,
                    "http://mycallbackurl.com/checkout.php",
                    "001ABC",
                    "Book Purchase from Bookify"
            );
            daraja.requestMPESAExpress(lnmExpress,
                    new DarajaListener<LNMResult>() {
                        @Override
                        public void onResult(@NonNull LNMResult lnmResult) {
                            Log.i(com.kenova.bookify.Mpesa.Payment.this.getClass().getSimpleName(), lnmResult.ResponseDescription);
                            AllPaymentActivity stkpayed = new AllPaymentActivity();
                            stkpayed.mpesaPayedSuccess();
                        }

                        @Override
                        public void onError(String error) {
                            Log.i(com.kenova.bookify.Mpesa.Payment.this.getClass().getSimpleName(), error);
                        }
                    }
            );
        }
    }
}