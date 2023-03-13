package com.kqw.dcm.Appointment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Consultation.Create_Consultation
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.acount_setting_clinic.*
import kotlinx.android.synthetic.main.appointment_list.*
import kotlinx.android.synthetic.main.change_password.*
import kotlinx.android.synthetic.main.create_appointment.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar_clinic.*
import kotlinx.android.synthetic.main.title_bar.*
import kotlinx.android.synthetic.main.view_appointment.*
import kotlinx.android.synthetic.main.view_appointment.etAppDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class View_Appointment: AppCompatActivity() {
    companion object{
        val TAG:String = View_Appointment::class.java.simpleName
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
        setContentView(R.layout.view_appointment)

        //init setting
        tvTitle.text = "Appointment"
        ibApp.setImageResource(R.drawable.appointment_orange)
        ibHomeC.setImageResource(R.drawable.home_orange)

        //variables
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        sp_uid = sharedPreferences.getString(USERID_KEY, null)!!
        sp_role = sharedPreferences.getString(ROLE_KEY, null)!!
        var appID:String?=null
        var appDate:String?=null
        var appTime:String?=null
        var appService:String?=null
        var appDocName:String?=null
        var appRoom:String?=null
        var appStatus:String?=null
        var patientID:String?=null
        var userID:String?=null
        var patientName:String?=null
        var cancelReason:String?=null
        var roomIDList: ArrayList<String>
        roomIDList = arrayListOf()
        var adapterRoomItems: ArrayAdapter<String>
        var selectedRoom:String?=null
        adapterRoomItems = ArrayAdapter(this, R.layout.list_item_ddl, roomIDList)
        ddlRoomView.setAdapter(adapterRoomItems)
        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")
//        val getToday = LocalDateTime.now().plusHours(9).minusMinutes(28)
//        val today = getToday.format(dateFormat)
//        val currentTime = getToday.format(timeFormat)
        var today: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
        var ldToday: LocalDate = LocalDateTime.ofInstant(today.toInstant(), today.getTimeZone().toZoneId()).toLocalDate()
        Log.d(MainActivity_Clinic.TAG, "today is "+ldToday)
        val ltNow:LocalTime = LocalDateTime.ofInstant(today.toInstant(), today.getTimeZone().toZoneId()).toLocalTime()
        val strToday = ldToday.format(dateFormat)
        val strNow = ltNow.format(timeFormat)

        val bundle: Bundle? = intent.extras

        bundle?.let {
            patientID = bundle.getString("msgPatientID")
            appID = bundle.getString("appID_message")
            appDate = bundle.getString("appDate_message")
            appTime = bundle.getString("appTime_message")
            appService = bundle.getString("appService_message")
            appDocName = bundle.getString("appDocName_message")
            appRoom = bundle.getString("appRoom_message")
            appStatus = bundle.getString("appStatus_message")
            cancelReason = bundle.getString("appCancelReason_message")
        }

        db = FirebaseFirestore.getInstance()

        if(sp_role=="Doctor"||sp_role=="Assistant"){
            Log.d(TAG, "patient ID:"+patientID)
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
                            Log.d(Create_Appointment.TAG, "retrieve user failed")
                        }
                }
            }.addOnFailureListener {
                Log.d(Create_Appointment.TAG, "retrieve patient failed")
            }
        }else{
            tvPatientName.text = ""
        }

        if ((sp_role=="Doctor"||sp_role=="Assistant")&&appRoom==""){
            db.collection("Room").get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        for (room in it.documents) {
                            val roomID = room.data?.get("room_ID").toString()
                            roomIDList.add(roomID)

                        }
                        if(roomIDList!=null&&roomIDList.size>0){
                            val firstRoom = roomIDList[0].toString()
                            ddlRoomView.setText(firstRoom,false)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d(Create_Appointment.TAG, "failed retrieve room")
                }
        }else{
            ddlRoomView.setText(appRoom)
        }


        etAppDate.setText(appDate)
        etAppTime.setText(appTime)
        etService.setText(appService)
        etDocInCharge.setText(appDocName)

        //ddlRoomView.setText(appRoom)
        etAppStatus.setText(appStatus)
        if(cancelReason!=""){
            etAppStatus.setText(appStatus+" - "+cancelReason)
        }


        if(sp_role=="Doctor"||sp_role=="Assistant"){
            mnPatientViewApp.visibility = View.INVISIBLE
            btnQR.visibility = View.GONE
        }
        else{
            mnClinicViewApp.visibility = View.INVISIBLE
            btnUpdateRoom.visibility = View.GONE
            btnCheckInManual.visibility = View.GONE
        }


        btnBack.setOnClickListener {
            finish()
        }

        btnLogout.setOnClickListener {
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        btnCancel.setOnClickListener {
            if(appStatus=="Pending"||appStatus=="Confirmed"){
                var dialog = PopUpFragment()
                val args = Bundle()
                args.putString("keyAppID", appID)
                dialog.arguments = args
                dialog.show(supportFragmentManager, "customDialog")
                overridePendingTransition(0, 0)
            }else{
                Toast.makeText(this, "This Appointment Cannot Be Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        btnUpdateRoom.setOnClickListener {

            val updateAppointmentMap = mapOf(
                "room_ID" to ddlRoomView.text.toString(),
                "appointment_status" to "Confirmed"
            )
            db.collection("Appointment").document(appID.toString()).update(updateAppointmentMap)
            finish()
            overridePendingTransition(0, 0)
            startActivity(getIntent())
            overridePendingTransition(0, 0)
        }

        btnQR.setOnClickListener {
            if(appStatus!="Confirmed"){
                Toast.makeText(this, "Please Wait Appointment To Be Confirmed", Toast.LENGTH_SHORT).show()
            }
            else{
                val intent = Intent(this, CheckIn_QRcode::class.java)
                intent.putExtra("msgAppID", appID)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }

        btnCheckInManual.setOnClickListener {
            val timeAppTime = LocalTime.parse(appTime, timeFormat)
            val timeCurrentTime = LocalTime.parse(strNow, timeFormat)
            val compareAppTime = (timeAppTime?.compareTo(timeCurrentTime))
            Log.d(TAG, timeAppTime.toString()+" vs "+timeCurrentTime.toString())
            Log.d(TAG, " today date "+strToday.toString())
            Log.d(TAG, " current time "+strNow.toString())
            if(compareAppTime!!>=0&&appDate==strToday&&appStatus=="Confirmed") {
                val updateAppMap = mapOf(
                    "appointment_status" to "Checked In"
                )
                db.collection("Appointment").document(appID.toString()).update(updateAppMap)

                val checkInID = db.collection("collection_name").document().id
                val checkInMap = hashMapOf(
                    "check_in_ID" to checkInID,
                    "check_in_date" to strToday,
                    "consultation_time" to strNow,
                    "appointment_ID" to appID
                )
                db.collection("Check In").document(checkInID)
                    .set(checkInMap)
                    .addOnSuccessListener { Log.d(TAG, "Success") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error") }
                Toast.makeText(this, "Check In Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Appointment_List::class.java)
                intent.putExtra("msgPatientID", patientID)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }else if(appStatus=="Checked In"){
                Toast.makeText(this, "Checked In Already", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Invalid Check In", Toast.LENGTH_SHORT).show()
            }
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
}