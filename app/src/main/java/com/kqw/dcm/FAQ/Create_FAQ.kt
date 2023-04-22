package com.kqw.dcm.FAQ

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.*
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.AccountSetting.Register
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import kotlinx.android.synthetic.main.create_faq.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.title_bar.*

class Create_FAQ:AppCompatActivity() {
    companion object{
        val TAG:String = FAQ_List::class.java.simpleName
    }

    private var db = Firebase.firestore
    lateinit var sharedPreferences: SharedPreferences
    var PREFS_KEY = "prefs"
    var USERID_KEY = "uid"
    var ROLE_KEY = "role"
    var sp_uid = ""
    var sp_role = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_faq)
        ibHomeC.setImageResource(R.drawable.home_orange)

        //init setting
        tvTitle.text = "FAQ"
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!

        //variables
        var action :String?=null
        var faqID: String?=null
        val bundle: Bundle? = intent.extras

        bundle?.let {
            action = bundle.getString("edit_message")
            faqID = bundle.getString("faqID_message")
        }

        if(action=="Edit"){
            btnSubmit.text = "Save"
            val refFAQ = db.collection("FAQ").document(faqID.toString())
            refFAQ.get().addOnSuccessListener {
                if (it != null) {
                    val faqQues = it.data?.get("FAQ_question").toString()
                    val faqAns = it.data?.get("FAQ_answer").toString()

                    etQues.setText(faqQues)
                    etAns.setText(faqAns)
                }
            }.addOnFailureListener {
                Log.w(TAG, "failed retrieve faq")
            }
        }else{
            btnDeleteFAQ.visibility = View.INVISIBLE
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

        btnSubmit.setOnClickListener {
            var faqQues:String?=null
            var faqAns:String?=null
            faqQues = etQues.text.toString()
            faqAns = etAns.text.toString()

            if(action=="Edit"){
                val updateFAQMap = mapOf(
                    "FAQ_question" to faqQues,
                    "FAQ_answer" to faqAns
                )

                db.collection("FAQ").document(faqID.toString()).update(updateFAQMap)
                finish()
            }else{
                db = FirebaseFirestore.getInstance()
                db.collection("Doctor").get()
                    .addOnSuccessListener {
                        if (!it.isEmpty) {
                            for (doctor in it.documents) {
                                val userID = doctor.get("user_ID").toString()
                                if(userID==sp_uid) {
                                    val docID = doctor.get("doctor_ID").toString()
                                    val faqID = db.collection("collection_name").document().id
                                    val faqMap = hashMapOf(
                                        "FAQ_ID" to faqID,
                                        "FAQ_question" to faqQues,
                                        "FAQ_answer" to faqAns,
                                        "doctor_ID" to docID
                                    )
                                    db.collection("FAQ").document(faqID)
                                        .set(faqMap)
                                        .addOnSuccessListener { Log.d(TAG, "Success") }
                                        .addOnFailureListener { e -> Log.w(TAG, "Error") }
                                    val intent = Intent(this, FAQ_List::class.java)
                                    startActivity(intent)
                                    overridePendingTransition(0, 0)

                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                            e -> Log.w(TAG, "failed retrieve doctor")
                    }
            }


        }
        btnDeleteFAQ.setOnClickListener {
            db.collection("FAQ").document(faqID.toString()).delete()
            Toast.makeText(this, "FAQ is deleted successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, FAQ_List::class.java)
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
            val intent = Intent(this, Account_Setting::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}