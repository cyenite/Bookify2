package com.divinetechs.ebooksapp.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.divinetechs.ebooksapp.Model.SuccessModel.SuccessModel;
import com.divinetechs.ebooksapp.Mpesa.Payment;
import com.divinetechs.ebooksapp.R;
import com.divinetechs.ebooksapp.Utility.PrefManager;
import com.divinetechs.ebooksapp.Webservice.AppAPI;
import com.divinetechs.ebooksapp.Webservice.BaseURL;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.shreyaspatil.EasyUpiPayment.EasyUpiPayment;
import com.shreyaspatil.EasyUpiPayment.listener.PaymentStatusListener;
import com.shreyaspatil.EasyUpiPayment.model.TransactionDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllPaymentActivity extends AppCompatActivity implements PaymentStatusListener, PaymentResultListener {

    LinearLayout ly_paypal, ly_upi;
    Button btn_continue;
    int select_payment = 0;

    String name, price;
    PrefManager prefManager;
    ProgressDialog progressDialog;

    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
    // note that these credentials will differ between live & sandbox
    // environments.

    private static PayPalConfiguration config;

    PayPalPayment thingToBuy;

    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;

    String amount, currency_code, short_description, id, state, create_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_payment);

        ly_paypal = findViewById(R.id.ly_paypal);
        ly_upi = findViewById(R.id.ly_upi);
        btn_continue = findViewById(R.id.btn_continue);

        prefManager = new PrefManager(this);

        progressDialog = new ProgressDialog(AllPaymentActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        Checkout.preload(getApplicationContext());

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            price = bundle.getString("bookprice");
            name = bundle.getString("booktitle");
            short_description = bundle.getString("bookdesc");
            create_time = bundle.getString("bookdate");
            id = bundle.getString("bookid");

            Log.e("bookdate", "" + create_time);
            Log.e("short_description", "" + short_description);
            Log.e("bookname", "" + name);
            Log.e("price", "" + price);
            Log.e("bookid", "" + id);
        }

        Integer[] arr = new Integer[9];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
        Collections.shuffle(Arrays.asList(arr));
        Log.e("value_", "" + Arrays.toString(arr));
        String TransactionId = Arrays.toString(arr);
        Log.e("value_str", "" + Arrays.toString(arr));


        ly_paypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_payment = 2;
                ly_paypal.setBackground(getResources().getDrawable(R.drawable.round_bor_yellow));
                ly_upi.setBackground(getResources().getDrawable(R.drawable.round_bor_gray));
            }
        });

        ly_upi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_payment = 3;
                ly_paypal.setBackground(getResources().getDrawable(R.drawable.round_bor_gray));
                ly_upi.setBackground(getResources().getDrawable(R.drawable.round_bor_yellow));
            }
        });

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (select_payment == 0) {
                    Toast.makeText(AllPaymentActivity.this, "Please select any payment method", Toast.LENGTH_LONG).show();
                } else if (select_payment == 2) {
                    Log.e("paypal_payment", "paypal_payment");
                    thingToBuy = new PayPalPayment(new BigDecimal("" + price), "KES",
                            "" + name, PayPalPayment.PAYMENT_INTENT_SALE);
                    Intent intent = new Intent(AllPaymentActivity.this,
                            PaymentActivity.class);
                    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
                    startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                } else if (select_payment == 3) {

                    //Toast.makeText(getApplicationContext(), "MPesa Payment coming soon", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(AllPaymentActivity.this, Payment.class);
                    intent.putExtra("price", price);
                    intent.putExtra("title", name);
                    startActivity(intent);
                }
            }
        });

//
        config = new PayPalConfiguration()
                .environment(CONFIG_ENVIRONMENT)
                .clientId(""+prefManager.getValue("paypal_client_id"))
                .merchantName(""+prefManager.getValue("app_name"))
                .merchantPrivacyPolicyUri(
                        Uri.parse("https://www.example.com/privacy"))
                .merchantUserAgreementUri(
                        Uri.parse("https://www.example.com/legal"));

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

    }

    /*================ Razor Pay =========================*/

    public void startPayment(String amount, String title) {
        Log.e("amount_pay", "" + amount);
        Log.e("book_title", "" + title);
        Checkout checkout = new Checkout();
        //checkout.setImage(R.drawable.logo);
        final Activity activity = this;
        try {
            JSONObject options = new JSONObject();
            options.put("name", title);
            options.put("amount", amount);
            options.put("description", "Reference No. #123456");
            options.put("currency", "INR");
            checkout.open(activity, options);
        } catch (Exception e) {
            Log.e("error", "Error in starting Razorpay Checkout", e);
            Log.e("error_msg", "msg" + e.getMessage());
        }
    }

    @Override
    public void onTransactionCompleted(TransactionDetails transactionDetails) {
        Log.d("TransactionDetails", transactionDetails.toString());
    }

    @Override
    public void onTransactionSuccess() {
        Log.d("Success_data", "Success");
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        currency_code = "INR";
        state = "approved";
        Log.e("s_id", "" + id);
        Log.e("u_id", "" + prefManager.getLoginId());
        Log.e("s_amout", "" + price);
        Log.e("s_currency_code", "" + currency_code);
        Log.e("s_short_description", "" + name);
        Log.e("s_create_time", "" + create_time);
        Purchasebook();
    }

    @Override
    public void onTransactionSubmitted() {
        Log.d("Submitted", "Submitted");
        Toast.makeText(this, "Pending | Submitted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTransactionFailed() {
        Log.d("Failed", "Failed");
        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        Log.e("", "");
    }

    @Override
    public void onTransactionCancelled() {
        Log.d("cancle", "Cancelled");
        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onPaymentSuccess(String s) {
        Log.e("on_success", "" + s);
        Toast.makeText(getApplicationContext(), "Payment Successfull", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Payment Successful: " + s, Toast.LENGTH_SHORT).show();
        currency_code = "INR";
        state = "approved";
        Log.e("successfully", "Payment Successfull");
        Log.e("s_id", "" + id);
        Log.e("u_id", "" + prefManager.getLoginId());
        Log.e("s_amout", "" + price);
        Log.e("s_currency_code", "" + currency_code);
        Log.e("s_short_description", "" + name);
        Log.e("s_create_time", "" + create_time);
        Purchasebook();
    }

    public void Purchasebook() {
        progressDialog.show();
        AppAPI bookNPlayAPI = BaseURL.getVideoAPI();
        Call<SuccessModel> call = bookNPlayAPI.add_purchase(id,
                prefManager.getLoginId(), price, currency_code,
                name, id, state, create_time);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                progressDialog.dismiss();
                Log.e("PurchaseBook", "" + response.body().getMessage());

                AlertDialog alertDialog = new AlertDialog.Builder(AllPaymentActivity.this).create();
                alertDialog.setTitle(""+getResources().getString(R.string.app_name));
                alertDialog.setMessage(""+response.body().getMessage());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                alertDialog.show();
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onPaymentError(int i, String s) {

    }

    /*================ End Razor Pay =========================*/


    /*===================Paypal===============================*/


    public void onFuturePaymentPressed(View pressed) {
        Intent intent = new Intent(AllPaymentActivity.this,
                PayPalFuturePaymentActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FUTURE_PAYMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data
                        .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        System.out.println(confirm.toJSONObject().toString(4));
                        System.out.println(confirm.getPayment().toJSONObject()
                                .toString(4));

                        new AlertDialog.Builder(AllPaymentActivity.this)
                                .setTitle(getResources().getString(R.string.app_name))
                                .setMessage("Thank you for purchasing " + confirm.getPayment().toJSONObject().getString("short_description"))
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();

                        Log.e("confirm1", "" + confirm.toJSONObject().toString(4));
                        Log.e("confirm2", "" + confirm.getPayment().toJSONObject()
                                .toString(4));

                        amount = confirm.getPayment().toJSONObject().getString("amount");
                        currency_code = confirm.getPayment().toJSONObject().getString("currency_code");
                        short_description = confirm.getPayment().toJSONObject().getString("short_description");
                        id = confirm.getProofOfPayment().toJSONObject().getString("id");
                        state = confirm.getProofOfPayment().toJSONObject().getString("state");
                        create_time = confirm.getProofOfPayment().toJSONObject().getString("create_time");

                        Log.e("amount", "" + amount);
                        Log.e("currency_code", "" + currency_code);
                        Log.e("short_description", "" + short_description);
                        Log.e("id", "" + id);
                        Log.e("state", "" + state);
                        Log.e("create_time", "" + create_time);

                        Purchasebook();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                System.out.println("The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                System.out
                        .println("An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth = data
                        .getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.e("FuturePaymentExample", auth.toJSONObject()
                                .toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.e("FuturePaymentExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        Toast.makeText(getApplicationContext(),
                                "Future Payment code received from PayPal",
                                Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        Log.e("FuturePaymentExample",
                                "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.e("FuturePaymentExample", "The user canceled.");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.e("FuturePaymentExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }
        }
    }

    private void sendAuthorizationToServer(PayPalAuthorization authorization) {

    }

    public void onFuturePaymentPurchasePressed(View pressed) {
        // Get the Application Correlation ID from the SDK
        String correlationId = PayPalConfiguration
                .getApplicationCorrelationId(this);

        Log.e("FuturePaymentExample", "Application Correlation ID: "
                + correlationId);
        // TODO: Send correlationId and transaction details to your server for
        // processing with
        // PayPal...
        Toast.makeText(getApplicationContext(),
                "App Correlation ID received from SDK", Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onDestroy() {
        // Stop service when done
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    /*=================End Paypal===============================*/

    public void mpesaPayedSuccess(){
        Toast.makeText(AllPaymentActivity.this,"Payment successful",Toast.LENGTH_LONG).show();
        state = "approved";
        Purchasebook();
    }

}
