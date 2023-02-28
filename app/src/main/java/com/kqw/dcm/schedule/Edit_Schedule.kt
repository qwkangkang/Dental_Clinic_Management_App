package com.kqw.dcm.schedule

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
import com.kqw.dcm.*
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.AccountSetting.Register
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient
import com.kqw.dcm.Patient.Patient_List
import kotlinx.android.synthetic.main.edit_schedule.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.patient.*
import kotlinx.android.synthetic.main.register_doc_assistant.*
import kotlinx.android.synthetic.main.schedule.*
import kotlinx.android.synthetic.main.title_bar.*
import kotlinx.android.synthetic.main.title_bar.tvTitle
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class Edit_Schedule: AppCompatActivity() {
    companion object {
        val TAG: String = Edit_Schedule::class.java.simpleName
    }

    private var db = Firebase.firestore
    lateinit var sharedPreferences: SharedPreferences
    var PREFS_KEY = "prefs"
    var USERID_KEY = "uid"
    var ROLE_KEY = "role"
    var sp_uid = ""
    var sp_role = ""
    var statusItems = listOf("Available", "Booked", "Unavailable")
    var timeItems = listOf("08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
    "12:00 PM", "12:30 PM", "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM",
        "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM", "07:00 PM", "07:30 PM", "08:00 PM")
    lateinit var adapterItems: ArrayAdapter<String>
    lateinit var adapterItems2: ArrayAdapter<String>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_schedule)

        //init setting
        btnBack.visibility = View.INVISIBLE
        tvTitle.text = "Schedule"
        //ibApp.setImageResource(R.drawable.appointment_orange)

        //variables
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!
        adapterItems = ArrayAdapter(this, R.layout.list_item_ddl, statusItems)
        adapterItems2 = ArrayAdapter(this, R.layout.list_item_ddl, timeItems)
        ddlStatus.setAdapter(adapterItems)
        ddlStartTime.setAdapter(adapterItems2)
        ddlEndTime.setAdapter(adapterItems2)
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        var doctorID:String ?= null
        var doctorName:String?=null
        var selectedDate:LocalDate?=null
        var selectedStartTime:LocalTime?=null
        var selectedEndTime:LocalTime?=null
        var selectedStatus:String?=null


        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")
        val today = LocalDateTime.now().format(dateFormat)
        etDateEditSchedule.setText(today)
        ddlStartTime.setText(adapterItems2.getItem(0), false)
        ddlEndTime.setText(adapterItems2.getItem(0), false)
        ddlStatus.setText(adapterItems.getItem(0), false)

        selectedDate = LocalDate.parse(etDateEditSchedule.text.toString(), dateFormat)

        db = FirebaseFirestore.getInstance()


                val refUser = db.collection("User").document(sp_uid.toString())
                refUser.get().addOnSuccessListener {
                    if(it!=null) {
                        doctorName = it.data?.get("user_first_name").toString()+" "+it.data?.get("user_last_name").toString()
                        Log.d(Edit_Schedule.TAG, "doctor: "+doctorName)
                        etDocInCharge.setText("Dr "+doctorName)

                        db.collection("Doctor").get()
                            .addOnSuccessListener {
                                if (!it.isEmpty) {
                                    for (doctor in it.documents) {
                                        if(doctor.data?.get("user_ID").toString()==sp_uid){
                                            doctorID = doctor.data?.get("doctor_ID").toString()
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener {
                                Log.d(Patient.TAG, "Failed retrieving doctor")
                            }

                        }
                }
                    .addOnFailureListener {
                        Log.d(Patient.TAG, "Failed retrieving User")
                    }





        btnLogout.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        etDateEditSchedule.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
                if(mDay<10){
                    if (mMonth+1<10){
                        etDateEditSchedule.setText("0"+mDay+"-0"+(mMonth+1)+"-"+mYear)
                    }else if(mMonth+1>=10){
                        etDateEditSchedule.setText("0"+mDay+"-"+(mMonth+1)+"-"+mYear)
                    }
                }
                else{
                    if ((mMonth+1)<10){
                        etDateEditSchedule.setText(""+mDay+"-0"+(mMonth+1)+"-"+mYear)
                    }
                    else {
                        etDateEditSchedule.setText("" + mDay + "-" + (mMonth + 1) + "-" + mYear)
                    }
                }

                selectedDate = LocalDate.parse(etDateEditSchedule.text.toString(), dateFormat)
                Log.d(Edit_Schedule.TAG, "date selected=>"+selectedDate)
            },year, month, day)
            dpd.show()
        }

        btnConfirm.setOnClickListener {
            selectedStartTime = LocalTime.parse(ddlStartTime.text.toString(), timeFormat)
            Log.d(Edit_Schedule.TAG, "start time selected=>"+selectedStartTime)
            selectedEndTime = LocalTime.parse(ddlEndTime.text.toString(), timeFormat)
            Log.d(Edit_Schedule.TAG, "end time selected=>"+selectedEndTime)

            val compareDate = selectedDate?.compareTo(LocalDate.now())
            val compareTime = selectedStartTime?.compareTo(selectedEndTime)

            tilDateEditSchedule.helperText = compareDate?.let { it1 -> validSelectedDate(it1) }
            tilEndTime.helperText = compareTime?.let { it1 -> validSelectedTime(it1) }

            if(tilDateEditSchedule.helperText==null&&tilEndTime.helperText==null){
                Log.d(Edit_Schedule.TAG, "Nothing wrong")

                val strSelectedDate:String ?= selectedDate?.format(dateFormat)
                val strSelectedStartTime:String?=selectedStartTime?.format(timeFormat)
                val strSelectedEndTime:String?=selectedEndTime?.format(timeFormat)
                selectedStatus = ddlStatus.text.toString()

                val id = db.collection("collection_name").document().id

                val scheduleMap = hashMapOf(
                    "schedule_ID" to id,
                    "schedule_date" to strSelectedDate,
                    "schedule_start_time" to strSelectedStartTime,
                    "schedule_end_time" to strSelectedEndTime,
                    "schedule_status" to selectedStatus,
                    "doctor_ID" to doctorID
                )
                db.collection("Schedule").document(id)
                    .set(scheduleMap)
                    .addOnSuccessListener { Log.d(Edit_Schedule.TAG, "Success add schedule") }
                    .addOnFailureListener { e -> Log.w(Edit_Schedule.TAG, "Error") }

            }
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

    }

    private fun validSelectedDate(compareDate:Int):String?{
        if (compareDate<0){
            return "Invalid Selected Time"
        }
        return null
    }

    private fun validSelectedTime(compareTime:Int):String?{
        if (compareTime>0){
            return "Invalid Selected Time"
        }
        return null
    }
}