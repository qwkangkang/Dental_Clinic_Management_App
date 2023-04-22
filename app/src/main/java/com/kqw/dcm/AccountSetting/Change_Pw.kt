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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.*
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Schedule_List
import com.scottyab.aescrypt.AESCrypt
import kotlinx.android.synthetic.main.change_password.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.register_doc_assistant.*
import kotlinx.android.synthetic.main.reset_password.*
import kotlinx.android.synthetic.main.title_bar.btnBack
import kotlinx.android.synthetic.main.title_bar.btnLogout
import kotlinx.android.synthetic.main.title_bar.tvTitle
import java.security.GeneralSecurityException

class Change_Pw:AppCompatActivity() {
    companion object{
        val TAG:String = Change_Pw::class.java.simpleName
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
        setContentView(R.layout.change_password)

        //init setting
        ibProfile.setImageResource(R.drawable.user_orange)
        ibProfileC.setImageResource(R.drawable.user_orange)
        tvTitle.text = getString(R.string.reset_password)

        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, "")!!
        sp_role = sharedPreferences.getString(ROLE_KEY, "")!!

        if(sp_role=="Doctor"||sp_role=="Assistant"){
            mnPatientChangePw.visibility = View.INVISIBLE
        }
        else if(sp_role=="Patient"){
            mnClinicChangePw.visibility = View.INVISIBLE
        }else{
            mnPatientChangePw.visibility = View.INVISIBLE
            mnClinicChangePw.visibility = View.INVISIBLE
        }

        val bundle: Bundle? = intent.extras
        var uid:String?=null
        var email:String?=null

        bundle?.let {
            uid = bundle.getString("msgUID")
            email = bundle.getString("msgEMAIL")
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
        }

        btnResetPw.setOnClickListener {

            tilPasswordChangePw.helperText = validPassword()

            if(tilPasswordChangePw.helperText==null){
                var inputPassword:String?=null
                inputPassword = etNewPw.text.toString().trim()

                val key = "passwordKey"
                var encryptedPassword:String?=null
                try {
                    encryptedPassword = AESCrypt.encrypt(key, inputPassword)
                } catch (e: GeneralSecurityException) {
                    Log.d(Register.TAG, "password encrypted failed")
                }

                val updateUserMap = mapOf(
                    "user_password" to encryptedPassword
                )

                if(sp_uid!=""){
                    db.collection("User").document(sp_uid).update(updateUserMap)
                    Toast.makeText(this, "Password Has Been Changed", Toast.LENGTH_SHORT).show()
                    if(sp_role=="Doctor"||sp_role=="Assistant"){
                        val intent = Intent(this, Account_Setting_Clinic::class.java)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                    }
                    else if(sp_role=="Patient"){
                        val intent = Intent(this, Account_Setting::class.java)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                    }
                }
                else{
                    Log.d(TAG, "uid is " + uid)
                    db.collection("User").document(uid.toString()).update(updateUserMap)
                    //alternatively
//                    db.collection("User")
//                        .whereEqualTo("user_email", email)
//                        .limit(1)
//                        .get()
//                        .addOnSuccessListener { documents ->
//                            for (document in documents) {
//                                db.collection("User").document(document.id).update(updateUserMap)
//                            }
//
//                        }
//                        .addOnFailureListener {
//                            Log.d(TAG, "error saving updateUserMap")
//                        }
                    Toast.makeText(this, "Password Has Been Changed. Please Login First", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            }
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
    private fun validPassword():String?{
        val password = etNewPw.text.toString().trim()

        if(password!!.isEmpty()){
            return "Password Required"
        }
        if(password!!.length<8){
            return "Minimum 8 Character Password"
        }
        if(!password!!.matches(".*[A-Z].*".toRegex())){
            return "Must Contain 1 Upper-case Character"
        }
        if(!password!!.matches(".*[a-z].*".toRegex())){
            return "Must Contain 1 Lower-case Character"
        }
        if(!password!!.matches(".*[@#\$%^&+=].*".toRegex())) {
            return "Must Contain 1 Special Character (@#\$%^&+=)"
        }
        return null
    }
}