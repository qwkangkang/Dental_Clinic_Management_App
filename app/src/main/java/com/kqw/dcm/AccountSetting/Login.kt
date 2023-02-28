package com.kqw.dcm.AccountSetting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.R
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.login.etEmail
import kotlinx.android.synthetic.main.login.etPassword
import kotlinx.android.synthetic.main.register.*

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
//    var PAT_KEY = "pid"
//    var DOC_KEY = "did"
//    var ASSI_KEY = "aid"
    var sp_uid = ""
    var sp_role = ""
//    var sp_pid = ""
//    var sp_did = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

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
                                    Log.d(TAG, "pw in db is $sPassword")
                                    Log.d(TAG, "pw input is $inputPassword")
                                    if(inputPassword==sPassword){
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
        }

    }


}