package com.kqw.dcm.Consultation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.*
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.FAQ.FAQListAdapter
import com.kqw.dcm.FAQ.FAQ_Data
import com.kqw.dcm.FAQ.FAQ_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Schedule
import kotlinx.android.synthetic.main.consultation_list.*
import kotlinx.android.synthetic.main.consultation_list.ibAdd
import kotlinx.android.synthetic.main.faq_list.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.title_bar.*

class Consultation_List: AppCompatActivity () {
    companion object {
        val TAG: String = Consultation_List::class.java.simpleName
    }

    private var db = Firebase.firestore
    lateinit var sharedPreferences: SharedPreferences
    var PREFS_KEY = "prefs"
    var USERID_KEY = "uid"
    var ROLE_KEY = "role"
    var sp_uid = ""
    var sp_role = ""
    private lateinit var conList: ArrayList<Consultation_Data>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.consultation_list)

        //init setting
        ibConsult.setImageResource(R.drawable.consult_orange)
        ibConsultC.setImageResource(R.drawable.consult_orange)
        tvTitle.text = "Consultation"
        btnBack.visibility = View.INVISIBLE

        //variables
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!


        if (sp_role == "Doctor" || sp_role == "Assistant") {
            mnPatientCon.visibility = View.INVISIBLE
            ibAdd.visibility = View.INVISIBLE
        } else {
            mnClinicCon.visibility = View.INVISIBLE
        }

        rvConsultation.layoutManager = LinearLayoutManager(this)
        conList = arrayListOf()

        db = FirebaseFirestore.getInstance()

        if(sp_role=="Patient"){
            db.collection("Patient").get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        for (patient in it.documents) {
                            val userID = patient.get("user_ID").toString()
                            if (userID == sp_uid) {
                                val patientID = patient.get("patient_ID").toString()

                                db.collection("Consultation").get()
                                    .addOnSuccessListener {
                                        if (!it.isEmpty) {
                                            for (con in it.documents) {
                                                if (patientID == con.get("patient_ID").toString()) {
                                                    val conID = con.get("consultation_ID").toString()
                                                    val conQues = con.get("consultation_question").toString()
                                                    val conDate = con.get("consultation_date").toString()
                                                    val conTime = con.get("consultation_time").toString()
                                                    val conStatus = con.get("consultation_status").toString()
                                                    val con = Consultation_Data(conID, conQues, conDate, conTime, conStatus)
                                                    Log.d(TAG, "con id=>"+conID)
                                                    Log.d(TAG, "con ques=>"+conQues)
                                                    if (con != null) {
                                                        conList.add(con)
                                                    }
                                                    rvConsultation.adapter =
                                                        ConsultationListAdapter(conList)
                                                }
                                            }
                                        }
                                    }
                                    .addOnFailureListener { Log.d(TAG, "failed retrieve Consultation") }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "failed retrieve patient")
                }
        }else{


                                    db.collection("Consultation").get()
                                        .addOnSuccessListener {
                                            if (!it.isEmpty) {
                                                for (con in it.documents) {
                                                    if (con.get("consultation_status").toString() == "Unsolved"||con.get("consultation_status").toString() == "Replied") {
                                                        val conID = con.get("consultation_ID").toString()
                                                        val conQues = con.get("consultation_question").toString()
                                                        val conDate = con.get("consultation_date").toString()
                                                        val conTime = con.get("consultation_time").toString()
                                                        val conStatus = con.get("consultation_status").toString()
                                                        val con = Consultation_Data(conID, conQues, conDate, conTime, conStatus)
                                                        Log.d(TAG, "con id=>"+conID)
                                                        Log.d(TAG, "con ques=>"+conQues)
                                                        if (con != null) {
                                                            conList.add(con)
                                                        }
                                                        rvConsultation.adapter =
                                                            ConsultationListAdapter(conList)
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener { Log.d(TAG, "failed retrieve Consultation") }

            }




        ibAdd.setOnClickListener {
            val intent = Intent(this, Create_Consultation::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, Login::class.java)
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
                        val intent = Intent(this, Schedule::class.java)
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