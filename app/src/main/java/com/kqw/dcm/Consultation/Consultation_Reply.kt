package com.kqw.dcm.Consultation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Schedule
import kotlinx.android.synthetic.main.consultation_list.*
import kotlinx.android.synthetic.main.consultation_reply.*
import kotlinx.android.synthetic.main.create_consultation.*
import kotlinx.android.synthetic.main.create_faq.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.title_bar.*
import java.time.format.DateTimeFormatter

class Consultation_Reply : AppCompatActivity() {
    companion object {
        val TAG: String = Consultation_Reply::class.java.simpleName
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
        setContentView(R.layout.consultation_reply)

        //init setting
        ibConsult.setImageResource(R.drawable.consult_orange)
        ibConsultC.setImageResource(R.drawable.consult_orange)
        tvTitle.text = "Consultation"
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!

        if (sp_role == "Doctor" || sp_role == "Assistant") {
            mnPatientConR.visibility = View.INVISIBLE
        } else {
            mnClinicConR.visibility = View.INVISIBLE
        }

        //variable
        var conID:String?=null
        val bundle: Bundle? = intent.extras

        bundle?.let {
            conID = bundle.getString("conID_message")
        }

        val refCon = db.collection("Consultation").document(conID.toString())
        refCon.get().addOnSuccessListener {
            if (it != null) {
                if (conID == it.data?.get("consultation_ID").toString()) {
                    val conDate = it.data?.get("consultation_date").toString()
                    val conTime = it.data?.get("consultation_time").toString()
                    val conQues = it.data?.get("consultation_question").toString()
                    val conStatus = it.data?.get("consultation_status").toString()
                    val conAns = it.data?.get("consultation_answer").toString()
                    val conPatientID = it.data?.get("patient_ID").toString()

                    val refPatient = db.collection("Patient").document(conPatientID)
                    refPatient.get().addOnSuccessListener {
                        if (it != null) {
                            val userID = it.data?.get("user_ID").toString()

                            val refUser = db.collection("User").document(userID)
                            refUser.get().addOnSuccessListener {
                                if (it != null) {
                                    val patientName = it.data?.get("user_first_name")
                                        .toString() + " " + it.data?.get("user_last_name")
                                        .toString()
                                    tvPatientName.text = patientName
                                }
                            }.addOnFailureListener { Log.d(TAG, "failed retrieve user") }
                        }
                    }.addOnFailureListener { Log.d(TAG, "failed retrieve patient") }


                    tvConDateTime.text = conDate+" "+conTime
                    etConQues.setText(conQues)
                    if(conAns!=""){
                        etConReply.setText(conAns)
                    }

                    if(sp_role=="Patient"){
                        etConReply.isEnabled = false
                        if(conAns==""){
                            tvAnswerFrmDoc.visibility = View.INVISIBLE
                            etConReply.visibility = View.INVISIBLE
                            tvSolved.visibility = View.INVISIBLE
                            btnSolved.visibility = View.INVISIBLE
                        }
                        if(conStatus=="Solved"){
                            tvSolved.visibility = View.INVISIBLE
                            btnSolved.visibility = View.INVISIBLE
                        }
                    }else if(sp_role=="Doctor"){
                        etConReply.isEnabled = true
                        tvSolved.visibility = View.INVISIBLE
                        btnSolved.text = "Submit"
                    }
                }
            }
        }
            .addOnFailureListener { Log.d(TAG, "failed retrieve consultation") }

        btnSolved.setOnClickListener {
            if(sp_role=="Doctor"){
                val updateConMap = mapOf(
                    "consultation_answer" to etConReply.text.toString(),
                    "consultation_status" to "Replied"
                )
                db.collection("Consultation").document(conID.toString()).update(updateConMap)

                val intent = Intent(this, Consultation_List::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }else if(sp_role=="Patient"){
                val updateConMap = mapOf(
                    "consultation_status" to "Solved"
                )
                db.collection("Consultation").document(conID.toString()).update(updateConMap)

                val intent = Intent(this, Consultation_List::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }
        btnBack.setOnClickListener {
            finish()
        }
        tvConDateTime.setOnClickListener{
            val date = FieldValue.serverTimestamp()
            val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")

            Log.d(TAG, "test date=>"+date.toString())
        }

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
