package com.shiru.pojodialer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shiru.pojodialer.R
import com.shiru.pojodialer.model.CallLogEntry

class CallLogAdapter(
    private val callLogs: List<CallLogEntry>,
    private val onDialClick: (CallLogEntry) -> Unit
) : RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder>() {

    class CallLogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val phoneNumber: TextView = view.findViewById(R.id.phoneNumber)
        val callTime: TextView = view.findViewById(R.id.callTime)
        val dialButton: ImageButton = view.findViewById(R.id.dialButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallLogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_call_log, parent, false)
        return CallLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: CallLogViewHolder, position: Int) {
        val callLog = callLogs[position]
        holder.phoneNumber.text = callLog.phoneNumber
        holder.callTime.text = callLog.callTime
        holder.dialButton.setOnClickListener { onDialClick(callLog) }
    }

    override fun getItemCount() = callLogs.size
} 