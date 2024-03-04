package com.kqw.dcm.schedule

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Appointment.Create_Appointment
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import kotlinx.android.synthetic.main.create_appointment.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.schedule.*
import kotlinx.android.synthetic.main.schedule.mnClinic
import kotlinx.android.synthetic.main.schedule.mnPatient
import kotlinx.android.synthetic.main.schedule_list.*
import kotlinx.android.synthetic.main.title_bar.*
import kotlinx.android.synthetic.main.view_appointment.*
import kotlinx.android.synthetic.main.view_appointment.etAppDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class Schedule_List:AppCompatActivity() {
    companion object {
        val TAG: String = Schedule_List::class.java.simpleName
    }

    private var db = Firebase.firestore
    lateinit var sharedPreferences: SharedPreferences
    var PREFS_KEY = "prefs"
    var USERID_KEY = "uid"
    var ROLE_KEY = "role"
    var sp_uid = ""
    var sp_role = ""
    @RequiresApi(Build.VERSION_CODES.O)
    val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    @RequiresApi(Build.VERSION_CODES.O)
    val timeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.schedule_list)
        ibHomeC.setImageResource(R.drawable.home_orange)

        //init setting
        tvTitle.text = "Schedule"
        ibApp.setImageResource(R.drawable.appointment_orange)
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!

        if(sp_role=="Doctor"||sp_role=="Assistant"){
            mnPatient.visibility = View.INVISIBLE
        }
        else{
            mnClinic.visibility = View.INVISIBLE
            btnEditSchedule.visibility = View.GONE
        }

        //variables
        val getToday = LocalDateTime.now().plusHours(9).minusMinutes(28)
        val today = getToday.format(dateFormat)
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        var doctorNameList: ArrayList<String>
        var adapterDocNameItems: ArrayAdapter<String>
        var selectedDoc:String?=null
        var selectedDate:LocalDate
        var selectedDoctorID:String?=null


        doctorNameList = arrayListOf()
        adapterDocNameItems = ArrayAdapter(this, R.layout.list_item_ddl, doctorNameList)
        ddlScheduleDoc.setAdapter(adapterDocNameItems)


        etScheduleDate.setText(today)

        val bundle: Bundle? = intent.extras

        bundle?.let {
            //patientID = bundle.getString("msgPatientID")
        }

        db.collection("User").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (user in it.documents) {
                        val role = user.data?.get("user_role").toString()
                        if(role=="Doctor"){
                            val doctorName = user.data?.get("user_first_name").toString()+" "+user.data?.get("user_last_name").toString()
                            doctorNameList.add("Dr "+doctorName)
                            if(doctorNameList!=null&&doctorNameList.size>0){
                                Log.d(Create_Appointment.TAG, "not null n >0")
                            }
                            if(doctorNameList!=null&&doctorNameList.size>0){
                                val firstDoc = doctorNameList[0]
                                ddlScheduleDoc.setText(firstDoc,false)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.d(Appointment_List.TAG, "failed retrieve user")
            }




        etScheduleDate.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
                if(mDay<10){
                    if (mMonth+1<10){
                        etScheduleDate.setText("0"+mDay+"-0"+(mMonth+1)+"-"+mYear)
                    }else if(mMonth+1>=10){
                        etScheduleDate.setText("0"+mDay+"-"+(mMonth+1)+"-"+mYear)
                    }
                }
                else{
                    if ((mMonth+1)<10){
                        etScheduleDate.setText(""+mDay+"-0"+(mMonth+1)+"-"+mYear)
                    }
                    else {
                        etScheduleDate.setText("" + mDay + "-" + (mMonth + 1) + "-" + mYear)
                    }
                }

                selectedDate = LocalDate.parse(etScheduleDate.text.toString(), dateFormat)
            },year, month, day)
            dpd.show()
        }

        ibSearchSchedule.setOnClickListener {
            selectedDoc = ddlScheduleDoc.text.toString()?.substring(3)
            //selectedDate = LocalDate.parse(etScheduleDate.text.toString(), dateFormat)
            db.collection("User").get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        for (user in it.documents) {
                            val role = user.data?.get("user_role").toString()
                            if (role == "Doctor") {
                                val doctorName = user.data?.get("user_first_name")
                                    .toString() + " " + user.data?.get("user_last_name").toString()
                                if (doctorName == selectedDoc) {
                                    val userID = user.data?.get("user_ID").toString()
                                    db.collection("Doctor").get()
                                        .addOnSuccessListener {
                                            if (!it.isEmpty) {
                                                for (doctor in it.documents) {
                                                    if (userID == doctor.data?.get("user_ID")
                                                            .toString()
                                                    ) {
                                                        selectedDoctorID =
                                                            doctor.data?.get("doctor_ID").toString()
                                                        refreshSchedule(selectedDoctorID!!)
                                                    }
                                                }
                                            }
                                        }.addOnFailureListener {
                                            Log.d(
                                                TAG,
                                                "failed retrieve doctor"
                                            )
                                        }
                                }
                            }
                        }
                    }
                }.addOnFailureListener { Log.d(TAG,"failed retrieve user") }
        }

        btnEditSchedule.setOnClickListener {
            if(sp_role!="Doctor"){
                Toast.makeText(this, "Only Doctors Can Edit Schedule", Toast.LENGTH_SHORT).show()
            }else{
                val intent = Intent(this, Edit_Schedule::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }

        btnLogout.setOnClickListener {
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnBack.setOnClickListener {
            finish()
        }

        //menu bar button
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
        ibHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }


        ibApp.setOnClickListener {
            val popupMenu: PopupMenu = PopupMenu(this, ibApp)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.btn_menu_submenu, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.iApp -> {
                        val intent = Intent(this, Appointment_List::class.java)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                    }
                    R.id.iSche -> {
                        val intent = Intent(this, Schedule_List::class.java)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                    }
                }
                true
            }

        }
        ibConsult.setOnClickListener {
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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceAsColor")
    private fun refreshSchedule(selectedDocID:String) {
        val selectedDate = etScheduleDate.text.toString()

        setDefaultColor()

        db.collection("Schedule").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (schedule in it.documents) {
                        val docID = schedule.data?.get("doctor_ID").toString()
                        val scheDate = schedule.data?.get("schedule_date").toString()
                        if (docID==selectedDocID&&scheDate==selectedDate){
                            val scheID = schedule.data?.get("schedule_ID").toString()
                            val scheStartTime = schedule.data?.get("schedule_start_time").toString()
                            val scheEnd = schedule.data?.get("schedule_end_time").toString()
                            val scheStatus = schedule.data?.get("schedule_status").toString()

                            setSlotColor(scheStartTime, scheEnd, scheStatus)
                        }
                    }
                }
            }.addOnFailureListener {Log.d(TAG, "failed retrieve schedule")}

    }

    private fun setDefaultColor() {
        tv0800to0830.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0830to0900.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0900to0930.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0930to1000.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv1000to1030.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv1030to1100.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv1100to1130.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv1130to1200.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv1200to1230.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv1230to0100.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0100to0130.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0130to0200.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0200to0230.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0230to0300.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0300to0330.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0330to0400.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0400to0430.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0430to0500.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0500to0530.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0530to0600.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0600to0630.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0630to0700.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0700to0730.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
        tv0730to0800.setBackground(ContextCompat.getDrawable(this, R.drawable.default_bar))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setSlotColor(scheStartTime: String, scheEnd: String, scheStatus: String) {

        //tv0830to0900.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        //selectedDate = LocalDate.parse(etScheduleDate.text.toString(), dateFormat)
        val startTime = LocalTime.parse(scheStartTime, timeFormat)
        val endTime = LocalTime.parse(scheEnd, timeFormat)

        val time8:LocalTime = LocalTime.of(8, 0)
        val time830:LocalTime = LocalTime.of(8, 30)
        val time9:LocalTime = LocalTime.of(9, 0)
        val time930:LocalTime = LocalTime.of(9, 30)
        val time10:LocalTime = LocalTime.of(10, 0)
        val time1030:LocalTime = LocalTime.of(10, 30)
        val time11:LocalTime = LocalTime.of(11, 0)
        val time1130:LocalTime = LocalTime.of(11, 30)
        val time12:LocalTime = LocalTime.of(12, 0)
        val time1230:LocalTime = LocalTime.of(12, 30)
        val time13:LocalTime = LocalTime.of(13, 0)
        val time1330:LocalTime = LocalTime.of(13, 30)
        val time14:LocalTime = LocalTime.of(14, 0)
        val time1430:LocalTime = LocalTime.of(14, 30)
        val time15:LocalTime = LocalTime.of(15, 0)
        val time1530:LocalTime = LocalTime.of(15, 30)
        val time16:LocalTime = LocalTime.of(16, 0)
        val time1630:LocalTime = LocalTime.of(16, 30)
        val time17:LocalTime = LocalTime.of(17, 0)
        val time1730:LocalTime = LocalTime.of(17, 30)
        val time18:LocalTime = LocalTime.of(18, 0)
        val time1830:LocalTime = LocalTime.of(18, 30)
        val time19:LocalTime = LocalTime.of(19, 0)
        val time1930:LocalTime = LocalTime.of(19, 30)
        val time20:LocalTime = LocalTime.of(20, 0)

        //available
        if(startTime.compareTo(time8)<=0&&endTime.compareTo(time830)>=0&&scheStatus=="Available"){
            tv0800to0830.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time830)<=0&&endTime.compareTo(time9)>=0&&scheStatus=="Available"){
            tv0830to0900.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time9)<=0&&endTime.compareTo(time930)>=0&&scheStatus=="Available"){
            tv0900to0930.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time930)<=0&&endTime.compareTo(time10)>=0&&scheStatus=="Available"){
            tv0930to1000.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time10)<=0&&endTime.compareTo(time1030)>=0&&scheStatus=="Available"){
            tv1000to1030.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time1030)<=0&&endTime.compareTo(time11)>=0&&scheStatus=="Available"){
            tv1030to1100.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time11)<=0&&endTime.compareTo(time1130)>=0&&scheStatus=="Available"){
            tv1100to1130.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time1130)<=0&&endTime.compareTo(time12)>=0&&scheStatus=="Available"){
            tv1130to1200.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time12)<=0&&endTime.compareTo(time1230)>=0&&scheStatus=="Available"){
            tv1200to1230.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time1230)<=0&&endTime.compareTo(time13)>=0&&scheStatus=="Available"){
            tv1230to0100.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time13)<=0&&endTime.compareTo(time1330)>=0&&scheStatus=="Available"){
            tv0100to0130.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time1330)<=0&&endTime.compareTo(time14)>=0&&scheStatus=="Available"){
            tv0130to0200.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time14)<=0&&endTime.compareTo(time1430)>=0&&scheStatus=="Available"){
            tv0200to0230.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time1430)<=0&&endTime.compareTo(time15)>=0&&scheStatus=="Available"){
            tv0230to0300.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time15)<=0&&endTime.compareTo(time1530)>=0&&scheStatus=="Available"){
            tv0300to0330.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time1530)<=0&&endTime.compareTo(time16)>=0&&scheStatus=="Available"){
            tv0330to0400.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time16)<=0&&endTime.compareTo(time1630)>=0&&scheStatus=="Available"){
            tv0400to0430.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time1630)<=0&&endTime.compareTo(time17)>=0&&scheStatus=="Available"){
            tv0430to0500.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time17)<=0&&endTime.compareTo(time1730)>=0&&scheStatus=="Available"){
            tv0500to0530.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time1730)<=0&&endTime.compareTo(time18)>=0&&scheStatus=="Available"){
            tv0530to0600.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time18)<=0&&endTime.compareTo(time1830)>=0&&scheStatus=="Available"){
            tv0600to0630.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time1830)<=0&&endTime.compareTo(time19)>=0&&scheStatus=="Available"){
            tv0630to0700.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time19)<=0&&endTime.compareTo(time1930)>=0&&scheStatus=="Available"){
            tv0700to0730.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }
        if(startTime.compareTo(time1930)<=0&&endTime.compareTo(time20)>=0&&scheStatus=="Available"){
            tv0730to0800.setBackground(ContextCompat.getDrawable(this, R.drawable.available_bar))
        }

        //Booked | Appointment
        if(startTime.compareTo(time8)<=0&&endTime.compareTo(time830)>=0&&scheStatus=="Booked"){
            tv0800to0830.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time830)<=0&&endTime.compareTo(time9)>=0&&scheStatus=="Booked"){
            tv0830to0900.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time9)<=0&&endTime.compareTo(time930)>=0&&scheStatus=="Booked"){
            tv0900to0930.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time930)<=0&&endTime.compareTo(time10)>=0&&scheStatus=="Booked"){
            tv0930to1000.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time10)<=0&&endTime.compareTo(time1030)>=0&&scheStatus=="Booked"){
            tv1000to1030.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time1030)<=0&&endTime.compareTo(time11)>=0&&scheStatus=="Booked"){
            tv1030to1100.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time11)<=0&&endTime.compareTo(time1130)>=0&&scheStatus=="Booked"){
            tv1100to1130.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time1130)<=0&&endTime.compareTo(time12)>=0&&scheStatus=="Booked"){
            tv1130to1200.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time12)<=0&&endTime.compareTo(time1230)>=0&&scheStatus=="Booked"){
            tv1200to1230.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time1230)<=0&&endTime.compareTo(time13)>=0&&scheStatus=="Booked"){
            tv1230to0100.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time13)<=0&&endTime.compareTo(time1330)>=0&&scheStatus=="Booked"){
            tv0100to0130.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time1330)<=0&&endTime.compareTo(time14)>=0&&scheStatus=="Booked"){
            tv0130to0200.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time14)<=0&&endTime.compareTo(time1430)>=0&&scheStatus=="Booked"){
            tv0200to0230.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time1430)<=0&&endTime.compareTo(time15)>=0&&scheStatus=="Booked"){
            tv0230to0300.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time15)<=0&&endTime.compareTo(time1530)>=0&&scheStatus=="Booked"){
            tv0300to0330.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time1530)<=0&&endTime.compareTo(time16)>=0&&scheStatus=="Booked"){
            tv0330to0400.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time16)<=0&&endTime.compareTo(time1630)>=0&&scheStatus=="Booked"){
            tv0400to0430.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time1630)<=0&&endTime.compareTo(time17)>=0&&scheStatus=="Booked"){
            tv0430to0500.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time17)<=0&&endTime.compareTo(time1730)>=0&&scheStatus=="Booked"){
            tv0500to0530.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time1730)<=0&&endTime.compareTo(time18)>=0&&scheStatus=="Booked"){
            tv0530to0600.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time18)<=0&&endTime.compareTo(time1830)>=0&&scheStatus=="Booked"){
            tv0600to0630.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time1830)<=0&&endTime.compareTo(time19)>=0&&scheStatus=="Booked"){
            tv0630to0700.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time19)<=0&&endTime.compareTo(time1930)>=0&&scheStatus=="Booked"){
            tv0700to0730.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }
        if(startTime.compareTo(time1930)<=0&&endTime.compareTo(time20)>=0&&scheStatus=="Booked"){
            tv0730to0800.setBackground(ContextCompat.getDrawable(this, R.drawable.appointment_bar))
        }

        //Unavailable
        if(startTime.compareTo(time8)<=0&&endTime.compareTo(time830)>=0&&scheStatus=="Unavailable"){
            tv0800to0830.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time830)<=0&&endTime.compareTo(time9)>=0&&scheStatus=="Unavailable"){
            tv0830to0900.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time9)<=0&&endTime.compareTo(time930)>=0&&scheStatus=="Unavailable"){
            tv0900to0930.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time930)<=0&&endTime.compareTo(time10)>=0&&scheStatus=="Unavailable"){
            tv0930to1000.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time10)<=0&&endTime.compareTo(time1030)>=0&&scheStatus=="Unavailable"){
            tv1000to1030.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time1030)<=0&&endTime.compareTo(time11)>=0&&scheStatus=="Unavailable"){
            tv1030to1100.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time11)<=0&&endTime.compareTo(time1130)>=0&&scheStatus=="Unavailable"){
            tv1100to1130.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time1130)<=0&&endTime.compareTo(time12)>=0&&scheStatus=="Unavailable"){
            tv1130to1200.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time12)<=0&&endTime.compareTo(time1230)>=0&&scheStatus=="Unavailable"){
            tv1200to1230.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time1230)<=0&&endTime.compareTo(time13)>=0&&scheStatus=="Unavailable"){
            tv1230to0100.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time13)<=0&&endTime.compareTo(time1330)>=0&&scheStatus=="Unavailable"){
            tv0100to0130.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time1330)<=0&&endTime.compareTo(time14)>=0&&scheStatus=="Unavailable"){
            tv0130to0200.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time14)<=0&&endTime.compareTo(time1430)>=0&&scheStatus=="Unavailable"){
            tv0200to0230.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time1430)<=0&&endTime.compareTo(time15)>=0&&scheStatus=="Unavailable"){
            tv0230to0300.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time15)<=0&&endTime.compareTo(time1530)>=0&&scheStatus=="Unavailable"){
            tv0300to0330.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time1530)<=0&&endTime.compareTo(time16)>=0&&scheStatus=="Unavailable"){
            tv0330to0400.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time16)<=0&&endTime.compareTo(time1630)>=0&&scheStatus=="Unavailable"){
            tv0400to0430.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time1630)<=0&&endTime.compareTo(time17)>=0&&scheStatus=="Unavailable"){
            tv0430to0500.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time17)<=0&&endTime.compareTo(time1730)>=0&&scheStatus=="Unavailable"){
            tv0500to0530.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time1730)<=0&&endTime.compareTo(time18)>=0&&scheStatus=="Unavailable"){
            tv0530to0600.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time18)<=0&&endTime.compareTo(time1830)>=0&&scheStatus=="Unavailable"){
            tv0600to0630.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time1830)<=0&&endTime.compareTo(time19)>=0&&scheStatus=="Unavailable"){
            tv0630to0700.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time19)<=0&&endTime.compareTo(time1930)>=0&&scheStatus=="Unavailable"){
            tv0700to0730.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }
        if(startTime.compareTo(time1930)<=0&&endTime.compareTo(time20)>=0&&scheStatus=="Unavailable"){
            tv0730to0800.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_bar))
        }



    }
}