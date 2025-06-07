package com.shiru.pojodialer.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shiru.pojodialer.R
import com.shiru.pojodialer.adapter.CallLogAdapter
import com.shiru.pojodialer.fragment.DialpadFragment
import com.shiru.pojodialer.model.CallLogEntry
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

//    private lateinit var phoneNumberEditText: EditText
//    private lateinit var callSim1Button: Button
//    private lateinit var callSim2Button: Button
    private lateinit var telecomManager: TelecomManager
    private lateinit var simHandles: List<PhoneAccountHandle>
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabDialpad: FloatingActionButton
    private lateinit var searchEditText: EditText
    private val PERMISSION_REQUEST_CODE = 101
    private var allCallLogs = listOf<CallLogEntry>()

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.callLogsRecyclerView)
        fabDialpad = findViewById(R.id.fabDialpad)
        searchEditText = findViewById(R.id.searchEditText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        setupSearch()
        checkAndRequestPermissions()
//        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
//        callSim1Button = findViewById(R.id.callSim1Button)
//        callSim2Button = findViewById(R.id.callSim2Button)

        telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        simHandles = telecomManager.callCapablePhoneAccounts

//        callSim1Button.setOnClickListener {
//            makeCall(0)
//        }
//
//        callSim2Button.setOnClickListener {
//            makeCall(1)
//        }

        fabDialpad.setOnClickListener {
            showDialpad()
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CALL_PHONE),
            1
        )

//        setListeners()
    }

    /*private fun makeCall(simIndex: Int) {
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
    }*/

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterCallLogs(s.toString())
            }
        })
    }

    private fun filterCallLogs(query: String) {
        if (query.isEmpty()) {
            updateCallLogsList(allCallLogs)
            return
        }

        val filteredList = allCallLogs.filter { callLog ->
            callLog.phoneNumber.contains(query, ignoreCase = true)
        }
        updateCallLogsList(filteredList)
    }

    private fun updateCallLogsList(callLogs: List<CallLogEntry>) {
        recyclerView.adapter = CallLogAdapter(callLogs) { callLog ->
            showDialpad(callLog.phoneNumber)
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

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.READ_CALL_LOG)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            loadCallLogs()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                loadCallLogs()
            } else {
                Toast.makeText(this, "Permissions required for app functionality", Toast.LENGTH_LONG).show()
            }
        }
    }

/*    fun setListeners(){
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
    }*/

    @SuppressLint("Range")
    private fun loadCallLogs() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val callLogs = mutableListOf<CallLogEntry>()
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

        contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            CallLog.Calls.DATE + " DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                val date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE))
                val type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))
                val duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))

                callLogs.add(
                    CallLogEntry(
                        phoneNumber = number,
                        callTime = dateFormat.format(Date(date)),
                        callType = type,
                        duration = duration
                    )
                )
            }
        }

        allCallLogs = callLogs
        updateCallLogsList(callLogs)
    }

    private fun showDialpad(prefilledNumber: String = "") {
        val dialpadFragment = DialpadFragment()
        dialpadFragment.show(supportFragmentManager, "dialpad")
    }

    override fun onResume() {
        super.onResume()
        loadCallLogs()
    }
}
