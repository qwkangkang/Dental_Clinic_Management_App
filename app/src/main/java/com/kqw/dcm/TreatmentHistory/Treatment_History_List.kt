package com.kqw.dcm.TreatmentHistory

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.appointment_list.*
import kotlinx.android.synthetic.main.consultation_list.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.title_bar.*
import kotlinx.android.synthetic.main.treatment_history_list.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class Treatment_History_List: AppCompatActivity() {
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
    private lateinit var treatmentList: ArrayList<Treatment_Data>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.treatment_history_list)

        //init setting
        tvTitle.text = "Treatment History"
        ibHistory.setImageResource(R.drawable.history_orange)
        ibPatientC.setImageResource(R.drawable.patient_orange)

        //variables
        var patientID: String? = null
        var userID: String? = null
        var patientName: String? = null
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!
        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")


        if (sp_role == "Doctor" || sp_role == "Assistant") {
            mnPatientTreat.visibility = View.INVISIBLE
        } else {
            mnClinicTreat.visibility = View.INVISIBLE
            btnAddTreatment.visibility = View.INVISIBLE
            tvPatientName.text = ""
        }

        val bundle: Bundle? = intent.extras

        bundle?.let {
            patientID = bundle.getString("msgPatientID")
        }


        db = FirebaseFirestore.getInstance()
        if(sp_role=="Doctor"||sp_role=="Assistant"){
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
                            Log.d(ViewNCreate_Treatment_History.TAG, "retrieve user failed")
                        }
                }
            }.addOnFailureListener {
                Log.d(ViewNCreate_Treatment_History.TAG, "retrieve patient failed")
            }
        }


        rvTreatment.layoutManager = LinearLayoutManager(this)
        treatmentList = arrayListOf()

        db = FirebaseFirestore.getInstance()


        if(sp_role=="Doctor"||sp_role=="Assistant"){
            db.collection("Treatment").get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        for (treatment in it.documents) {
                            val tPatientID = treatment.get("patient_ID").toString()
                            if (tPatientID == patientID) {
                                val treatmentID = treatment.get("treatment_ID").toString()
                                val treatmentName = treatment.get("treatment_name").toString()
                                val treatmentDate = treatment.get("treatment_date").toString()
                                val treatmentTime = treatment.get("treatment_time").toString()
                                val treatmentDocID = treatment.get("doctor_ID").toString()
                                val treatmentPatientID = treatment.get("patient_ID").toString()
                                val treatmentRemark = treatment.get("treatment_remark").toString()
                                val treatmentPres = treatment.get("treatment_prescription").toString()
                                val treatmentDetail = treatment.get("treatment_detail").toString()

                                val refDoc = db.collection("Doctor").document(treatmentDocID)
                                refDoc.get().addOnSuccessListener {
                                    if (it != null) {
                                        userID = it.data?.get("user_ID").toString()
                                        val refUser = db.collection("User").document(userID!!)
                                        refUser.get().addOnSuccessListener {
                                            if (it != null) {
                                                val doctorName = it.data?.get("user_first_name")
                                                    .toString() + " " + it.data?.get("user_last_name")
                                                    .toString()
                                                val treatment = Treatment_Data(
                                                    treatmentID,
                                                    patientID.toString(),
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
                                                rvTreatment.adapter =
                                                    TreatmentListAdapter(result)
                                            }
                                        }.addOnFailureListener { Log.d(TAG, "failed retrieve user") }
                                    }
                                }.addOnFailureListener { Log.d(TAG, "failed retrieve doctor") }
                            }
                        }
                    }
                }.addOnFailureListener { Log.d(TAG, "failed retrieve treatment") }
        }else{
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
                                                    val treatmentPatientID =
                                                        treatment.get("patient_ID").toString()
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
                                                                        patientID.toString(),
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
                                                                    rvTreatment.adapter =
                                                                        TreatmentListAdapter(result)
                                                                }
                                                            }.addOnFailureListener {
                                                                Log.d(
                                                                    TAG,
                                                                    "failed retrieve user"
                                                                )
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
                                    }.addOnFailureListener {
                                        Log.d(
                                            TAG,
                                            "failed retrieve treatment"
                                        )
                                    }
                            }
                        }
                    }
                }.addOnFailureListener { Log.d(TAG, "failed retrieve patient") }

        }


        btnLogout.setOnClickListener{
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

        btnAddTreatment.setOnClickListener {
            Log.d(TAG, "id is "+patientID)
            if(sp_role=="Doctor"){
                val intent = Intent(this, ViewNCreate_Treatment_History::class.java)
                intent.putExtra("msgPatientID", patientID)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
            else if(sp_role=="Assistant"){
                Toast.makeText(this, "Only Doctors Can Add Treatment History", Toast.LENGTH_SHORT).show()
            }
        }

        //menu bar button
        ibHome.setOnClickListener{
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
}