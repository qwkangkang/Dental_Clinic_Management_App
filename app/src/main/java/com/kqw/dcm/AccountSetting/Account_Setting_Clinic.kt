package com.kqw.dcm.AccountSetting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import kotlinx.android.synthetic.main.acount_setting_clinic.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.title_bar.*
import kotlinx.android.synthetic.main.title_bar.tvTitle

class Account_Setting_Clinic: AppCompatActivity() {
    companion object {
        val TAG: String = Account_Setting_Clinic::class.java.simpleName
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
        setContentView(R.layout.acount_setting_clinic)

        //init setting
        ibProfileC.setImageResource(R.drawable.user_orange)
        tvTitle.text = getString(R.string.account_setting)


        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!

        //variables
        var doctorID:String?=null
        var password:String?=null
        var email:String?=null
        var assistantID:String?=null
        var specialistItems = listOf("None", "General Dentist", "Pedodontist", "Orthodontist", "Others")
        lateinit var adapterItems: ArrayAdapter<String>
        adapterItems = ArrayAdapter(this, R.layout.list_item_ddl, specialistItems)
        ddlSpecialistAccC.setAdapter(adapterItems)


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

                if (sp_role == "Doctor") {
                    db.collection("Doctor").get()
                        .addOnSuccessListener {
                            if (!it.isEmpty) {
                                for (doctor in it.documents) {
                                    if (doctor.data?.get("user_ID").toString() == sp_uid) {
                                        doctorID = doctor.data?.get("doctor_ID").toString()
                                        val specialist =
                                            doctor.data?.get("doctor_specialist").toString()
                                        etFNameAccC.setText(fname)
                                        etLNameAccC.setText(lastName)
                                        etEmailAccC.setText(email)
                                        etContactAccC.setText(contactNo)
                                        etICAccC.setText(ICNo)
                                        if (gender == "Male") {
                                            radMaleAccC.isChecked = true
                                        } else {
                                            radFemaleAccC.isChecked = true
                                        }
                                        ddlSpecialistAccC.setText(specialist, false)

                                    }
                                }
                            }
                        }.addOnFailureListener { Log.d(TAG, "failed retrieve doctor") }
                } else if (sp_role == "Assistant") {
                    db.collection("Assistant").get()
                        .addOnSuccessListener {
                            if (!it.isEmpty) {
                                for (assistant in it.documents) {
                                    if (assistant.data?.get("user_ID").toString() == sp_uid) {
                                        assistantID =
                                            assistant.data?.get("assistant_ID").toString()

                                        etFNameAccC.setText(fname)
                                        etLNameAccC.setText(lastName)
                                        etEmailAccC.setText(email)
                                        etContactAccC.setText(contactNo)
                                        etICAccC.setText(ICNo)
                                        if (gender == "Male") {
                                            radMaleAccC.isChecked = true
                                        } else {
                                            radFemaleAccC.isChecked = true
                                        }
                                        tvSpecialistAccC.visibility = View.GONE
                                        tilSpecialistAcc.visibility = View.GONE
                                        ddlSpecialistAccC.visibility = View.GONE

                                    }
                                }
                            }
                        }.addOnFailureListener { Log.d(TAG, "failed retrieve assistant") }
                }
            }
        }


        btnLogout.setOnClickListener{
            val editor:SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnResetPwAccC.setOnClickListener {
            val intent = Intent(this, Reset_Pw::class.java)
            intent.putExtra("msgUserPw", password)
            intent.putExtra("msgUserEmail", email)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnUpdateAccC.setOnClickListener{
            tilFNameClinicAcc.helperText = validFName()
            tilLNameClinicAcc.helperText = validLName()
            tilEmailClinicAcc.helperText = validEmail()
            tilContactClinicAcc.helperText = validContactNo()
            tilICClinicAcc.helperText = validIC()
            tilSpecialistAcc.helperText = validSpecialist()

            if(tilFNameClinicAcc.helperText==null && tilLNameClinicAcc.helperText==null && tilEmailClinicAcc.helperText==null &&
                tilContactClinicAcc.helperText==null && tilICClinicAcc.helperText==null && tilSpecialistAcc.helperText==null) {

                if(sp_role=="Doctor"){
                    val updateDocMap = mapOf(
                        "doctor_specialist" to ddlSpecialistAccC.text.toString()
                    )
                    db.collection("Doctor").document(doctorID.toString()).update(updateDocMap)
                }
                var gender:String?=null
                if(radMaleAccC.isChecked){
                    gender="Male"
                }else{
                    gender="Female"
                }

                val updateUserMap = mapOf(
                    "user_first_name" to etFNameAccC.text.toString(),
                    "user_last_name" to etLNameAccC.text.toString(),
                    "user_email" to etEmailAccC.text.toString(),
                    "user_phone" to etContactAccC.text.toString(),
                    "user_IC_no" to etICAccC.text.toString(),
                    "user_gender" to gender
                )
                db.collection("User").document(sp_uid).update(updateUserMap)
                finish()
                overridePendingTransition(0, 0)
                startActivity(getIntent())
                overridePendingTransition(0, 0)
            }
        }

        btnCancelAccC.setOnClickListener {
            val intent = Intent(this, MainActivity_Clinic::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnBack.setOnClickListener {
            finish()
        }

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
    }

    private fun validFName():String?{
        val fname = etFNameAccC.text.toString().trim()

        if(fname.isEmpty()){
            return "First Name Required"
        }
        return null
    }

    private fun validLName():String?{
        val lname = etLNameAccC.text.toString().trim()

        if(lname.isEmpty()){
            return "Last Name Required"
        }
        return null
    }

    private fun validEmail():String?{
        val email = etEmailAccC.text.toString().trim()

        if(email.isEmpty()){
            return "Email Required"
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid Email Format"
        }
        return null
    }

    private fun validContactNo():String?{
        val contactNo = etContactAccC.text.toString().trim()

        if(contactNo.isEmpty()){
            return "Contact No. Required"
        }
        if(!contactNo.matches(".*[0-9].*".toRegex())){
            return "Must Be Digits"
        }
        if((contactNo.length) != 10 && (contactNo.length) != 11){
            return "Must Be 10 or 11 Digits"
        }
        return null
    }

    private fun validIC():String?{
        val IC = etICAccC.text.toString().trim()

        if(IC.isEmpty()){
            return "IC No. Required"
        }
        if(!IC.matches(".*[0-9]{6}-[0-9]{2}-[0-9]{4}.*".toRegex())||IC.length!=14){
            return "Invalid IC No. Format"
        }
        return null
    }

    private fun validSpecialist():String?{
        val specialist = ddlSpecialistAccC.text.toString().trim()

        if(specialist.isEmpty()){
            return "Please Choose A Specialist"
        }
        return null
    }

}