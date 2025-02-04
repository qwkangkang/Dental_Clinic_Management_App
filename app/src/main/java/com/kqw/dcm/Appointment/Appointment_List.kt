package com.kqw.dcm.Appointment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.*
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.appointment_list.*
import kotlinx.android.synthetic.main.appointment_list.mnClinic
import kotlinx.android.synthetic.main.appointment_list.mnPatient
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.patient_list.*
import kotlinx.android.synthetic.main.schedule.*
import kotlinx.android.synthetic.main.title_bar.*
import kotlinx.android.synthetic.main.treatment_history_list.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Appointment_List: AppCompatActivity() {
    companion object {
        val TAG: String = Appointment_List::class.java.simpleName
    }

    private var db = Firebase.firestore
    lateinit var sharedPreferences: SharedPreferences
    var PREFS_KEY = "prefs"
    var USERID_KEY = "uid"
    var ROLE_KEY = "role"
    var sp_uid = ""
    var sp_role = ""
    private lateinit var appList: ArrayList<Appointment_Data>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.appointment_list)

        //init setting
        ibApp.setImageResource(R.drawable.appointment_orange)

        tvTitle.text = "Appointment"
        //btnBack.visibility = View.INVISIBLE


        //variables
        var patientID:String?=null
        var userID:String?=null
        var patientName:String?=null
        var pPatientID:String?=null
        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!


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

        if (sp_role=="Doctor"||sp_role=="Assistant"){

            if(patientID!=null){
                ibPatientC.setImageResource(R.drawable.patient_orange)
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
                                Log.d(Appointment_List.TAG, "retrieve user failed")
                            }
                    }
                }.addOnFailureListener {
                    Log.d(Appointment_List.TAG, "retrieve patient failed")
                }
            }else{
                ibHomeC.setImageResource(R.drawable.home_orange)
                tvPatientName.text = ""
                btnAddAppointment.visibility = View.GONE
//                cvAppList.setLayoutParams(RelativeLayout.LayoutParams(CardView., 500));
            }
        }
        else{
            tvPatientName.text = ""
        }


        rvAppointment.layoutManager = LinearLayoutManager(this)
        appList = arrayListOf()

        db = FirebaseFirestore.getInstance()

        //from patient list
        if(patientID!=null){
            if(sp_role=="Doctor"||sp_role=="Assistant"){
                db.collection("Appointment").get()
                    .addOnSuccessListener {
                        if (!it.isEmpty) {
                            for (appointment in it.documents) {
                                val aPatientID = appointment.get("patient_ID").toString()
                                if(aPatientID==patientID){

                                    val appID = appointment.get("appointment_ID").toString()

                                    val service = appointment.get("appointment_service").toString()
                                    val appDate = appointment.get("appointment_date").toString()
                                    val appStartTime = appointment.get("appointment_start_time").toString()
                                    val status = appointment.get("appointment_status").toString()
                                    val room = appointment.get("room_ID").toString()
                                    val scheduleID = appointment.get("schedule_ID").toString()
                                    val cancelReason = appointment.get("appointment_cancel_reason").toString()
                                    Log.d(Appointment_List.TAG, "schedule id =>"+scheduleID)
                                    val refSchedule = db.collection("Schedule").document(scheduleID)
                                    refSchedule.get().addOnSuccessListener {
                                        if (it != null) {
                                            val doctorID = it.data?.get("doctor_ID").toString()

                                            Log.d(Appointment_List.TAG, "doc id =>"+doctorID)
                                            val refDoc = db.collection("Doctor").document(doctorID)
                                            refDoc.get().addOnSuccessListener {
                                                if (it != null) {
                                                    userID = it.data?.get("user_ID").toString()
                                                    Log.d(Appointment_List.TAG, "user id =>"+userID)
                                                    val refUser = db.collection("User").document(userID!!)
                                                    refUser.get().addOnSuccessListener {
                                                        if (it != null) {
                                                            val doctorName = it.data?.get("user_first_name").toString()+" "+it.data?.get("user_last_name").toString()
                                                            Log.d(Appointment_List.TAG, "doc name =>"+doctorName)
                                                            val app = Appointment_Data(appID, aPatientID, doctorName, service, appDate, appStartTime, status, room, cancelReason)

                                                            if(app!=null){
                                                                appList.add(app)
                                                            }
                                                            val result = appList.sortedByDescending {
                                                                LocalDate.parse(it.appDate, dateFormat)
                                                            }
                                                            rvAppointment.adapter = AppointmentListAdapter(result)
                                                        }
                                                    }
                                                        .addOnFailureListener {
                                                            Log.d(Appointment_List.TAG, "retrieve user failed")
                                                        }
                                                }
                                            }
                                                .addOnFailureListener {
                                                    Log.d(Appointment_List.TAG, "retrieve doctor failed")
                                                }
                                        }
                                    }
                                        .addOnFailureListener {
                                            Log.d(Appointment_List.TAG, "retrieve schedule failed")
                                        }
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Log.d(Appointment_List.TAG, "retrieve appointment failed")
                    }
            }else {
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
                                                                        userID = it.data?.get("user_ID")
                                                                            .toString()
                                                                        Log.d(
                                                                            Appointment_List.TAG,
                                                                            "user id =>" + userID
                                                                        )
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
                                                                                    rvAppointment.adapter = AppointmentListAdapter(result)
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
                    }.addOnFailureListener { Log.d(TAG, "failed retrieve user") }
            }
        }
        else{
            if(sp_role=="Doctor"||sp_role=="Assistant"){
                db.collection("Appointment").get()
                    .addOnSuccessListener {
                        if (!it.isEmpty) {
                            for (appointment in it.documents) {
                                val aPatientID = appointment.get("patient_ID").toString()
                                    val appID = appointment.get("appointment_ID").toString()

                                    val service = appointment.get("appointment_service").toString()
                                    val appDate = appointment.get("appointment_date").toString()
                                    val appStartTime = appointment.get("appointment_start_time").toString()
                                    val status = appointment.get("appointment_status").toString()
                                    val room = appointment.get("room_ID").toString()
                                    val scheduleID = appointment.get("schedule_ID").toString()
                                    val cancelReason = appointment.get("appointment_cancel_reason").toString()
                                    Log.d(Appointment_List.TAG, "schedule id =>"+scheduleID)
                                    val refSchedule = db.collection("Schedule").document(scheduleID)
                                    refSchedule.get().addOnSuccessListener {
                                        if (it != null) {
                                            val doctorID = it.data?.get("doctor_ID").toString()

                                            Log.d(Appointment_List.TAG, "doc id =>"+doctorID)
                                            val refDoc = db.collection("Doctor").document(doctorID)
                                            refDoc.get().addOnSuccessListener {
                                                if (it != null) {
                                                    userID = it.data?.get("user_ID").toString()
                                                    Log.d(Appointment_List.TAG, "user id =>"+userID)
                                                    val refUser = db.collection("User").document(userID!!)
                                                    refUser.get().addOnSuccessListener {
                                                        if (it != null) {
                                                            val doctorName = it.data?.get("user_first_name").toString()+" "+it.data?.get("user_last_name").toString()
                                                            Log.d(Appointment_List.TAG, "doc name =>"+doctorName)
                                                            val app = Appointment_Data(appID, aPatientID, doctorName, service, appDate, appStartTime, status, room, cancelReason)

                                                            if(app!=null){
                                                                appList.add(app)
                                                            }
                                                            val result = appList.sortedByDescending {
                                                                LocalDate.parse(it.appDate, dateFormat)
                                                            }
                                                            rvAppointment.adapter = AppointmentListAdapter(result)

                                                        }
                                                    }
                                                        .addOnFailureListener {
                                                            Log.d(Appointment_List.TAG, "retrieve user failed")
                                                        }
                                                }
                                            }
                                                .addOnFailureListener {
                                                    Log.d(Appointment_List.TAG, "retrieve doctor failed")
                                                }
                                        }
                                    }
                                        .addOnFailureListener {
                                            Log.d(Appointment_List.TAG, "retrieve schedule failed")
                                        }
                                }

                        }
                    }
                    .addOnFailureListener {
                        Log.d(Appointment_List.TAG, "retrieve appointment failed")
                    }
            }else {
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
                                                                        userID = it.data?.get("user_ID")
                                                                            .toString()
                                                                        Log.d(
                                                                            Appointment_List.TAG,
                                                                            "user id =>" + userID
                                                                        )
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
                                                                                    rvAppointment.adapter = AppointmentListAdapter(result)
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
                    }.addOnFailureListener { Log.d(TAG, "failed retrieve user") }
            }
        }


        btnAddAppointment.setOnClickListener {

            val intent = Intent(this, Create_Appointment::class.java)
            if(sp_role=="Doctor"||sp_role=="Assistant"){
                intent.putExtra("msgPatientID", patientID)
            }else{
                intent.putExtra("msgPatientID", pPatientID)
            }
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnLogout.setOnClickListener {
            val editor:SharedPreferences.Editor = sharedPreferences.edit()
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
}