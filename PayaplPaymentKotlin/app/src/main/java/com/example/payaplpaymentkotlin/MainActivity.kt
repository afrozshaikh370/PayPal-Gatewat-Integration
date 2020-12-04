package com.example.payaplpaymentkotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.paypal.android.sdk.payments.*
import org.json.JSONObject
import java.math.BigDecimal


class MainActivity : AppCompatActivity() {

    val paypalrequestcode: Int = 7171
    val paypalConfig = PayPalConfiguration()
        .environment(PayPalConfiguration.ENVIRONMENT_NO_NETWORK)
        .clientId(Config().paypalClintId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val donatebtn = findViewById<Button>(R.id.donate_btn)
        val amountenter = findViewById<EditText>(R.id.amount_enter)
        intent = Intent(this, PayPalService::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig)
        startService(intent)

        donatebtn.setOnClickListener {
            val amount = amountenter.text.toString()
            if (amount.isEmpty()) {
                amountenter.error = "Enter Amount!"
                return@setOnClickListener
            }

            processPayment(amount)

        }

    }

    private fun processPayment(amount: String) {
        val payPalPayment = PayPalPayment(
            BigDecimal(amount),
            "USD",
            "Donate",
            PayPalPayment.PAYMENT_INTENT_SALE
        )
        val intent = Intent(this, PaymentActivity::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig)
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment)
        startActivityForResult(intent, paypalrequestcode)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == paypalrequestcode) {
            if (resultCode == RESULT_OK) {
                val paymentConfirmation =
                    data?.getParcelableExtra<PaymentConfirmation>(PaymentActivity.EXTRA_RESULT_CONFIRMATION)
                if (paymentConfirmation != null) {
                    val confirmationDetails = paymentConfirmation.toJSONObject().toString(4)
                    showDetails(confirmationDetails)
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Payment Cancel!", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == PaymentActivity.RESULT_EXTRAS_INVALID)
            Toast.makeText(this, "Invalid!", Toast.LENGTH_SHORT).show()
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showDetails(details: String) {
        val jsonObject = JSONObject(details)
        Toast.makeText(
            this,
            "Payment ID : " + jsonObject.getJSONObject("response").getString("id")
                    + "\nStatus : " + jsonObject.getJSONObject("response").getString("state"),
            Toast.LENGTH_SHORT
        ).show()
    }


    override fun onDestroy() {
        stopService(Intent(this, PayPalService::class.java))
        super.onDestroy()
    }
}