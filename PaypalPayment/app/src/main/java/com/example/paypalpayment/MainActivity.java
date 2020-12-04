package com.example.paypalpayment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {


    private static final PayPalConfiguration payPalConfig = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_NO_NETWORK)
            .clientId(Configuration.PayPAl_Clint_Id);

    private Button donate_btn;
    private Context context;

    private static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        Intent intent = new Intent(context, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfig);
        startService(intent);

        //Views 
        {

            donate_btn = findViewById(R.id.donation_btn);
        }

        donate_btn.setOnClickListener((view) -> {
            int amount = 100;
            paymentPocess(amount);
        });
    }

    private void paymentPocess(int amount) {
        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(amount), "USD",
                "Pay For Product",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfig);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        startActivityForResult(intent, Configuration.PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Configuration.PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                PaymentConfirmation paymentConfirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (paymentConfirmation != null) {
                    String confirmation = null;
                    try {
                        confirmation = paymentConfirmation.toJSONObject().toString(4);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showDetails(confirmation);
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(context, "Payment Cancel!", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Toast.makeText(context, "Invalid!", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showDetails(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            Toast.makeText(context, "Payment ID : " + jsonObject.getJSONObject("response").getString("id")
                    + "\nStatus : " + jsonObject.getJSONObject("response").getString("state"), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        stopService(new Intent(context, PayPalService.class));
        super.onDestroy();
    }
}