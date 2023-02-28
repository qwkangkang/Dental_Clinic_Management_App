package com.kqw.dcm.Appointment

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.AccountSetting.Register
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_Data
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Edit_Schedule
import kotlinx.android.synthetic.main.appointment_list.*
import kotlinx.android.synthetic.main.appointment_list.mnClinic
import kotlinx.android.synthetic.main.appointment_list.mnPatient
import kotlinx.android.synthetic.main.create_appointment.*
import kotlinx.android.synthetic.main.edit_schedule.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.title_bar.*
import kotlinx.android.synthetic.main.view_appointment.*
import kotlinx.android.synthetic.main.view_appointment.etAppDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class Create_Appointment:AppCompatActivity() {
    companion object{
        val TAG:String = Create_Appointment::class.java.simpleName
    }

    private var db = Firebase.firestore
    lateinit var sharedPreferences: SharedPreferences
    var PREFS_KEY = "prefs"
    var USERID_KEY = "uid"
    var ROLE_KEY = "role"
    var sp_uid = ""
    var sp_role = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_appointment)

        //init setting
        tvTitle.text = "Appointment"
        ibApp.setImageResource(R.drawable.appointment_orange)


        //variables
        var patientID:String?=null
        var userID:String?=null
        var patientName:String?=null
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")
        val today = LocalDateTime.now().format(dateFormat)
        etAppDate.setText(today)
        var doctorNameList: ArrayList<String>
        var roomIDList: ArrayList<String>
        var timeItems = listOf("08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
            "12:00 PM", "12:30 PM", "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM",
            "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM", "07:00 PM", "07:30 PM", "08:00 PM")
        var serviceItems = listOf("Checkup (30mins)", "Scaling (30mins)", "Filling (1hr)", "Extraction (2hrs)")
        doctorNameList = arrayListOf()
        roomIDList = arrayListOf()
        var adapterTimeItems: ArrayAdapter<String>
        var adapterServiceItems: ArrayAdapter<String>
        var adapterDocNameItems: ArrayAdapter<String>
        var adapterRoomItems: ArrayAdapter<String>
        var selectedDate:LocalDate?=null
        var appStartTime:LocalTime?=null
        var selectedService:String?=null
        var duration:Int?=null
        var appEndTime:LocalTime?=null
        var selectedDoc:String?=null
        var selectedRoom:String?=null
        var selectedDoctorID:String?=null

        if(sp_role=="Doctor"||sp_role=="Assistant"){
            mnPatient.visibility = View.INVISIBLE
        }
        else{
            mnClinic.visibility = View.INVISIBLE
        }

        val bundle: Bundle? = intent.extras

        bundle?.let {
            patientID = bundle.getString("msgPatientID")
        }

        db = FirebaseFirestore.getInstance()

        val refPatient = db.collection("Patient").document(patientID.toString())
        refPatient.get().addOnSuccessListener {
            if (it != null) {
                userID = it.data?.get("user_ID").toString()

                val refUser = db.collection("User").document(userID.toString())
                refUser.get().addOnSuccessListener {
                    if (it != null) {
                        patientName = it.data?.get("user_first_name")
                            .toString() + " " + it.data?.get("user_last_name").toString()
                        tvPatientName.text = patientName
                    }
                }
                    .addOnFailureListener {
                        Log.d(Create_Appointment.TAG, "retrieve user failed")
                    }
            }
        }.addOnFailureListener {
            Log.d(Create_Appointment.TAG, "retrieve patient failed")
        }

        db.collection("Room").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (room in it.documents) {
                        val roomID = room.data?.get("room_ID").toString()
//                        Log.d(Create_Appointment.TAG, "room ID=>"+roomID)
                        roomIDList.add(roomID)
                    }
                }
            }
            .addOnFailureListener {
                Log.d(Create_Appointment.TAG, "failed retrieve room")
            }

        db.collection("User").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (user in it.documents) {
                        val role = user.data?.get("user_role").toString()
                        if(role=="Doctor"){
                            val doctorName = user.data?.get("user_first_name").toString()+" "+user.data?.get("user_last_name").toString()
//                            Log.d(Create_Appointment.TAG, "doctor name=>"+doctorName)
                            doctorNameList.add("Dr "+doctorName)
                        }

                    }
                }
            }
            .addOnFailureListener {
                Log.d(Appointment_List.TAG, "failed retrieve user")
            }

        adapterTimeItems = ArrayAdapter(this, R.layout.list_item_ddl, timeItems)
        adapterServiceItems = ArrayAdapter(this, R.layout.list_item_ddl, serviceItems)
        adapterDocNameItems = ArrayAdapter(this, R.layout.list_item_ddl, doctorNameList)
        adapterRoomItems = ArrayAdapter(this, R.layout.list_item_ddl, roomIDList)
        ddlAppTime.setAdapter(adapterTimeItems)
        ddlService.setAdapter(adapterServiceItems)
        ddlDocInCharge.setAdapter(adapterDocNameItems)
        ddlRoom.setAdapter(adapterRoomItems)
        ddlAppTime.setText(adapterTimeItems.getItem(0), false)
        ddlService.setText(adapterServiceItems.getItem(0), false)
//        ddlDocInCharge.setText(adapterDocNameItems.getItem(0), false)
//        ddlRoom.setText(adapterRoomItems.getItem(0), false)

        btnBack.setOnClickListener {
            val intent = Intent(this, Appointment_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        etAppDate.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
                if(mDay<10){
                    if (mMonth+1<10){
                        etAppDate.setText("0"+mDay+"-0"+(mMonth+1)+"-"+mYear)
                    }else if(mMonth+1>=10){
                        etAppDate.setText("0"+mDay+"-"+(mMonth+1)+"-"+mYear)
                    }
                }
                else{
                    if ((mMonth+1)<10){
                        etAppDate.setText(""+mDay+"-0"+(mMonth+1)+"-"+mYear)
                    }
                    else {
                        etAppDate.setText("" + mDay + "-" + (mMonth + 1) + "-" + mYear)
                    }
                }

                selectedDate = LocalDate.parse(etAppDate.text.toString(), dateFormat)
            },year, month, day)
            dpd.show()
        }

        btnCreate.setOnClickListener {

            //selectedDate
//            Log.d(Create_Appointment.TAG, "start time=>"+ddlAppTime.text.toString())
            appStartTime = LocalTime.parse(ddlAppTime.text.toString(), timeFormat)
            selectedService = ddlService.text.toString()
            selectedRoom = ddlRoom.text.toString()
            selectedDate = LocalDate.parse(etAppDate.text.toString(), dateFormat)
//            Log.d(Create_Appointment.TAG, "service=>"+selectedService)
            if (selectedService=="Checkup (30mins)"||selectedService=="Scaling (30mins)"){
                appEndTime = appStartTime?.plusMinutes(30)
            }
            else if (selectedService=="Filling (1hr)"){
                appEndTime = appStartTime?.plusMinutes(60)
            }
            else if (selectedService=="Extraction (2hrs)"){
                appEndTime = appStartTime?.plusMinutes(120)
            }

            val compareDate = selectedDate?.compareTo(LocalDate.now())
            tilAppDate.helperText = compareDate?.let { it1 -> validSelectedDate(it1) }

//            now.add(Calendar.MINUTE, diffInMin.toInt())
//            appEndTime = appStartTime.add(Calendar.MINUTE, diff)

            selectedDoc = ddlDocInCharge.text.toString()
            val strDoctorName = selectedDoc?.substring(3)

            Log.d(Create_Appointment.TAG, "Step -2")
            db.collection("User").get()
                .addOnSuccessListener {
                    Log.d(Create_Appointment.TAG, "Step -1")
                    if (!it.isEmpty) {
                        for (user in it.documents) {
                            val role = user.data?.get("user_role").toString()
                            if(role=="Doctor"){
                                Log.d(Create_Appointment.TAG, "Step 0")
                                val doctorName = user.data?.get("user_first_name").toString()+" "+user.data?.get("user_last_name").toString()
                                if(doctorName==strDoctorName) {
                                    val userID = user.data?.get("user_ID").toString()
                                    db.collection("Doctor").get()
                                        .addOnSuccessListener {
                                            if (!it.isEmpty) {
                                                for (doctor in it.documents) {
                                                    if (userID == doctor.data?.get("user_ID").toString()
                                                    ) {
                                                        selectedDoctorID = doctor.data?.get("doctor_ID").toString()
                                                        Log.d(Create_Appointment.TAG, "1. selected docID: "+selectedDoctorID)



                                                        //paste
                                                        var availableDate:LocalDate?=null
                                                        var availableStartTime:LocalTime?=null
                                                        var availableEndTime:LocalTime?=null
                                                        Log.d(Create_Appointment.TAG, "2. selected docID: "+selectedDoctorID)
                                                        db.collection("Schedule").get()
                                                            .addOnSuccessListener {
                                                                if (!it.isEmpty) {
                                                                    for (schedule in it.documents) {
                                                                        val sDoctorID = schedule.data?.get("doctor_ID").toString()
                                                                        if (sDoctorID == selectedDoctorID) {
                                                                            val sStrScheduleDate = schedule.data?.get("schedule_date").toString()
                                                                            availableDate = LocalDate.parse(sStrScheduleDate, dateFormat)
                                                                            if (availableDate == selectedDate) {
                                                                                if (schedule.data?.get("schedule_status")
                                                                                        .toString() == "Available"
                                                                                ) {
                                                                                    val strScheduleStartTime =
                                                                                        schedule.data?.get("schedule_start_time").toString()
                                                                                    availableStartTime =
                                                                                        LocalTime.parse(strScheduleStartTime, timeFormat)
                                                                                    val strScheduleEndTime =
                                                                                        schedule.data?.get("schedule_end_time").toString()
                                                                                    availableEndTime =
                                                                                        LocalTime.parse(strScheduleEndTime, timeFormat)

                                                                                    Log.d(Create_Appointment.TAG, "retrieve start time=>"+strScheduleStartTime)
                                                                                    Log.d(Create_Appointment.TAG, "retrieve end time=>"+strScheduleEndTime)

//                                                                                    var valid:Boolean=checkAppTimeAvailability(availableStartTime, availableEndTime, appStartTime, appEndTime)
                                                                                    if(checkAppTimeAvailability(availableStartTime, availableEndTime, appStartTime, appEndTime)){

                                                                                        val refRoom = db.collection("Room").document(selectedRoom.toString())
                                                                                        var status:String
                                                                                        Log.d(Create_Appointment.TAG, "selected room "+selectedRoom)
                                                                                        refRoom.get().addOnSuccessListener {
                                                                                            if(it!=null) {
                                                                                                status = it.data?.get("room_status").toString()
                                                                                                Log.d(Create_Appointment.TAG, "room status "+status)

                                                                                                if(status=="Available"){
                                                                                                    Log.d(Create_Appointment.TAG, "All is available after retrieve")
                                                                                                    val scheduleID = schedule.data?.get("schedule_ID").toString()
                                                                                                    Log.d(Create_Appointment.TAG, "date"+etAppDate.text.toString())
                                                                                                    Log.d(Create_Appointment.TAG, "start"+appStartTime?.format(timeFormat))
                                                                                                    createAppointment(etAppDate.text.toString(), appStartTime?.format(timeFormat),
                                                                                                        appEndTime?.format(timeFormat), selectedRoom, patientID, scheduleID, selectedService)
                                                                                                }
                                                                                                else{
                                                                                                    Log.d(Create_Appointment.TAG, "room is not available")
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        break
                                                                                    }else{
                                                                                        Log.d(Create_Appointment.TAG, "time is not within")
                                                                                        Log.d(Create_Appointment.TAG, "S1 "+appStartTime+" vs S2 "+availableStartTime)
                                                                                        Log.d(Create_Appointment.TAG, "E1 "+appEndTime+" vs E2 "+availableEndTime)
                                                                                    }

                                                                                } else {
                                                                                    Log.d(Create_Appointment.TAG, "status not available")
                                                                                }
                                                                            } else {
                                                                                Log.d(Create_Appointment.TAG, "date not available")
                                                                                Log.d(Create_Appointment.TAG, "Date1 "+selectedDate.toString()+" vs Date2"+availableDate.toString())
                                                                            }
                                                                        } else {
                                                                            Log.d(Create_Appointment.TAG, sDoctorID+" vs "+selectedDoctorID)
                                                                            Log.d(Create_Appointment.TAG, "doctor not available")
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            .addOnFailureListener {
                                                                Log.d(Create_Appointment.TAG, "failed retrieve schedule")
                                                            }
                                                        //end paste
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener {
                                            Log.d(Create_Appointment.TAG, "failed retrieve doctor")
                                        }

                                }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d(Create_Appointment.TAG, "failed retrieve user")
                }




        }




        //menu bar button
        ibHome.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        ibApp.setOnClickListener{
            val intent = Intent(this, Appointment_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        ibConsult.setOnClickListener{
            val intent = Intent(this, Consultation_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        ibHistory.setOnClickListener {
            val intent = Intent(this, Treatment_History_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        ibProfile.setOnClickListener {
            val intent = Intent(this, Account_Setting::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        ibHomeC.setOnClickListener {
            val intent = Intent(this, MainActivity_Clinic::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        ibPatientC.setOnClickListener {
            val intent = Intent(this, Patient_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        ibConsultC.setOnClickListener {
            val intent = Intent(this, Consultation_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        ibProfileC.setOnClickListener {
            val intent = Intent(this, Account_Setting::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
    private fun validSelectedDate(compareDate:Int):String?{
        if (compareDate<0){
            return "Invalid Selected Time"
        }
        return null
    }
    private fun checkAppTimeAvailability(availableStartTime:LocalTime?, availableEndTime:LocalTime?, appStartTime:LocalTime?, appEndTime:LocalTime?):Boolean{
        val compareStartTime = appStartTime?.compareTo(availableStartTime)
        val compareEndTime = appEndTime?.compareTo(availableEndTime)

            if(compareStartTime!!>=0&&compareEndTime!!<=0) {
                return true
            }
        return false
    }

    private fun createAppointment(appDate: String?, appStartTime: String?,appEndTime: String?, selectedRoom:String?, patientID:String?, scheduleID:String?, service:String?){
        if(tilAppDate.helperText==null){

            val appID = db.collection("collection_name").document().id

            val appMap = hashMapOf(
                "appointment_ID" to appID,
                "appointment_status" to "Confirmed",
                "appointment_date" to appDate,
                "appointment_start_time" to appStartTime,
                "appointment_end_time" to appEndTime,
                "appointment_service" to service,
                "appointment_cancel_reason" to "",
                "room_ID" to selectedRoom,
                "patient_ID" to patientID,
                "schedule_ID" to scheduleID
            )

            db.collection("Appointment").document(appID)
                .set(appMap)
                .addOnSuccessListener { Log.d(Create_Appointment.TAG, "Success add appointment")
                finish()
//                    val updateSchedule
                }
                .addOnFailureListener { e -> Log.w(Create_Appointment.TAG, "failed create app") }

        }

    }
}