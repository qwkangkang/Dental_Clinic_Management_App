package com.kqw.dcm.AccountSetting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.R
import com.scottyab.aescrypt.AESCrypt
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.login.etEmail
import kotlinx.android.synthetic.main.login.etPassword
import kotlinx.android.synthetic.main.register.*
import java.security.GeneralSecurityException
import java.util.*


//import kotlinx.android.synthetic.main.login.*

class Login : AppCompatActivity() {
    companion object{
        val TAG:String = Login::class.java.simpleName
    }

    private var db = Firebase.firestore
    lateinit var sharedPreferences: SharedPreferences
    var PREFS_KEY = "prefs"
    var USERID_KEY = "uid"
    var ROLE_KEY = "role"
    var sp_uid = ""
    var sp_role = ""


    override fun onResume() {
        super.onResume()
        tvForgotPassword.setTextColor(Color.parseColor("#000000"))
        tvSignUp.setTextColor(Color.parseColor("#000000"))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        //init setting
        tvSignUp.setTextColor(Color.parseColor("#000000"))
        tvForgotPassword.setTextColor(Color.parseColor("#000000"))

        //variables
        var role: String? = null
        var inputEmail:String?= null
        var inputPassword:String?=null
        var emailFound:Boolean=false
        var currentUserId:String?=null


        btnLogin.setOnClickListener{

            inputEmail=etEmail.text.toString().trim()
            inputPassword=etPassword.text.toString().trim()



            db = FirebaseFirestore.getInstance()

            db.collection("User").get()
                .addOnSuccessListener {
                    if(!it.isEmpty){
                        var no:Int=1
                        for(user in it.documents){
                            Log.d(TAG, "User"+no++)
                            var sEmail:String = user.data?.get("user_email").toString()
                            Log.d(TAG, sEmail)
                            Log.d(TAG, inputEmail!!)
                            if(sEmail==inputEmail){
                                emailFound=true
                                currentUserId = user.data?.get("user_ID").toString()
                                Log.d(TAG, "email same")
                            }
                        }
                        if(emailFound){
                            val ref = db.collection("User").document(currentUserId.toString())
                            ref.get().addOnSuccessListener {
                                if(it!=null){
                                    val sPassword = it.data?.get("user_password").toString()

                                    // To Decrypt
                                    val key = "passwordKey"
                                    var passwordAfterDecrypt:String?=null
                                    try {
                                        passwordAfterDecrypt = AESCrypt.decrypt(key, sPassword)
                                    } catch (e: GeneralSecurityException) {
                                        Log.d(TAG, "decryption failed")
                                    }
                                    Log.d(TAG, "decryped pw is "+passwordAfterDecrypt)
                                    if(inputPassword==passwordAfterDecrypt){
                                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                        role = it.data?.get("user_role").toString()
                                        Log.d(TAG, "login success")
                                        Log.d(TAG, "Role is "+role!!)

                                        //store in shared preferences
                                        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
                                        sp_uid = sharedPreferences.getString(USERID_KEY, "").toString()
                                        sp_role = sharedPreferences.getString(ROLE_KEY, "").toString()

                                        val editor:SharedPreferences.Editor = sharedPreferences.edit()
                                        editor.putString(USERID_KEY, currentUserId)
                                        editor.putString(ROLE_KEY, role)
                                        editor.apply()

                                        if(role=="Patient"){
                                            val intent = Intent(this, MainActivity::class.java)
                                            startActivity(intent)
                                            overridePendingTransition(0, 0)
                                        }else if (role=="Doctor"||role=="Assistant"){
                                            val intent = Intent(this, MainActivity_Clinic::class.java)
                                            startActivity(intent)
                                            overridePendingTransition(0, 0)
                                        }

                                    }else{
                                        Toast.makeText(this, "Sorry, Your Password Is Wrong", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        else{
                            Toast.makeText(this, "Sorry, This Account Has Not Been Registered", Toast.LENGTH_SHORT).show()
                        }

                        //end try
                    }else
                    {
                        Toast.makeText(this, "There Are No Patient Yet", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
                }
            Log.d(TAG, "Email is $emailFound")



        }

        tvSignUp.setOnClickListener {
            tvSignUp.setTextColor(Color.parseColor("#FE8800"))
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
//            val key = "passwordKey"
//            var encryptedPassword:String?=null
//            try {
//                encryptedPassword = AESCrypt.encrypt(key, etPassword.text.toString())
//            } catch (e: GeneralSecurityException) {
//                Log.d(Register.TAG, "password encrypted failed")
//            }
//            Log.d(TAG, "encrypted pw is :"+encryptedPassword)
        }

        tvForgotPassword.setOnClickListener {
            tvForgotPassword.setTextColor(Color.parseColor("#FE8800"))
            val email = etEmail.text.toString()
            if(email==""){
                Toast.makeText(this, "Please Enter Your Registered Email First", Toast.LENGTH_SHORT).show()
            }else {

                inputEmail = etEmail.text.toString()
                db = FirebaseFirestore.getInstance()

                db.collection("User").get()
                    .addOnSuccessListener {
                        if (!it.isEmpty) {
                            var no: Int = 1
                            for (user in it.documents) {
                                Log.d(TAG, "User" + no++)
                                var sEmail: String = user.data?.get("user_email").toString()
                                Log.d(TAG, sEmail)
                                Log.d(TAG, inputEmail!!)
                                if (sEmail == inputEmail) {
                                    emailFound = true
                                    currentUserId = user.data?.get("user_ID").toString()
                                    Log.d(TAG, "email same")
                                    break
                                }
                            }

                            //start try
                            Log.d(TAG, "email is=>"+email)
                            var code:Int
                            var random: Random = Random()
                            code = random.nextInt(8999)+1000
                            var requestQueue : RequestQueue = Volley.newRequestQueue(applicationContext)

                            var stringRequest: StringRequest =  object: StringRequest(
                                Method.POST, "https://infinityqw.000webhostapp.com/sendEmail.php",
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
                                    params["email"] = email
                                    params["code"] = code.toString()
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

                            Log.d(TAG, "uid login is "+currentUserId)
                            val intent = Intent(this, Email_Verification::class.java)
                            intent.putExtra("msgOTP", code.toString())
                            intent.putExtra("msgUID", currentUserId)
                            intent.putExtra("msgEMAIL", etEmail.text.toString())
                            startActivity(intent)
                            overridePendingTransition(0, 0)

                            //end try



                        }
                    }




            }
        }

    }


}