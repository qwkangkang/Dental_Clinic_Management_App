package com.kqw.dcm.AccountSetting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.*
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.account_setting.*
import kotlinx.android.synthetic.main.acount_setting_clinic.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.title_bar.*

class Account_Setting: AppCompatActivity() {
    companion object{
        val TAG:String = Account_Setting::class.java.simpleName
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
        setContentView(R.layout.account_setting)

        //init setting
        ibProfile.setImageResource(R.drawable.user_orange)
        tvTitle.text = "Account Setting"

        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!

        //variables
        var email: String? = null
        var password: String? = null
        var patientID: String? = null


        db = FirebaseFirestore.getInstance()
        val refUser = db.collection("User").document(sp_uid)
        refUser.get().addOnSuccessListener {
            if (it != null) {
                val fname = it.data?.get("user_first_name").toString()
                val lastName = it.data?.get("user_last_name").toString()
                email = it.data?.get("user_email").toString()
                val contactNo = it.data?.get("user_phone").toString()
                val ICNo = it.data?.get("user_IC_no").toString()
                val gender = it.data?.get("user_gender").toString()
                password = it.data?.get("user_password").toString()

                if (sp_role == "Patient") {
                    db.collection("Patient").get()
                        .addOnSuccessListener {
                            if (!it.isEmpty) {
                                for (patient in it.documents) {
                                    if (patient.data?.get("user_ID").toString() == sp_uid) {
                                        patientID = patient.data?.get("patient_ID").toString()
                                        val address =
                                            patient.data?.get("patient_address").toString()

                                        etFNameAcc.setText(fname)
                                        etLNameAcc.setText(lastName)
                                        etEmailAcc.setText(email)
                                        etContactAcc.setText(contactNo)
                                        etICAcc.setText(ICNo)
                                        if (gender == "Male") {
                                            radMaleAcc.isChecked = true
                                        } else {
                                            radFemaleAcc.isChecked = true
                                        }
                                        etAddressAcc.setText(address)
                                    }
                                }
                            }
                        }.addOnFailureListener {
                            Log.d(
                                Account_Setting.TAG,
                                "failed retrieve patient"
                            )
                        }

                }
            }
        }.addOnFailureListener { Log.d(Account_Setting.TAG, "failed retrieve user") }

        btnLogout.setOnClickListener {
            val editor:SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnResetPw.setOnClickListener {
            val intent = Intent(this, Reset_Pw::class.java)
            intent.putExtra("msgUserPw", password)
            intent.putExtra("msgUserEmail", email)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnUpdate.setOnClickListener {
            tilFNameAcc.helperText = validFName()
            tilLNameAcc.helperText = validLName()
            tilEmailAcc.helperText = validEmail()
            tilContactAcc.helperText = validContactNo()
            tilICAcc.helperText = validIC()
            tilAddressAcc.helperText = validAddress()

            if (tilFNameAcc.helperText == null && tilLNameAcc.helperText == null && tilEmailAcc.helperText == null &&
                tilContactAcc.helperText == null && tilICAcc.helperText == null && tilAddressAcc.helperText == null
            ) {
                var gender:String?=null
                if(radMaleAcc.isChecked){
                    gender="Male"
                }else{
                    gender="Female"
                }

                val updateUserMap = mapOf(
                    "user_first_name" to etFNameAcc.text.toString(),
                    "user_last_name" to etLNameAcc.text.toString(),
                    "user_email" to etEmailAcc.text.toString(),
                    "user_phone" to etContactAcc.text.toString(),
                    "user_IC_no" to etICAcc.text.toString(),
                    "user_gender" to gender
                )
                db.collection("User").document(sp_uid).update(updateUserMap)

                val updatePatientMap = mapOf(
                    "patient_address" to etAddressAcc.text.toString()
                )
                db.collection("Patient").document(patientID.toString()).update(updatePatientMap)
                finish()
                overridePendingTransition(0, 0)
                startActivity(getIntent())
                overridePendingTransition(0, 0)
            }
        }
        btnCancel.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnBack.setOnClickListener {
            finish()
        }

        //menu bar button
        ibHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        ibApp.setOnClickListener {
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
    private fun validFName():String?{
        val fname = etFNameAcc.text.toString().trim()

        if(fname!!.isEmpty()){
            return "First Name Required"
        }
        return null
    }

    private fun validLName():String?{
        val lname = etLNameAcc.text.toString().trim()

        if(lname!!.isEmpty()){
            return "Last Name Required"
        }
        return null
    }

    private fun validEmail():String?{
        val email = etEmailAcc.text.toString().trim()

        if(email!!.isEmpty()){
            return "Email Required"
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid Email Format"
        }
        return null
    }

    private fun validContactNo():String?{
        val contactNo = etContactAcc.text.toString().trim()

        if(contactNo!!.isEmpty()){
            return "Contact No. Required"
        }
        if(!contactNo!!.matches(".*[0-9].*".toRegex())){
            return "Must Be Digits"
        }
        if((contactNo!!.length) != 10 && (contactNo!!.length) != 11){
            return "Must Be 10 or 11 Digits"
        }
        return null
    }

    private fun validIC():String?{
        val IC = etICAcc.text.toString().trim()

        if(IC!!.isEmpty()){
            return "IC No. Required"
        }
        if(!IC!!.matches(".*[0-9]{6}-[0-9]{2}-[0-9]{4}.*".toRegex())||IC!!.length!=14){
            return "Invalid IC No. Format"
        }
        return null
    }

    private fun validAddress():String?{
        val address = etAddressAcc.text.toString().trim()

        if(address!!.isEmpty()){
            return "Address Required"
        }
        return null
    }
}