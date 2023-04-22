package com.kqw.dcm.Patient

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.AccountSetting.Account_Setting_Clinic
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.R
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.patient_list.*
import kotlinx.android.synthetic.main.title_bar.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Patient_List: AppCompatActivity() {
    companion object{
        val TAG:String = Patient_List::class.java.simpleName
    }


    private lateinit var patientList: ArrayList<Patient_Data>
    private var db = Firebase.firestore
    private var sPatientID: String? = null
    lateinit var sharedPreferences: SharedPreferences
    var PREFS_KEY = "prefs"
    var USERID_KEY = "uid"
    var ROLE_KEY = "role"
    var sp_uid = ""
    var sp_role = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_list)

        //init setting
        ibPatientC.setImageResource(R.drawable.patient_orange)
        tvTitle.text = "Patient"


        //variable
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!


        rvPatient.layoutManager = LinearLayoutManager(this)
        patientList = arrayListOf()

        db = FirebaseFirestore.getInstance()

        db.collection("User").get()
            .addOnSuccessListener {
                if(!it.isEmpty){
                    for(user in it.documents){
                        val role = user.get("user_role").toString()
                        if(role=="Patient") {

                            val userID = user.get("user_ID").toString()

                            db.collection("Patient").get()
                                .addOnSuccessListener {
                                    if (!it.isEmpty) {
                                        for (patient in it.documents) {
                                            if (userID==patient.data?.get("user_ID").toString()){
                                                sPatientID = patient.data?.get("patient_ID").toString()
                                                Log.d(Patient_List.TAG, "patient id=>"+sPatientID.toString())

                                                val patientName = user.get("user_first_name").toString()+" "+user.get("user_last_name").toString()
                                                Log.d(Login.TAG, "patient name=>"+patientName)
                                                var currentYear:Int = SimpleDateFormat("yyyy").format(Date()).toInt()
                                                val ICFirst2Digit = user.get("user_IC_no").toString().substring(0,2).toInt()
                                                currentYear = currentYear.toString().substring(2).toInt()
                                                val age:Int
                                                if(ICFirst2Digit>currentYear){
                                                    currentYear+=100
                                                }
                                                age = currentYear-ICFirst2Digit
                                                Log.d(Login.TAG, "patient age=>"+age.toString())
                                                val gender = user.get("user_gender").toString().substring(0,1)
                                                Log.d(Login.TAG, "patient gender=>"+gender)
                                                val contactNo = user.get("user_phone").toString()
                                                Log.d(Login.TAG, "patient contact=>"+contactNo)
                                                Log.d(Patient_List.TAG, "patient id=>"+sPatientID.toString())
                                                val patient = Patient_Data(sPatientID.toString(), patientName, gender, age, contactNo)

                                                // val patient: Patient_Data ?= data.toObject(Patient_Data::class.java)
                                                if(patient!=null){
                                                    patientList.add(patient)
                                                }
                                                rvPatient.adapter = PatientListAdapter(patientList)
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Log.d(Patient_List.TAG, "retrieve patient failed")
                                }

                        }
                        }

                }
            }

        btnLogout.setOnClickListener {
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnSearch.setOnClickListener {
            patientList.clear()
            val inputSearch = etSearch.text.toString().trim()
            var nameFound:Boolean = false
            db = FirebaseFirestore.getInstance()

            db.collection("User").get()
                .addOnSuccessListener {
                    if(!it.isEmpty){
                        for(user in it.documents){
                            var sFName:String = user.data?.get("user_first_name").toString()
                            var sLName:String = user.data?.get("user_last_name").toString()

                            Log.d(Login.TAG, inputSearch!!)
                            if(inputSearch==sFName||inputSearch==sLName||inputSearch==sFName+" "+sLName){
                                nameFound=true

                                val role = user.get("user_role").toString()
                                if(role=="Patient"){
                                    val patientName = user.get("user_first_name").toString()+" "+user.get("user_last_name").toString()
                                    var currentYear:Int = SimpleDateFormat("yyyy").format(Date()).toInt()
                                    val ICFirst2Digit = user.get("user_IC_no").toString().substring(0,2).toInt()
                                    currentYear = currentYear.toString().substring(2).toInt()
                                    val age:Int
                                    if(ICFirst2Digit>currentYear){
                                        currentYear+=100
                                    }
                                    age = currentYear-ICFirst2Digit
                                    val gender = user.get("user_gender").toString().substring(0,1)
                                    val contactNo = user.get("user_phone").toString()
                                    val patient = Patient_Data(sPatientID.toString(), patientName, gender, age, contactNo)

                                    // val patient: Patient_Data ?= data.toObject(Patient_Data::class.java)
                                    if(patient!=null){

                                        patientList.add(patient)
                                        Log.d(Login.TAG, "Going to refresh")
                                    }
                                }

                                rvPatient.adapter = PatientListAdapter(patientList)

                            }else if(inputSearch==""){
                                val role = user.get("user_role").toString()
                                if(role=="Patient"){
                                    val patientName = user.get("user_first_name").toString()+" "+user.get("user_last_name").toString()
                                    var currentYear:Int = SimpleDateFormat("yyyy").format(Date()).toInt()
                                    val ICFirst2Digit = user.get("user_IC_no").toString().substring(0,2).toInt()
                                    currentYear = currentYear.toString().substring(2).toInt()
                                    val age:Int
                                    if(ICFirst2Digit>currentYear){
                                        currentYear+=100
                                    }
                                    age = currentYear-ICFirst2Digit
                                    val gender = user.get("user_gender").toString().substring(0,1)
                                    val contactNo = user.get("user_phone").toString()
                                    val patient = Patient_Data(sPatientID.toString(),patientName, gender, age, contactNo)

                                    if(patient!=null){
                                        patientList.add(patient)
                                    }
                                }

                                rvPatient.adapter = PatientListAdapter(patientList)

                            }

                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Something Wrong", Toast.LENGTH_SHORT).show()
                }

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
            val intent = Intent(this, Account_Setting_Clinic::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

    }
}