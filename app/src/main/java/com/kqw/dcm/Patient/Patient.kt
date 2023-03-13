package com.kqw.dcm.Patient

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.patient.*
import kotlinx.android.synthetic.main.title_bar.*

class Patient : AppCompatActivity(){
    companion object{
        val TAG:String = Patient_List::class.java.simpleName
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
        setContentView(R.layout.patient)

        //init setting
        ibPatientC.setImageResource(R.drawable.patient_orange)
        tvTitle.text = "Patient"

        //variables
        var patientID: String? =null
        var userID:String ?= null
        var patientName:String ?= null
        var email:String ?= null
        var ICNo:String ?= null
        var gender:String ?= null
        var address:String ?= null
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!

        val bundle: Bundle? = intent.extras

        bundle?.let {
            patientID = bundle.getString("user_message")
        }

        db = FirebaseFirestore.getInstance()

        val refPatient = db.collection("Patient").document(patientID.toString())
        refPatient.get().addOnSuccessListener {
                    if(it!=null) {
                        userID = it.data?.get("user_ID").toString()
                        address = it.data?.get("patient_address").toString()
                        Log.d(Patient.TAG, "1.User ID=>"+userID)
                        Log.d(Patient.TAG, "2.address=>"+address)


                        val refUser = db.collection("User").document(userID.toString())
                        refUser.get().addOnSuccessListener {
                            if(it!=null) {
                                patientName = it.data?.get("user_first_name").toString()+" "+it.data?.get("user_last_name").toString()
                                email = it.data?.get("user_email").toString()
                                gender = it.data?.get("user_gender").toString()
                                ICNo = it.data?.get("user_IC_no").toString()
                                Log.d(Patient.TAG, "3.User Name=>"+patientName)

                                tvPatientName.text = patientName
                                etEmail.setText(email)
                                etIC.setText(ICNo)
                                etGender.setText(gender)
                                etAddress.setText(address)

                                Log.d(Patient.TAG, "4.User ID2=>"+userID)
                                Log.d(Patient.TAG, "5.address2=>"+address)

                            }
                        }
                            .addOnFailureListener {
                                Log.d(Patient.TAG, "Failed retrieving User")
                            }

                    }
                }
                    .addOnFailureListener {
                        Log.d(Patient.TAG, "Failed retrieving patient")
                    }

        btnTreatment.setOnClickListener {
            val intent = Intent(this, Treatment_History_List::class.java)
//            intent.putExtra("user_message", patientID)
            intent.putExtra("msgPatientID", patientID)
            startActivity(intent)
        }

        btnAppointment.setOnClickListener {
//            val msgPatientID : String ?= patientID
            val intent = Intent(this, Appointment_List::class.java)
            intent.putExtra("msgPatientID", patientID)
            startActivity(intent)

//            val intent = Intent(this, Appointment_List::class.java)
//            intent.putExtra("user_message", patientID)
//            startActivity(intent)
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