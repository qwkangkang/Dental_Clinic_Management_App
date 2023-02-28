package com.kqw.dcm.Appointment

data class Appointment_Data (val appID:String, val patientID:String, val docName: String, val service:String, val appDate: String,
val appStartTime:String, val status:String){
}