package com.kqw.dcm.AccountSetting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.reset_password.*
import kotlinx.android.synthetic.main.title_bar.*
import kotlinx.android.synthetic.main.view_treatment.*
import java.util.*


class Reset_Pw:AppCompatActivity() {
    companion object{
        val TAG:String = Reset_Pw::class.java.simpleName
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
        setContentView(R.layout.reset_password)

        //init setting
        ibProfile.setImageResource(R.drawable.user_orange)
        ibProfileC.setImageResource(R.drawable.user_orange)
        tvTitle.text = "Verify Password"


        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!

        //variables
        var currentPw:String?=null
        var email:String?=null

        val bundle: Bundle? = intent.extras

        bundle?.let {
            currentPw = bundle.getString("msgUserPw")
            email = bundle.getString("msgUserEmail")
        }

        if(sp_role=="Doctor"||sp_role=="Assistant"){
            mnPatientResetPw.visibility = View.INVISIBLE
        }
        else{
            mnClinicResetPw.visibility = View.INVISIBLE
        }

        btnLogout.setOnClickListener{
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
        btnContinue.setOnClickListener {

            var inputPw:String?=null
            inputPw=etCurrentPw.text.toString()
            if(inputPw==currentPw){
                val intent = Intent(this, Change_Pw::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }else{
                Toast.makeText(this, "Wrong Current Password", Toast.LENGTH_SHORT).show()
            }

        }
        tvForgotPassword.setOnClickListener{

            Log.d(TAG, "email is=>"+email)
            val webAddress : String? = ""
            var code:Int
            var random:Random = Random()
            code = random.nextInt(8999)+1000
           // var url = "https://www.google.com"
            var requestQueue :RequestQueue = Volley.newRequestQueue(applicationContext)

//            http://infinityqw.rf.gd/sendEmail.php
            //https://infinityqw.000webhostapp.com/sendEmail.php
            var stringRequest:StringRequest =  object:StringRequest(Method.POST, "https://infinityqw.000webhostapp.com/sendEmail.php",
                Response.Listener{ response->
                    Toast.makeText(this, ""+response, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, response)
                },
                Response.ErrorListener{ error ->
                    Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, error.toString())
                })
            {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["email"] = email.toString()
                    params["code"] = code.toString()
                    return params
                }
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/x-www-form-urlencoded"
                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )

            requestQueue.add(stringRequest)


            //StringRequest request = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {

            val intent = Intent(this, Email_Verification::class.java)
            intent.putExtra("msgOTP", code.toString())
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        //menu bar button
        ibHome.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        ibApp.setOnClickListener{
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
            val intent = Intent(this, Account_Setting_Clinic::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}