package com.kqw.dcm.AccountSetting

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.R
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.register.*
import kotlinx.android.synthetic.main.register.etEmail
import kotlinx.android.synthetic.main.register.etPassword
import kotlinx.android.synthetic.main.register.tilEmail
import kotlinx.android.synthetic.main.register.tilPassword

class Register : AppCompatActivity() {

    companion object{
        val TAG:String = Register::class.java.simpleName
    }

    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        //variables
        var valid: Boolean = true
        var role:String?="Patient"
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


//            contactNoFocusListener()
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
                Toast.makeText(this, "validation pass", Toast.LENGTH_SHORT).show()

//                val userID = FirebaseAuth.getInstance().currentUser!!.uid
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

                finish()
            }


//            }else{
//                Toast.makeText(this, "something wrong", Toast.LENGTH_SHORT).show()
//            }

//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            overridePendingTransition(0, 0)
        }

        btnCancel.setOnClickListener{
//            val intent = Intent(this, Login::class.java)
//            startActivity(intent)
//            overridePendingTransition(0, 0)
            finish()
        }

        tvSignUpAsDoc.setOnClickListener{
            tvSignUpAsDoc.setTextColor(Color.parseColor("#FE8800"))
            val intent = Intent(this, Register_Clinic::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }


    private fun contactNoFocusListener(){
       // Toast.makeText(this, validContactNo(), Toast.LENGTH_SHORT).show()
//        tilContact.setOnFocusChangeListener { _, focused ->
//            if(!focused){
////                val x:String? = validContactNo()
//                Toast.makeText(this, "byebye", Toast.LENGTH_SHORT).show()
//                tilContact.helperText = validContactNo()
//            }
//        }
        tilContact.helperText = validContactNo()
//        etContact.setOnFocusChangeListener { _, hasFocus ->
//            if(!hasFocus) {
//                Toast.makeText(this, "byebye", Toast.LENGTH_SHORT).show()
//                tilContact.helperText = validContactNo()
//            }
//        }
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
//        if(IC!!.length!=14 ){
//            return "Invalid IC No. Format"
//        }
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

    private fun validAddress():String?{
        val address = etAddress.text.toString().trim()

        if(address!!.isEmpty()){
            return "Address Required"
        }
        return null
    }
}