package com.kqw.dcm.AccountSetting

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.R
import kotlinx.android.synthetic.main.register.*
import kotlinx.android.synthetic.main.register_doc_assistant.*
import kotlinx.android.synthetic.main.register_doc_assistant.btnCancel
import kotlinx.android.synthetic.main.register_doc_assistant.btnSignUp
import kotlinx.android.synthetic.main.register_doc_assistant.etCPassword
import kotlinx.android.synthetic.main.register_doc_assistant.etContact
import kotlinx.android.synthetic.main.register_doc_assistant.etEmail
import kotlinx.android.synthetic.main.register_doc_assistant.etFName
import kotlinx.android.synthetic.main.register_doc_assistant.etIC
import kotlinx.android.synthetic.main.register_doc_assistant.etLName
import kotlinx.android.synthetic.main.register_doc_assistant.etPassword

class Register_Clinic:AppCompatActivity() {
    companion object{
        val TAG:String = Register_Clinic::class.java.simpleName
    }

//    var specialistItems = ArrayList<String>()
    var specialistItems = listOf("None", "General Dentist", "Pedodontist", "Orthodontist", "Others")
    lateinit var autoCompleteTxt:AutoCompleteTextView
    lateinit var adapterItems:ArrayAdapter<String>

    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_doc_assistant)

        //variables
        var valid: Boolean = true
        var role:String?=null
        var fname:String?=null
        var lname:String?=null
        var email:String?=null
        var contactNo: String?=null
        var IC: String?=null
        var password: String?=null
        var confirmPassword: String?=null
        var gender: String?=null
        var specialist: String?=null
        adapterItems = ArrayAdapter(this, R.layout.list_item_ddl, specialistItems)
        ddlSpecialist.setAdapter(adapterItems)


        btnSignUp.setOnClickListener {
            fname=etFName.text.toString().trim()
            lname=etLName.text.toString().trim()
            email=etEmail.text.toString().trim()
            contactNo=etContact.text.toString().trim()
            IC=etIC.text.toString().trim()
            password=etPassword.text.toString().trim()
            confirmPassword=etCPassword.text.toString().trim()

            if (radMaleClinic.isChecked) {
                gender="Male"
            }
            else if (radFemaleClinic.isChecked) {
                gender="Female"
            }
            if (radDoc.isChecked) {
                role="Doctor"
            }
            else if (radAssistant.isChecked) {
                role="Assistant"
            }
            specialist = ddlSpecialist.text.toString().trim()

            tilFNameClinic.helperText = validFName()
            tilLNameClinic.helperText = validLName()
            tilEmailClinic.helperText = validEmail()
            tilContactClinic.helperText = validContactNo()
            tilICClinic.helperText = validIC()
            tilPasswordClinic.helperText = validPassword()
            tilCPasswordClinic.helperText = validCPassword()
            tilSpecialist.helperText = validSpecialist()

            if(tilFNameClinic.helperText==null && tilLNameClinic.helperText==null && tilEmailClinic.helperText==null &&
                tilContactClinic.helperText==null && tilICClinic.helperText==null && tilPasswordClinic.helperText==null &&
                tilCPasswordClinic.helperText==null && tilSpecialist.helperText==null) {

               // Toast.makeText(this, "validation pass", Toast.LENGTH_SHORT).show()

                val id = db.collection("collection_name").document().id

                val userMap = hashMapOf(
                    "user_ID" to id,
                    "user_first_name" to fname,
                    "user_last_name" to lname,
                    "user_email" to email,
                    "user_role" to role,
                    "user_password" to password,
                    "user_phone" to contactNo,
                    "user_IC_no" to IC,
                    "user_gender" to gender
                )

                db.collection("User").document(id)
                    .set(userMap)
                    .addOnSuccessListener { Log.d(Register.TAG, "Success add user") }
                    .addOnFailureListener { e -> Log.w(Register.TAG, "Error") }

                if (role=="Doctor"){
                    val doctorID = db.collection("collection_name1").document().id

                    val doctorMap = hashMapOf(
                        "doctor_ID" to doctorID,
                        "doctor_specialst" to specialist,
                        "user_ID" to id
                    )

                    db.collection("Doctor").document(doctorID)
                        .set(doctorMap)
                        .addOnSuccessListener { Log.d(Register.TAG, "Success Add doctor") }
                        .addOnFailureListener { e -> Log.w(Register.TAG, "Error") }
                }
                else if(role=="Assistant"){
                    val assistantID = db.collection("collection_name1").document().id

                    val assistantMap = hashMapOf(
                        "assistant_ID" to assistantID,
                        "user_ID" to id
                    )

                    db.collection("Assistant").document(assistantID)
                        .set(assistantMap)
                        .addOnSuccessListener { Log.d(Register.TAG, "Success Add assistant") }
                        .addOnFailureListener { e -> Log.w(Register.TAG, "Error") }
                }

            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validFName():String?{
        val fname = etFName.text.toString().trim()

        if(fname!!.isEmpty()){
            return "First Name Required"
        }
        return null
    }

    private fun validLName():String?{
        val lname = etLName.text.toString().trim()

        if(lname!!.isEmpty()){
            return "Last Name Required"
        }
        return null
    }

    private fun validEmail():String?{
        val email = etEmail.text.toString().trim()

        if(email!!.isEmpty()){
            return "Email Required"
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid Email Format"
        }
        return null
    }

    private fun validContactNo():String?{
        val contactNo = etContact.text.toString().trim()

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
        val IC = etIC.text.toString().trim()

        if(IC!!.isEmpty()){
            return "IC No. Required"
        }
        if(!IC!!.matches(".*[0-9]{6}-[0-9]{2}-[0-9]{4}.*".toRegex())||IC!!.length!=14){
            return "Invalid IC No. Format"
        }
        return null
    }

    private fun validPassword():String?{
        val password = etPassword.text.toString().trim()

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

    private fun validCPassword():String?{
        val confirmPassword = etCPassword.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if(confirmPassword!!.isEmpty()){
            return "Confirm Password Required"
        }
        if(confirmPassword!=password){
            return "Confirm Password Is Not Matched"
        }
        return null
    }

    private fun validSpecialist():String?{
        val specialist = ddlSpecialist.text.toString().trim()

        if(specialist!!.isEmpty()){
            return "Please Choose A Specialist"
        }
        return null
    }
}