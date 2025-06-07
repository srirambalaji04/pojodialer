package com.shiru.pojodialer.model

data class CallLogEntry(
    val phoneNumber: String,
    val callTime: String,
    val callType: Int,
    val duration: Long
) 