package com.kqw.dcm.AccountSetting

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
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
import com.kqw.dcm.R
import com.scottyab.aescrypt.AESCrypt
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.register.*
import kotlinx.android.synthetic.main.register.etEmail
import kotlinx.android.synthetic.main.register.etPassword
import kotlinx.android.synthetic.main.register.tilEmail
import kotlinx.android.synthetic.main.register.tilPassword
import java.security.GeneralSecurityException
import java.util.HashMap


class Register : AppCompatActivity() {

    companion object{
        val TAG:String = Register::class.java.simpleName
    }

    private var db = Firebase.firestore
    override fun onResume() {
        super.onResume()
        tvSignUpAsDoc.setTextColor(Color.parseColor("#000000"))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        //int setting
        tvSignUpAsDoc.setTextColor(Color.parseColor("#000000"))

        //variables
        val role:String="Patient"
        var fname:String?=null
        var lname:String?=null
        var email:String?=null
        var contactNo: String?=null
        var IC: String?=null
        var password: String?=null
        var confirmPassword: String?=null
        var gender: String?=null
        var address: String?=null


        btnSignUp.setOnClickListener {
            fname=etFName.text.toString().trim()
            lname=etLName.text.toString().trim()
            email=etEmail.text.toString().trim()
            contactNo=etContact.text.toString().trim()
            IC=etIC.text.toString().trim()
            password=etPassword.text.toString().trim()
            confirmPassword=etCPassword.text.toString().trim()
            address = etAddress.text.toString().trim()
            if (radMale.isChecked)
                gender="Male"
            else if (radFemale.isChecked)
                gender="Female"

            tilFName.helperText = validFName()
            tilLName.helperText = validLName()
            tilEmail.helperText = validEmail()
            tilContact.helperText = validContactNo()
            tilIC.helperText = validIC()
            tilPassword.helperText = validPassword()
            tilCPassword.helperText = validCPassword()
            tilAddress.helperText = validAddress()

            if(tilFName.helperText==null && tilLName.helperText==null && tilEmail.helperText==null &&
                tilContact.helperText==null && tilIC.helperText==null && tilPassword.helperText==null &&
                tilCPassword.helperText==null && tilAddress.helperText==null) {
                var emailFound:Boolean=false
                val email = etEmail.text.toString().trim()
                db.collection("User").get()
                    .addOnSuccessListener{
                        if (!it.isEmpty) {
                            for (user in it.documents) {
                                var sEmail: String = user.data?.get("user_email").toString()
                                if (sEmail == email) {
                                    Log.d(TAG, "found the same")
                                    emailFound = true
                                }
                            }
                            if(emailFound){
                                Toast.makeText(this, "This Email Has Been Registered", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                val key = "passwordKey"
                                var encryptedPassword:String?=null
                                try {
                                    encryptedPassword = AESCrypt.encrypt(key, password)
                                } catch (e: GeneralSecurityException) {
                                    Log.d(TAG, "password encrypted failed")
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
                                    .addOnSuccessListener { Log.d(TAG, "Success") }
                                    .addOnFailureListener { e -> Log.w(TAG, "Error") }

                                val patientID = db.collection("collection_name1").document().id

                                val patientMap = hashMapOf(
                                    "patient_ID" to patientID,
                                    "patient_address" to address,
                                    "user_ID" to id
                                )

                                db.collection("Patient").document(patientID)
                                    .set(patientMap)
                                    .addOnSuccessListener { Log.d(TAG, "Success") }
                                    .addOnFailureListener { e -> Log.w(TAG, "Error") }
                                sendEmail()
                                Toast.makeText(this, "Register Successful. Please Login First", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    }
            }
        }

        btnCancel.setOnClickListener{
            finish()
            overridePendingTransition(0, 0)
        }

        tvSignUpAsDoc.setOnClickListener{
            tvSignUpAsDoc.setTextColor(Color.parseColor("#FE8800"))
            val intent = Intent(this, Register_Clinic::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
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



    private fun validFName():String?{
        val fname = etFName.text.toString().trim()

        if(fname.isEmpty()){
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

    private fun validAddress():String?{
        val address = etAddress.text.toString().trim()

        if(address.isEmpty()){
            return "Address Required"
        }
        return null
    }
}