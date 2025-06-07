package com.shiru.pojodialer.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.shiru.pojodialer.R

class DialpadFragment : BottomSheetDialogFragment() {
    private lateinit var phoneNumberDisplay: TextView
    private lateinit var backspaceButton: ImageButton
    private lateinit var callSim1Button: MaterialButton
    private lateinit var callSim2Button: MaterialButton
    private lateinit var telecomManager: TelecomManager
    private lateinit var simHandles: List<PhoneAccountHandle>
    private var currentNumber = StringBuilder()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialpad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        phoneNumberDisplay = view.findViewById(R.id.phoneNumberDisplay)
        backspaceButton = view.findViewById(R.id.backspaceButton)
        callSim1Button = view.findViewById(R.id.callSim1Button)
        callSim2Button = view.findViewById(R.id.callSim2Button)

        telecomManager = requireContext().getSystemService(TelecomManager::class.java)
        simHandles = telecomManager.callCapablePhoneAccounts

        // Set up backspace button
        backspaceButton.setOnClickListener {
            if (currentNumber.isNotEmpty()) {
                currentNumber.deleteCharAt(currentNumber.length - 1)
                updatePhoneNumberDisplay()
            }
        }

        // Set up dialpad buttons
        val buttonIds = listOf(
            R.id.button1, R.id.button2, R.id.button3,
            R.id.button4, R.id.button5, R.id.button6,
            R.id.button7, R.id.button8, R.id.button9,
            R.id.buttonStar, R.id.button0, R.id.buttonHash
        )

        buttonIds.forEach { id ->
            view.findViewById<MaterialButton>(id).setOnClickListener { button ->
                currentNumber.append((button as MaterialButton).text)
                updatePhoneNumberDisplay()
            }
        }

        // Set up call buttons
        callSim1Button.setOnClickListener { makeCall(0) }
        callSim2Button.setOnClickListener { makeCall(1) }

        // Update SIM buttons visibility
        updateSimButtonsVisibility()
    }

    private fun updatePhoneNumberDisplay() {
        phoneNumberDisplay.text = currentNumber.toString()
    }

    private fun makeCall(simIndex: Int) {
        val number = currentNumber.toString()
        if (number.isBlank()) {
            Toast.makeText(context, "Enter a valid number", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Call permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        if (simHandles.size > simIndex) {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
                putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", simHandles[simIndex])
            }
            startActivity(callIntent)
            dismiss()
        } else {
            val simNumber = simIndex + 1
            Toast.makeText(context, "SIM $simNumber not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSimButtonsVisibility() {
        callSim1Button.visibility = if (simHandles.isNotEmpty()) View.VISIBLE else View.GONE
        callSim2Button.visibility = if (simHandles.size > 1) View.VISIBLE else View.GONE
    }
} 