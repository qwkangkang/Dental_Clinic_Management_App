package com.kqw.dcm.AccountSetting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import kotlinx.android.synthetic.main.change_password.*
import kotlinx.android.synthetic.main.email_verification.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.title_bar.btnBack
import kotlinx.android.synthetic.main.title_bar.btnLogout
import kotlinx.android.synthetic.main.title_bar.tvTitle

class Email_Verification:AppCompatActivity() {
    companion object{
        val TAG:String = Email_Verification::class.java.simpleName
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
        setContentView(R.layout.email_verification)

        //init setting
        ibProfile.setImageResource(R.drawable.user_orange)
        ibProfileC.setImageResource(R.drawable.user_orange)
        tvTitle.text = "Reset Password"

        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, "")!!
        sp_role = sharedPreferences.getString(ROLE_KEY, "")!!



        Log.d(TAG, "role is "+sp_role)
        if(sp_role=="Doctor"||sp_role=="Assistant"){
            mnPatientOTP.visibility = View.INVISIBLE
        }
        else if(sp_role=="Patient"){
            mnClinicOTP.visibility = View.INVISIBLE
        }
        else{
            mnPatientOTP.visibility = View.INVISIBLE
            mnClinicOTP.visibility = View.INVISIBLE
        }

        val bundle: Bundle? = intent.extras

        //variables
        var otp:String?=null

        bundle?.let {
            otp = bundle.getString("msgOTP")
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
            this.finish()
            overridePendingTransition(0, 0)
        }

        btnVerifyOTP.setOnClickListener {

            if(etOTP.text.toString()==otp){
                val intent = Intent(this, Change_Pw::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }else{
                Toast.makeText(this, "Entered OTP Is Wrong", Toast.LENGTH_SHORT).show()
            }

        }


        //menu bar button
        ibHome.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        ibApp.setOnClickListener{
            val intent = Intent(this, Appointment_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
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
            val intent = Intent(this, Account_Setting_Clinic::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}