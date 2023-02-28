package com.kqw.dcm.AccountSetting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kqw.dcm.*
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import kotlinx.android.synthetic.main.account_setting.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.title_bar.*

class Account_Setting: AppCompatActivity() {
    companion object{
        val TAG:String = Account_Setting::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_setting)

        //init setting
        ibProfile.setImageResource(R.drawable.user_orange)
        tvTitle.text = "Account Setting"
        btnBack.visibility = View.INVISIBLE

        btnLogout.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnResetPw.setOnClickListener {
            val intent = Intent(this, Reset_Pw::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnUpdate.setOnClickListener{

        }

        btnCancel.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
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
    }
}