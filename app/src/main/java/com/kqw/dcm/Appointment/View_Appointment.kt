package com.kqw.dcm.Appointment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import kotlinx.android.synthetic.main.appointment_list.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.title_bar.btnBack
import kotlinx.android.synthetic.main.title_bar.btnLogout
import kotlinx.android.synthetic.main.title_bar.tvTitle
import kotlinx.android.synthetic.main.view_appointment.*

class View_Appointment: AppCompatActivity() {
    companion object{
        val TAG:String = View_Appointment::class.java.simpleName
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
        setContentView(R.layout.view_appointment)

        //init setting
        tvTitle.text = "Appointment"
        ibApp.setImageResource(R.drawable.appointment_orange)

        //variables
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!
        var appID:String?=null
        var appDate:String?=null
        var appTime:String?=null
        var appService:String?=null
        var appDocName:String?=null

        val bundle: Bundle? = intent.extras

        bundle?.let {
            appID = bundle.getString("appID_message")
            appDate = bundle.getString("appDate_message")
            appTime = bundle.getString("appTime_message")
            appService = bundle.getString("appService_message")
            appDocName = bundle.getString("appDocName_message")
        }

        etAppDate.setText(appDate)
        etAppTime.setText(appTime)
        etService.setText(appService)
        etDocInCharge.setText(appDocName)


        if(sp_role=="Doctor"||sp_role=="Assistant"){
            mnPatientViewApp.visibility = View.INVISIBLE
        }
        else{
            mnClinicViewApp.visibility = View.INVISIBLE
        }


        btnBack.setOnClickListener {
            finish()
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnCancel.setOnClickListener {
//            val intent = Intent(this, Cancel_Appointment::class.java)
//            intent.putExtra("popuptitle", "Cancellation Reason")
//            startActivity(intent)

//            val layoutInflater:LayoutInflater = LayoutInflater.from(this).context.getSystemService(
//                LAYOUT_INFLATER_SERVICE
//            ) as LayoutInflater

//            val showPopUp = PopUpFragment()
//            showPopUp.show((activity as AppCompatActivity).supportFragmentManager, "showPopUp")

            var dialog = PopUpFragment()

            val args = Bundle()
            args.putString("keyAppID", appID)
            dialog.arguments = args
            dialog.show(supportFragmentManager, "customDialog")
            overridePendingTransition(0, 0)
        }

        //menu bar button
        ibHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        ibApp.setOnClickListener {
            val intent = Intent(this, Appointment_List::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
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