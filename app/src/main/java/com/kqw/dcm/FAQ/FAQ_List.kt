package com.kqw.dcm.FAQ

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
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Appointment.Appointment_Data
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.PatientListAdapter
import com.kqw.dcm.Patient.Patient_Data
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.appointment_list.*
import kotlinx.android.synthetic.main.faq_list.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.patient_list.*
import kotlinx.android.synthetic.main.title_bar.*

class FAQ_List: AppCompatActivity() {
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
    private lateinit var faqList: ArrayList<FAQ_Data>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.faq_list)
        ibHome.setImageResource(R.drawable.home_orange)
        ibHomeC.setImageResource(R.drawable.home_orange)

        //init setting
        tvTitle.text = "FAQ"


        //variables
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!


        if(sp_role=="Doctor"){
            mnPatientFAQ.visibility = View.INVISIBLE
        }
        else if (sp_role=="Assistant"){
            mnPatientFAQ.visibility = View.INVISIBLE
            ibAdd.visibility = View.INVISIBLE
        }
        else{
            mnClinicFAQ.visibility = View.INVISIBLE
            ibAdd.visibility = View.INVISIBLE
        }

        rvFAQ.layoutManager = LinearLayoutManager(this)
        faqList = arrayListOf()

        db = FirebaseFirestore.getInstance()

        db.collection("FAQ").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (faq in it.documents) {
                        val faqQues = faq.get("FAQ_question").toString()
                        val faqAns = faq.get("FAQ_answer").toString()
                        val faqID = faq.get("FAQ_ID").toString()

                        val faq = FAQ_Data(faqID, faqQues, faqAns)

                        if(faq!=null){
                            faqList.add(faq)
                        }
                        rvFAQ.adapter = FAQListAdapter(faqList)
                    }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed retrieve FAQ")
            }


        btnLogout.setOnClickListener {
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        ibAdd.setOnClickListener {
            val intent = Intent(this, Create_FAQ::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnBack.setOnClickListener {
            finish()
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
            val intent = Intent(this, Account_Setting::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}