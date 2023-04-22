package com.kqw.dcm.Home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.FAQ.FAQ_List
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Appointment.Appointment_Data
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_Data
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.cvAppointment
import kotlinx.android.synthetic.main.activity_main.cvFAQ
import kotlinx.android.synthetic.main.activity_main.ibLogout
import kotlinx.android.synthetic.main.activity_main_clinic.*
import kotlinx.android.synthetic.main.appointment_list.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.treatment_history_list.*
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
    private lateinit var appList: ArrayList<Appointment_Data>
    private lateinit var treatmentList: ArrayList<Treatment_Data>
    var today: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
    @RequiresApi(Build.VERSION_CODES.O)
    var ldToday: LocalDate = LocalDateTime.ofInstant(today.toInstant(), today.getTimeZone().toZoneId()).toLocalDate()
    @RequiresApi(Build.VERSION_CODES.O)
    val ltNow: LocalTime = LocalDateTime.ofInstant(today.toInstant(), today.getTimeZone().toZoneId()).toLocalTime()

    @RequiresApi(Build.VERSION_CODES.O)
    val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    @RequiresApi(Build.VERSION_CODES.O)
    val timeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ibHome.setImageResource(R.drawable.home_orange)

        //init
        tvViewMore.visibility = View.INVISIBLE
        tvViewMore2.visibility = View.INVISIBLE

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
        refreshLastApp()
        refreshLastTreatment()

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
            refreshLastApp()
            refreshLastTreatment()


        }.also { runnable = it }, delay)
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshQueueNo() {

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshLastApp(){
        var pPatientID:String?=null
        appList = arrayListOf()
        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        db = FirebaseFirestore.getInstance()
        db.collection("Patient").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (patient in it.documents) {
                        val pUserID = patient.get("user_ID").toString()
                        if (pUserID == sp_uid) {
                            pPatientID = patient.get("patient_ID").toString()
                            db.collection("Appointment").get()
                                .addOnSuccessListener {
                                    if (!it.isEmpty) {
                                        for (appointment in it.documents) {
                                            val aPatientID =
                                                appointment.get("patient_ID").toString()
                                            if (aPatientID == pPatientID) {

                                                val appID =
                                                    appointment.get("appointment_ID").toString()
                                                val service =
                                                    appointment.get("appointment_service")
                                                        .toString()
                                                val appDate =
                                                    appointment.get("appointment_date")
                                                        .toString()
                                                val appStartTime =
                                                    appointment.get("appointment_start_time")
                                                        .toString()
                                                val status =
                                                    appointment.get("appointment_status")
                                                        .toString()
                                                val room =
                                                    appointment.get("room_ID")
                                                        .toString()
                                                val scheduleID =
                                                    appointment.get("schedule_ID").toString()
                                                val cancelReason = appointment.get("appointment_cancel_reason").toString()
                                                val refSchedule = db.collection("Schedule")
                                                    .document(scheduleID)
                                                refSchedule.get().addOnSuccessListener {
                                                    if (it != null) {
                                                        val doctorID =
                                                            it.data?.get("doctor_ID").toString()
                                                        val refDoc = db.collection("Doctor")
                                                            .document(doctorID)
                                                        refDoc.get().addOnSuccessListener {
                                                            if (it != null) {
                                                                val userID = it.data?.get("user_ID")
                                                                    .toString()
                                                                val refUser =
                                                                    db.collection("User")
                                                                        .document(userID!!)
                                                                refUser.get()
                                                                    .addOnSuccessListener {
                                                                        if (it != null) {
                                                                            val doctorName =
                                                                                it.data?.get("user_first_name")
                                                                                    .toString() + " " + it.data?.get(
                                                                                    "user_last_name"
                                                                                ).toString()
                                                                            val app =
                                                                                Appointment_Data(
                                                                                    appID,
                                                                                    aPatientID,
                                                                                    doctorName,
                                                                                    service,
                                                                                    appDate,
                                                                                    appStartTime,
                                                                                    status,
                                                                                    room,
                                                                                    cancelReason
                                                                                )
                                                                            if (app != null) {
                                                                                appList.add(app)
                                                                            }
                                                                            val result = appList.sortedByDescending {
                                                                                LocalDate.parse(it.appDate, dateFormat)
                                                                            }

                                                                            val ldAppDate = LocalDate.parse(result[0].appDate, dateFormat)
                                                                            val ltAppTime = LocalTime.parse(result[0].appStartTime, timeFormat)
                                                                            if(ldAppDate.compareTo(ldToday)>=0){

                                                                                if(ldAppDate.compareTo(ldToday)==0&&ltAppTime.compareTo(ltNow)<0){
                                                                                    tvAppDetailP.text = "There Is No Upcoming Appointment"
                                                                                    tvViewMore2.visibility = View.INVISIBLE
                                                                                }else{
                                                                                    tvAppDetailP.text = "Dr. "+result[0].docName+"\n"+result[0].appDate+
                                                                                            "  "+result[0].appStartTime
                                                                                    tvViewMore2.visibility = View.VISIBLE
                                                                                }
                                                                            }else{
                                                                                tvAppDetailP.text = "There Is No Upcoming Appointment"
                                                                                tvViewMore2.visibility = View.INVISIBLE
                                                                            }

                                                                        }
                                                                    }
                                                                    .addOnFailureListener {
                                                                        Log.d(
                                                                            Appointment_List.TAG,
                                                                            "retrieve user failed"
                                                                        )
                                                                    }
                                                            }
                                                        }
                                                            .addOnFailureListener {
                                                                Log.d(
                                                                    Appointment_List.TAG,
                                                                    "retrieve doctor failed"
                                                                )
                                                            }
                                                    }
                                                }
                                                    .addOnFailureListener {
                                                        Log.d(
                                                            Appointment_List.TAG,
                                                            "retrieve schedule failed"
                                                        )
                                                    }
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Log.d(Appointment_List.TAG, "retrieve appointment failed")
                                }

                        }
                    }
                }
            }.addOnFailureListener { Log.d(Appointment_List.TAG, "failed retrieve user") }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshLastTreatment(){
        treatmentList = arrayListOf()
        db = FirebaseFirestore.getInstance()
        var userID:String?=null
        db.collection("Patient").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (patient in it.documents) {
                        val pUserID = patient.get("user_ID").toString()
                        if (pUserID == sp_uid) {
                            val pPatient_ID = patient.get("patient_ID").toString()
                            db.collection("Treatment").get()
                                .addOnSuccessListener {
                                    if (!it.isEmpty) {
                                        for (treatment in it.documents) {
                                            val tPatientID =
                                                treatment.get("patient_ID").toString()
                                            if (tPatientID == pPatient_ID) {
                                                val treatmentID =
                                                    treatment.get("treatment_ID").toString()
                                                val treatmentName =
                                                    treatment.get("treatment_name").toString()
                                                val treatmentDate =
                                                    treatment.get("treatment_date").toString()
                                                val treatmentTime =
                                                    treatment.get("treatment_time").toString()
                                                val treatmentDocID =
                                                    treatment.get("doctor_ID").toString()
//                                                val treatmentPatientID =
//                                                    treatment.get("patient_ID").toString()
                                                val treatmentRemark =
                                                    treatment.get("treatment_remark").toString()
                                                val treatmentPres =
                                                    treatment.get("treatment_prescription")
                                                        .toString()
                                                val treatmentDetail =
                                                    treatment.get("treatment_detail").toString()

                                                val refDoc = db.collection("Doctor")
                                                    .document(treatmentDocID)
                                                refDoc.get().addOnSuccessListener {
                                                    if (it != null) {
                                                        userID =
                                                            it.data?.get("user_ID").toString()
                                                        val refUser = db.collection("User")
                                                            .document(userID!!)
                                                        refUser.get().addOnSuccessListener {
                                                            if (it != null) {
                                                                val doctorName =
                                                                    it.data?.get("user_first_name")
                                                                        .toString() + " " + it.data?.get(
                                                                        "user_last_name"
                                                                    )
                                                                        .toString()
                                                                val treatment = Treatment_Data(
                                                                    treatmentID,
                                                                    //patientID.toString(),
                                                                    tPatientID,
                                                                    doctorName,
                                                                    treatmentName,
                                                                    treatmentDate,
                                                                    treatmentTime,
                                                                    treatmentRemark,
                                                                    treatmentPres,
                                                                    treatmentDetail
                                                                )

                                                                if (treatment != null) {
                                                                    treatmentList.add(treatment)
                                                                }
                                                                val result = treatmentList.sortedByDescending {
                                                                    LocalDate.parse(it.treatmentDate, dateFormat)
                                                                }
                                                                if(result!=null){
                                                                    tvTreatmentDetail.text = result[0].treatmentDate+"\t\t\t\t"+result[0].treatmentName
                                                                    tvViewMore.visibility = View.VISIBLE
                                                                }else{
                                                                    tvTreatmentDetail.text = "There Is No Treatment History"
                                                                    tvViewMore.visibility = View.INVISIBLE
                                                                }

                                                            }
                                                        }.addOnFailureListener {
                                                            Log.d(
                                                                Treatment_History_List.TAG,
                                                                "failed retrieve user"
                                                            )
                                                        }
                                                    }
                                                }.addOnFailureListener {
                                                    Log.d(
                                                        Treatment_History_List.TAG,
                                                        "failed retrieve doctor"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }.addOnFailureListener {
                                    Log.d(
                                        Treatment_History_List.TAG,
                                        "failed retrieve treatment"
                                    )
                                }
                        }
                    }
                }
            }.addOnFailureListener { Log.d(Treatment_History_List.TAG, "failed retrieve patient") }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!) //stop handler when activity not visible super.onPause();
    }

}