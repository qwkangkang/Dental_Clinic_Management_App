package com.kqw.dcm.TreatmentHistory

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.Appointment.Appointment_List
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Appointment.Create_Appointment
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.appointment_list.*
import kotlinx.android.synthetic.main.consultation_reply.*
import kotlinx.android.synthetic.main.create_appointment.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.title_bar.*
import kotlinx.android.synthetic.main.view_appointment.*
import kotlinx.android.synthetic.main.view_treatment.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class ViewNCreate_Treatment_History: AppCompatActivity() {
    companion object {
        val TAG: String = ViewNCreate_Treatment_History::class.java.simpleName
    }

    private var db = Firebase.firestore
    lateinit var sharedPreferences: SharedPreferences
    var PREFS_KEY = "prefs"
    var USERID_KEY = "uid"
    var ROLE_KEY = "role"
    var sp_uid = ""
    var sp_role = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_treatment)

        //init setting
        tvTitle.text = "Treatment History"
        ibHistory.setImageResource(R.drawable.history_orange)
        ibPatientC.setImageResource(R.drawable.patient_orange)

        //variables
        var patientID:String?=null
        var userID:String?=null
        var patientName:String?=null
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val timeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        val today = LocalDateTime.now().format(dateFormat)
        etTreatmentDate.setText(today)
        var doctorNameList: ArrayList<String>
        var timeItems = listOf("08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
            "12:00 PM", "12:30 PM", "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM",
            "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM", "07:00 PM", "07:30 PM", "08:00 PM")
        doctorNameList = arrayListOf()
        var adapterTimeItems: ArrayAdapter<String>
        var adapterDocNameItems: ArrayAdapter<String>
        var selectedDate: LocalDate?=null
        var treatTime: LocalTime?=null
        var selectedDoc:String?=null
        var selectedDoctorID:String?=null
        var action:String?=null
        var bunTreatmentID:String?=null
        var bunPatientID:String?=null
        var bunTreatmentDate:String?=null
        var bunTreatmentTime:String?=null
        var bunTreatmentName:String?=null
        var bunTreatmentDocName:String?=null
        var bunTreatmentRemark:String?=null
        var bunTreatmentPres:String?=null
        var bunTreatmentDetail:String?=null

        if(sp_role=="Doctor"||sp_role=="Assistant"){
            mnPatientTreatVC.visibility = View.INVISIBLE
        }
        else{
            mnClinicTreatVC.visibility = View.INVISIBLE
            btnSaveTreatment.visibility = View.INVISIBLE
            tvPatientName.visibility = View.INVISIBLE
        }

        val bundle: Bundle? = intent.extras

        bundle?.let {
            patientID = bundle.getString("msgPatientID")
            action = bundle.getString("viewNEdit_message")
            bunTreatmentID = bundle.getString("treatmentID_message")
            bunPatientID = bundle.getString("patientID_message")
            bunTreatmentDate = bundle.getString("treatmentDate_message")
            bunTreatmentTime = bundle.getString("treatmentTime_message")
            bunTreatmentName = bundle.getString("treatmentName_message")
            bunTreatmentDocName = bundle.getString("treatmentDocName_message")
            bunTreatmentRemark = bundle.getString("treatmentRemark_message")
            bunTreatmentPres = bundle.getString("treatmentPres_message")
            bunTreatmentDetail = bundle.getString("treatmentDetail_message")
        }

        adapterTimeItems = ArrayAdapter(this, R.layout.list_item_ddl, timeItems)
        adapterDocNameItems = ArrayAdapter(this, R.layout.list_item_ddl, doctorNameList)
        ddlTreatTime.setAdapter(adapterTimeItems)
        ddlDocInChargeTreat.setAdapter(adapterDocNameItems)
        ddlTreatTime.setText(adapterTimeItems.getItem(0), false)
        //ddlDocInChargeTreat.setText(adapterDocNameItems.getItem(0), false)

        db = FirebaseFirestore.getInstance()

        if(action=="viewNEdit"){
            if(sp_role=="Patient"||sp_role=="Assistant"){
                btnSaveTreatment.visibility = View.GONE
                etTreatmentDate.isEnabled = false
                ddlTreatTime.isEnabled = false
                etTreatmentName.isEnabled = false
                ddlDocInChargeTreat.isEnabled = false
                etRemark.isEnabled = false
                etPrescription.isEnabled = false
                etTreatmentDetail.isEnabled = false

                ddlTreatTime.setAdapter(null)
                ddlDocInChargeTreat.setAdapter(null)

            }
            btnSaveTreatment.text = "Update"



                val refPatient = db.collection("Patient").document(bunPatientID.toString())
                refPatient.get().addOnSuccessListener {
                    if (it != null) {
                        userID = it.data?.get("user_ID").toString()
                        val refUser = db.collection("User").document(userID.toString())
                        refUser.get().addOnSuccessListener {
                            if (it != null) {
                                patientName = it.data?.get("user_first_name")
                                    .toString() + " " + it.data?.get("user_last_name").toString()
                                tvPatientName.text = patientName
                            }
                        }
                            .addOnFailureListener {
                                Log.d(TAG, "retrieve user failed")
                            }
                    }
                }.addOnFailureListener {
                    Log.d(TAG, "retrieve patient failed")
                }


            etTreatmentDate.setText(bunTreatmentDate)
            //ddlTreatTime.setText(bunTreatmentTime)
//            if(savedInstanceState ==null){
//                ddlTreatTime.setText(bunTreatmentTime)
//            }
            //adapterTimeItems.filter.filter(null)
            ddlTreatTime.setText(bunTreatmentTime,false)

            etTreatmentName.setText(bunTreatmentName)
            ddlDocInChargeTreat.setText("Dr "+bunTreatmentDocName, false)
            etRemark.setText(bunTreatmentRemark)
            etPrescription.setText(bunTreatmentPres)
            etTreatmentDetail.setText(bunTreatmentDetail)
//            ddlDocInChargeTreat.setAdapter(adapterDocNameItems)
//            ddlTreatTime.setAdapter(adapterTimeItems)

        }else{
            Log.d(TAG, "patient id is2 "+patientID)
            val refPatient = db.collection("Patient").document(patientID.toString())
            refPatient.get().addOnSuccessListener {
                if (it != null) {
                    userID = it.data?.get("user_ID").toString()
                    val refUser = db.collection("User").document(userID.toString())
                    refUser.get().addOnSuccessListener {
                        if (it != null) {
                            patientName = it.data?.get("user_first_name")
                                .toString() + " " + it.data?.get("user_last_name").toString()
                            tvPatientName.text = patientName
                        }
                    }
                        .addOnFailureListener {
                            Log.d(TAG, "retrieve user failed")
                        }
                }
            }.addOnFailureListener {
                Log.d(TAG, "retrieve patient failed")
            }
        }



        db.collection("User").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (user in it.documents) {
                        val role = user.data?.get("user_role").toString()
                        if(role=="Doctor"){
                            val doctorName = user.data?.get("user_first_name").toString()+" "+user.data?.get("user_last_name").toString()
                            doctorNameList.add("Dr "+doctorName)
                            if(doctorNameList!=null&&doctorNameList.size>0){
                                Log.d(Create_Appointment.TAG, "not null n >0")
                            }
                            if(doctorNameList!=null&&doctorNameList.size>0){
                                val firstDoc = doctorNameList[0]
                                Log.d(Create_Appointment.TAG, firstDoc)
                                ddlDocInChargeTreat.setText(firstDoc,false)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed retrieve user")
            }



        etTreatmentDate.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
                if(mDay<10){
                    if (mMonth+1<10){
                        etTreatmentDate.setText("0"+mDay+"-0"+(mMonth+1)+"-"+mYear)
                    }else if(mMonth+1>=10){
                        etTreatmentDate.setText("0"+mDay+"-"+(mMonth+1)+"-"+mYear)
                    }
                }
                else{
                    if ((mMonth+1)<10){
                        etTreatmentDate.setText(""+mDay+"-0"+(mMonth+1)+"-"+mYear)
                    }
                    else {
                        etTreatmentDate.setText("" + mDay + "-" + (mMonth + 1) + "-" + mYear)
                    }
                }

                selectedDate = LocalDate.parse(etTreatmentDate.text.toString(), dateFormat)
            },year, month, day)
            dpd.show()
        }

        btnSaveTreatment.setOnClickListener {
            treatTime = LocalTime.parse(ddlTreatTime.text.toString(), timeFormat)
            selectedDate = LocalDate.parse(etTreatmentDate.text.toString(), dateFormat)
            val treatmentName = etTreatmentName.text.toString()
            val selectedDoc = ddlDocInChargeTreat.text.toString()?.substring(3)
            val remark = etRemark.text.toString()
            val pres = etPrescription.text.toString()
            val treatmentDetail = etTreatmentDetail.text.toString()

            val compareDate = selectedDate?.compareTo(LocalDate.now())
            tilTreatDate.helperText = compareDate?.let { it1 -> validSelectedDate(it1) }

            db.collection("User").get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        for (user in it.documents) {
                            val role = user.data?.get("user_role").toString()
                            if (role == "Doctor") {
                                val doctorName = user.data?.get("user_first_name")
                                    .toString() + " " + user.data?.get("user_last_name").toString()
                                if (doctorName == selectedDoc) {
                                    val userID = user.data?.get("user_ID").toString()
                                    db.collection("Doctor").get()
                                        .addOnSuccessListener {
                                            if (!it.isEmpty) {
                                                for (doctor in it.documents) {
                                                    if (userID == doctor.data?.get("user_ID")
                                                            .toString()
                                                    ) {
                                                        Log.d(TAG, "userid=>"+userID)
                                                        selectedDoctorID =
                                                            doctor.data?.get("doctor_ID").toString()

                                                        if(tilTreatDate.helperText==null){
                                                            if(action=="viewNEdit") {

                                                                Log.d(TAG, "now going to update")
                                                                val updateTreatmentMap = mapOf(
                                                                    "treatment_date" to etTreatmentDate.text.toString(),
                                                                    "treatment_time" to ddlTreatTime.text.toString(),
                                                                    "treatment_name" to treatmentName,
                                                                    "treatment_prescription" to pres,
                                                                    "treatment_detail" to treatmentDetail,
                                                                    "treatment_remark" to remark,
                                                                    "patient_ID" to bunPatientID,
                                                                    "doctor_ID" to selectedDoctorID,
                                                                )
                                                                db.collection("Treatment").document(bunTreatmentID.toString()).update(updateTreatmentMap)

                                                                val intent = Intent(this, Treatment_History_List::class.java)
                                                                intent.putExtra("msgPatientID", bunPatientID)
                                                                startActivity(intent)
                                                                overridePendingTransition(0, 0)
//                                                                finish()

                                                            }else{
                                                                val treatmentID =
                                                                    db.collection("collection_name")
                                                                        .document().id

                                                                val treatmentMap = hashMapOf(
                                                                    "treatment_ID" to treatmentID,
                                                                    "treatment_date" to etTreatmentDate.text.toString(),
                                                                    "treatment_time" to ddlTreatTime.text.toString(),
                                                                    "treatment_name" to treatmentName,
                                                                    "treatment_prescription" to pres,
                                                                    "treatment_detail" to treatmentDetail,
                                                                    "treatment_remark" to remark,
                                                                    "patient_ID" to patientID,
                                                                    "doctor_ID" to selectedDoctorID
                                                                )

                                                                db.collection("Treatment")
                                                                    .document(treatmentID)
                                                                    .set(treatmentMap)
                                                                    .addOnSuccessListener {
                                                                        Log.d(TAG, "Success add treatment")
                                                                        val intent = Intent(this, Treatment_History_List::class.java)
                                                                        intent.putExtra("msgPatientID", patientID)
                                                                        startActivity(intent)
                                                                        overridePendingTransition(0, 0)
//                                                                        finish()
                                                                    }
                                                                    .addOnFailureListener { e ->
                                                                        Log.w(
                                                                            TAG,
                                                                            "failed create treatment"
                                                                        )
                                                                    }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }.addOnFailureListener { Log.d(TAG, "failed retrieve doctor") }
                                }
                            }
                        }
                    }
                }.addOnFailureListener { Log.d(TAG, "failed retrieve user") }
        }

        btnLogout.setOnClickListener{
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnBack.setOnClickListener {
            finish()
        }

        //menu bar button
        ibHome.setOnClickListener{
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
            val intent = Intent(this, Account_Setting::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun validSelectedDate(compareDate:Int):String?{
        if (compareDate>0){
            return "Invalid Selected Time"
        }
        return null
    }
}