package com.kqw.dcm.AccountSetting

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.reset_password.*
import kotlinx.android.synthetic.main.title_bar.btnBack
import kotlinx.android.synthetic.main.title_bar.btnLogout
import kotlinx.android.synthetic.main.title_bar.tvTitle

class Reset_Pw:AppCompatActivity() {
    companion object{
        val TAG:String = Reset_Pw::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset_password)

        //init setting
        ibProfile.setImageResource(R.drawable.user_orange)
        tvTitle.text = "Verify Password"

        btnLogout.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        btnBack.setOnClickListener {
//            val intent = Intent(this, Account_Setting::class.java)
//            startActivity(intent)
            this.finish()
            overridePendingTransition(0, 0)
        }
        btnContinue.setOnClickListener {
            val intent = Intent(this, Change_Pw::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        tvForgotPassword.setOnClickListener{
            val intent = Intent(this, Email_Verification::class.java)
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