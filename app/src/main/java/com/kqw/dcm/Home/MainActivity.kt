package com.kqw.dcm.Home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.FAQ.FAQ_List
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.R
import com.kqw.dcm.schedule.Schedule
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.cvAppointment
import kotlinx.android.synthetic.main.activity_main.cvFAQ
import kotlinx.android.synthetic.main.activity_main.ibLogout
import kotlinx.android.synthetic.main.activity_main_clinic.*
import kotlinx.android.synthetic.main.menu_bar.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object{
        val TAG:String = MainActivity::class.java.simpleName
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
    val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ibHome.setImageResource(R.drawable.home_orange)

        //variables
        var patientID:String?=null

        db.collection("Patient").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (patient in it.documents) {
                        if (sp_uid == patient.get("user_ID").toString()) {
                            patientID = patient.get("patient_ID").toString()
                        }
                    }
                }
            }.addOnFailureListener { Log.d(TAG, "Failed retrieve patient") }


        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!


        //retrieve user's fname
        val ref = db.collection("User").document(sp_uid.toString())
        ref.get().addOnSuccessListener {
            if (it != null) {
                val fname = it.data?.get("user_first_name").toString()
                tvHello.text = "Hello, $fname"
            }
        }
        refreshQueueNo()

        //buttons to next activities
        ibLogout.setOnClickListener {
            val editor:SharedPreferences.Editor = sharedPreferences.edit()
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

        cvTreatment.setOnClickListener {
            val intent = Intent(this, Treatment_History_List::class.java)
            intent.putExtra("msgPatientID", patientID)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        cvFAQ.setOnClickListener {
            val intent = Intent(this, FAQ_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        //menu bar button
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
            intent.putExtra("msgPatientID", patientID)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        ibProfile.setOnClickListener {
            val intent = Intent(this, Account_Setting::class.java)
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
//            Toast.makeText(
//                this, "This method is run every 10 seconds",
//                Toast.LENGTH_SHORT
//            ).show()
            refreshQueueNo()


        }.also { runnable = it }, delay)
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshQueueNo() {
        var today: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
        var ldToday: LocalDate = LocalDateTime.ofInstant(today.toInstant(), today.getTimeZone().toZoneId()).toLocalDate()
        Log.d(MainActivity_Clinic.TAG, "today is "+ldToday)
        val ltNow: LocalTime = LocalDateTime.ofInstant(today.toInstant(), today.getTimeZone().toZoneId()).toLocalTime()

        var queueNo:Int=0
        db.collection("Check In").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (checkin in it.documents) {
                        val checkinDate = checkin.data?.get("check_in_date").toString()
                        val appID = checkin.data?.get("appointment_ID").toString()
                        if(checkinDate==ldToday.format(dateFormat)){
                            Log.d(MainActivity_Clinic.TAG, "appID is "+appID)
                            val refApp = db.collection("Appointment").document(appID)
                            refApp.get().addOnSuccessListener {
                                if (it != null) {
                                    val aAppEndTime = it.data?.get("appointment_end_time").toString()
                                    Log.d(MainActivity_Clinic.TAG, "end time is "+aAppEndTime)
                                    val appEndTime = LocalTime.parse(aAppEndTime, timeFormat)
                                    if(appEndTime.compareTo(ltNow)>0){
                                        queueNo++
                                        Log.d(MainActivity_Clinic.TAG, "queue no: "+queueNo)
                                        tvQueueNoC.text = queueNo.toString()
                                    }
                                }
                            }.addOnFailureListener { Log.d(MainActivity_Clinic.TAG, "failed retrieve app") }
                        }
                    }
                    tvQueueNo.text = queueNo.toString()
                }
            }.addOnFailureListener { Log.d(MainActivity_Clinic.TAG, "failed retrieve checkin") }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!) //stop handler when activity not visible super.onPause();
    }

}