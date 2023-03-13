package com.kqw.dcm.Consultation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kqw.dcm.*
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.FAQ.Create_FAQ
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Schedule
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.create_consultation.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.title_bar.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Create_Consultation: AppCompatActivity() {
    companion object {
        val TAG: String = Create_Consultation::class.java.simpleName
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
        setContentView(R.layout.create_consultation)
        AndroidThreeTen.init(this)

        //init setting
        ibConsult.setImageResource(R.drawable.consult_orange)
        tvTitle.text = "Consultation"
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!

        //variables
        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")
//        val getToday = LocalDateTime.now().plusHours(12).minusMinutes(8)
        val getToday = LocalDateTime.now().plusHours(8)
        val today = getToday.format(dateFormat)
//        val currentTime = LocalDateTime.now().plusHours(8).format(timeFormat)
        val currentTime = getToday.format(timeFormat)
        val date  = ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Kuala_Lumpur"))
        //Log.d(TAG, "Zone date time: "+date.format(dateFormat))

        btnSubmit.setOnClickListener {
//            Log.d(TAG, "Zone date time: "+today)
//            Log.d(TAG, "Zone time: "+currentTime)
//            Log.d(TAG, "date:"+today)
//            Log.d(TAG, "time:"+currentTime)

            val dateF: DateFormat = SimpleDateFormat("hh:mm a")
            val formatDate:String = dateF.format(Date()).toString()
            Log.d(TAG, "try:"+today)
            Log.d(TAG, "try:"+currentTime)


//            val consultationQues = etConsultQues.text.toString().trim()
//            db = FirebaseFirestore.getInstance()
//            db.collection("Patient").get()
//                .addOnSuccessListener {
//                    if (!it.isEmpty) {
//                        for (patient in it.documents) {
//                            val userID = patient.get("user_ID").toString()
//                            if (userID == sp_uid) {
//                                val patientID = patient.get("patient_ID").toString()
//                                val conID = db.collection("collection_name").document().id
//                                val conMap = hashMapOf(
//                                    "consultation_ID" to conID,
//                                    "consultation_question" to consultationQues,
//                                    "consultation_answer" to "",
//                                    "consultation_status" to "Unsolved",
//                                    "consultation_date" to today,
//                                    "consultation_time" to currentTime,
//                                    "patient_ID" to patientID,
//                                    "doctor_ID" to ""
//                                )
//                                db.collection("Consultation").document(conID)
//                                    .set(conMap)
//                                    .addOnSuccessListener { Log.d(TAG, "Success") }
//                                    .addOnFailureListener { e -> Log.w(TAG, "Error") }
////                                finish()
//                                val intent = Intent(this, Consultation_List::class.java)
//                                startActivity(intent)
//                                overridePendingTransition(0, 0)
//                            }
//                        }
//                    }
//                }.addOnFailureListener { Log.d(TAG, "failed retrieve patient") }
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
        ibHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        ibApp.setOnClickListener {
            val popupMenu = PopupMenu(this, ibApp)
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
}