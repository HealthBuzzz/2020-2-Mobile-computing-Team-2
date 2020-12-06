package com.healthbuzz.healthbuzz

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_water_amount_input.*

class WaterAmountInputActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_amount_input)
        water_btn_input_water_go.setOnClickListener {
            val theValue = editTextNumberWaterInput.text.toString()
            try {
                RealtimeModel.water_count.postValue(
                    (RealtimeModel.water_count.value?.toLong() ?: 0) +
                            theValue.toInt()
                ) // add 1
            } catch (e: Exception) {
                Log.e(TAG, "Failed to input $theValue")
            }
            Toast.makeText(this, "Successfully added water log!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}