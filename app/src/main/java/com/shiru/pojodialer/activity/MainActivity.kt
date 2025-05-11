package com.shiru.pojodialer.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shiru.pojodialer.R

class MainActivity : AppCompatActivity() {

    private lateinit var phoneNumberEditText: EditText
    private lateinit var callSim1Button: Button
    private lateinit var callSim2Button: Button
    private lateinit var telecomManager: TelecomManager
    private lateinit var simHandles: List<PhoneAccountHandle>
    private val PERMISSION_REQUEST_CODE = 101

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        checkAndRequestPermissions()
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        callSim1Button = findViewById(R.id.callSim1Button)
        callSim2Button = findViewById(R.id.callSim2Button)

        telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        simHandles = telecomManager.callCapablePhoneAccounts

        callSim1Button.setOnClickListener {
            makeCall(0)
        }

        callSim2Button.setOnClickListener {
            makeCall(1)
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CALL_PHONE),
            1
        )

        setListeners()
    }

    private fun makeCall(simIndex: Int) {
        val number = phoneNumberEditText.text.toString()
        if (number.isBlank()) {
            Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Call permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        if (simHandles.size > simIndex) {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
                putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", simHandles[simIndex])
            }
            startActivity(callIntent)
        } else {
            val simNumber = simIndex+1
            Toast.makeText(this, "SIM $simNumber  not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.CALL_PHONE)
        }

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val denied = permissions.zip(grantResults.toTypedArray())
                .filter { it.second != PackageManager.PERMISSION_GRANTED }

            if (denied.isNotEmpty()) {
                Toast.makeText(this, "Permissions denied: ${denied.map { it.first }}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setListeners(){
//        phoneNumberDisplay = findViewById(R.id.phoneNumberDisplay)

        val buttonIds = listOf(
            R.id.button1, R.id.button2, R.id.button3,
            R.id.button4, R.id.button5, R.id.button6,
            R.id.button7, R.id.button8, R.id.button9,
            R.id.buttonStar, R.id.button0, R.id.buttonHash
        )

        buttonIds.forEach { id ->
            findViewById<Button>(id).setOnClickListener { button ->
                val currentText = phoneNumberEditText.text.toString()
                val newChar = (button as Button).text.toString()
                phoneNumberEditText.setText(currentText + newChar)
            }
        }
    }

}
