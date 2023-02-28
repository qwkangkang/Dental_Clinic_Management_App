package com.kqw.dcm.Home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.FAQ.FAQ_List
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.schedule.Schedule
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.cvAppointment
import kotlinx.android.synthetic.main.activity_main.cvFAQ
import kotlinx.android.synthetic.main.activity_main.ibLogout
import kotlinx.android.synthetic.main.activity_main_clinic.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_clinic)
        ibHomeC.setImageResource(R.drawable.home_orange)

        //variables


        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!
        Toast.makeText(this, sp_role, Toast.LENGTH_SHORT).show()
        Log.d(Login.TAG, sp_role)

        //retrieve user's fname
        val ref = db.collection("User").document(sp_uid.toString())
        ref.get().addOnSuccessListener {
            if (it != null) {
                val fname = it.data?.get("user_first_name").toString()
                tvHelloClinic.text = "Hello, $fname"
            }
        }


        //buttons to next activities
        ibLogout.setOnClickListener {
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
            val intent = Intent(this, Schedule::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        //menu bar button
        ibHomeC.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
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