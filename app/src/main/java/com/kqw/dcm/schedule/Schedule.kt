package com.kqw.dcm.schedule

import android.app.DatePickerDialog
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_Data
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.schedule.*
import kotlinx.android.synthetic.main.title_bar.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Schedule: AppCompatActivity() {
    companion object {
        val TAG: String = Schedule::class.java.simpleName
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
        setContentView(R.layout.schedule)

        //init setting
        btnBack.visibility = View.INVISIBLE
        tvTitle.text = "Schedule"
        ibApp.setImageResource(R.drawable.appointment_orange)

        //val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy (EEEE)")
        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val today = LocalDateTime.now().format(dateFormat)
        etDate.setText(today)

        //variables
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!


        if(sp_role=="Doctor"||sp_role=="Assistant"){
            mnPatient.visibility = View.INVISIBLE
        }
        else{
            mnClinic.visibility = View.INVISIBLE
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnEdit.setOnClickListener {
            if(sp_role!="Doctor"){
                Toast.makeText(this, "Only Doctors Can Edit Schedule", Toast.LENGTH_SHORT).show()
            }else{
                val intent = Intent(this, Edit_Schedule::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }

        btnDelete.setOnClickListener {
            if(sp_role!="Doctor"){
                Toast.makeText(this, "Only Doctors Can Edit Schedule", Toast.LENGTH_SHORT).show()
            }else{

            }
        }

        etDate.setOnClickListener {
            Log.d(Schedule.TAG, "date is clicked")
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
                etDate.setText(""+mDay+"-"+mMonth+"-"+mYear)
            },year, month, day)

            dpd.show()
        }

        //menu bar button
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
        ibHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }


        ibApp.setOnClickListener {
            val popupMenu: PopupMenu = PopupMenu(this, ibApp)
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

        }
    }
}