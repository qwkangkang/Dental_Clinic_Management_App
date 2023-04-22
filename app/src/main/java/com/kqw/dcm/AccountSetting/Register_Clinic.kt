package com.kqw.dcm.AccountSetting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.R
import com.scottyab.aescrypt.AESCrypt
import kotlinx.android.synthetic.main.create_appointment.*
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
import java.security.GeneralSecurityException
import java.util.HashMap

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
        ddlSpecialist.setText(adapterItems.getItem(0), false)

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
                tvSpecialist.isEnabled = true
                ddlSpecialist.isEnabled = true
            }
            else if (radAssistant.isChecked) {
                role="Assistant"
                tvSpecialist.isEnabled = false
                ddlSpecialist.isEnabled = false
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
                var emailFound:Boolean=false
                val email = etEmail.text.toString().trim()
                db.collection("User").get()
                    .addOnSuccessListener {
                        if (!it.isEmpty) {
                            for (user in it.documents) {
                                var sEmail: String = user.data?.get("user_email").toString()
                                if (sEmail == email) {
                                    Log.d(Register.TAG, "found the same")
                                    emailFound = true
                                }
                            }
                            if (emailFound) {
                                Toast.makeText(
                                    this,
                                    "This Email Has Been Registered",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val key = "passwordKey"
                                var encryptedPassword:String?=null
                                try {
                                    encryptedPassword = AESCrypt.encrypt(key, password)
                                } catch (e: GeneralSecurityException) {
                                    Log.d(Register.TAG, "password encrypted failed")
                                }

                                val id = db.collection("collection_name").document().id

                                val userMap = hashMapOf(
                                    "user_ID" to id,
                                    "user_first_name" to fname,
                                    "user_last_name" to lname,
                                    "user_email" to email,
                                    "user_role" to role,
                                    "user_password" to encryptedPassword,
                                    "user_phone" to contactNo,
                                    "user_IC_no" to IC,
                                    "user_gender" to gender
                                )

                                db.collection("User").document(id)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        Log.d(
                                            Register.TAG,
                                            "Success add user"
                                        )
                                    }
                                    .addOnFailureListener { e -> Log.w(Register.TAG, "Error") }

                                if (role == "Doctor") {
                                    val doctorID = db.collection("collection_name1").document().id

                                    val doctorMap = hashMapOf(
                                        "doctor_ID" to doctorID,
                                        "doctor_specialist" to specialist,
                                        "user_ID" to id
                                    )

                                    db.collection("Doctor").document(doctorID)
                                        .set(doctorMap)
                                        .addOnSuccessListener {
                                            Log.d(
                                                Register.TAG,
                                                "Success Add doctor"
                                            )
                                        }
                                        .addOnFailureListener { e -> Log.w(Register.TAG, "Error") }
                                    Toast.makeText(this, "Register Successful. Please Login First", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, Login::class.java)
                                    startActivity(intent)
                                    overridePendingTransition(0, 0)
                                } else if (role == "Assistant") {
                                    val assistantID =
                                        db.collection("collection_name1").document().id

                                    val assistantMap = hashMapOf(
                                        "assistant_ID" to assistantID,
                                        "user_ID" to id
                                    )

                                    db.collection("Assistant").document(assistantID)
                                        .set(assistantMap)
                                        .addOnSuccessListener {
                                            Log.d(
                                                Register.TAG,
                                                "Success Add assistant"
                                            )
                                        }
                                        .addOnFailureListener { e -> Log.w(Register.TAG, "Error") }
                                    sendEmail()
                                    Toast.makeText(this, "Register Successful. Please Login First", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, Login::class.java)
                                    startActivity(intent)
                                    overridePendingTransition(0, 0)
                                }
                            }
                        }
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

        if(lname.isEmpty()){
            return "Last Name Required"
        }
        return null
    }

    private fun validEmail():String?{
        val email = etEmail.text.toString().trim()

        if(email.isEmpty()){
            return "Email Required"
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid Email Format"
        }
        return null
    }

    private fun validContactNo():String?{
        val contactNo = etContact.text.toString().trim()

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
        val IC = etIC.text.toString().trim()

        if(IC.isEmpty()){
            return "IC No. Required"
        }
        if(!IC.matches(".*[0-9]{6}-[0-9]{2}-[0-9]{4}.*".toRegex())||IC.length!=14){
            return "Invalid IC No. Format"
        }
        return null
    }

    private fun validPassword():String?{
        val password = etPassword.text.toString().trim()

        if(password.isEmpty()){
            return "Password Required"
        }
        if(password.length<8){
            return "Minimum 8 Character Password"
        }
        if(!password.matches(".*[A-Z].*".toRegex())){
            return "Must Contain 1 Upper-case Character"
        }
        if(!password.matches(".*[a-z].*".toRegex())){
            return "Must Contain 1 Lower-case Character"
        }
        if(!password.matches(".*[@#\$%^&+=].*".toRegex())) {
            return "Must Contain 1 Special Character (@#\$%^&+=)"
        }
        return null
    }

    private fun validCPassword():String?{
        val confirmPassword = etCPassword.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if(confirmPassword.isEmpty()){
            return "Confirm Password Required"
        }
        if(confirmPassword!=password){
            return "Confirm Password Is Not Matched"
        }
        return null
    }

    private fun validSpecialist():String?{
        val specialist = ddlSpecialist.text.toString().trim()

        if(specialist.isEmpty()){
            return "Please Choose A Specialist"
        }
        return null
    }

    private fun sendEmail() {
        val requestQueue : RequestQueue = Volley.newRequestQueue(applicationContext)

        val stringRequest: StringRequest =  object: StringRequest(
            Method.POST, "https://infinityqw.000webhostapp.com/SendEmailRegister.php",
            Response.Listener{ response->
                Toast.makeText(this, ""+response, Toast.LENGTH_SHORT).show()
                Log.d(Reset_Pw.TAG, response)
            },
            Response.ErrorListener{ error ->
                Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show()
                Log.d(Reset_Pw.TAG, error.toString())
            })
        {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["email"] = etEmail.text.toString()
                params["fname"] = etFName.text.toString()
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
    }
}