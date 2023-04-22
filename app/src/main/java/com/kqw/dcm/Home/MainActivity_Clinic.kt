package com.kqw.dcm.Home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting_Clinic
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Appointment.CheckIn_Scanner
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.FAQ.FAQ_List
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.cvAppointment
import kotlinx.android.synthetic.main.activity_main.cvFAQ
import kotlinx.android.synthetic.main.activity_main.ibLogout
import kotlinx.android.synthetic.main.activity_main_clinic.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity_Clinic: AppCompatActivity() {
    companion object{
        val TAG:String = MainActivity_Clinic::class.java.simpleName
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
        setContentView(R.layout.activity_main_clinic)
        ibHomeC.setImageResource(R.drawable.home_orange)

        //init
//        tvViewMore.visibility = View.INVISIBLE
//        tvViewMore2.visibility = View.INVISIBLE

        //variables


        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!


        //retrieve user's fname
        val ref = db.collection("User").document(sp_uid.toString())
        ref.get().addOnSuccessListener {
            if (it != null) {
                val fname = it.data?.get("user_first_name").toString()
                tvHelloClinic.text = "Hello, $fname"
            }
        }

        refreshQueueNo()
        refreshPatientNo()


        //buttons to next activities
        ibLogout.setOnClickListener {
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        cvAppointment.setOnClickListener {
            val intent = Intent(this, Appointment_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        cvPatient.setOnClickListener {
            val intent = Intent(this, Patient_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        cvFAQ.setOnClickListener {
            val intent = Intent(this, FAQ_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        cvSchedule.setOnClickListener {
            val intent = Intent(this, Schedule_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        cvScan.setOnClickListener {
            val intent = Intent(this, CheckIn_Scanner::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
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
            val intent = Intent(this, Account_Setting_Clinic::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
    var handler: Handler = Handler()
    var runnable: Runnable? = null
    var delay:Long = 30000
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay)
            refreshQueueNo()
            refreshPatientNo()
        }.also { runnable = it }, delay)
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshQueueNo() {
        var today: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
        var ldToday: LocalDate = LocalDateTime.ofInstant(today.toInstant(), today.getTimeZone().toZoneId()).toLocalDate()
        Log.d(TAG, "today is "+ldToday)
        val ltNow:LocalTime = LocalDateTime.ofInstant(today.toInstant(), today.getTimeZone().toZoneId()).toLocalTime()

        var queueNo:Int=0
        db.collection("Check In").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (checkin in it.documents) {
                        val checkinDate = checkin.data?.get("check_in_date").toString()
                        val appID = checkin.data?.get("appointment_ID").toString()
                        if(checkinDate==ldToday.format(dateFormat)){
                            Log.d(TAG, "appID is "+appID)
                            val refApp = db.collection("Appointment").document(appID)
                            refApp.get().addOnSuccessListener {
                                if (it != null) {
                                    val aAppEndTime = it.data?.get("appointment_end_time").toString()
                                    Log.d(TAG, "end time is "+aAppEndTime)
                                    val appEndTime = LocalTime.parse(aAppEndTime, timeFormat)
                                    if(appEndTime.compareTo(ltNow)>0){
                                        queueNo++
                                        Log.d(TAG, "queue no: "+queueNo)
                                        tvQueueNoC.text = queueNo.toString()
                                    }
                                }
                            }.addOnFailureListener { Log.d(TAG, "failed retrieve app") }
                        }
                    }
                    tvQueueNoC.text = queueNo.toString()
                }
            }.addOnFailureListener { Log.d(TAG, "failed retrieve checkin") }
    }

    private fun refreshPatientNo() {
        var patientNo:Int=0
        db.collection("Patient").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (patient in it.documents) {
                        patientNo++
                    }
                    tvPatientDetail.text = "There are total of "+ patientNo.toString()+" patient(s) records currently"
                }
            }.addOnFailureListener { Log.d(TAG, "failed to retrieve patient") }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!) //stop handler when activity not visible super.onPause();
    }

}