package com.kqw.dcm.Appointment


import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
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
import androidx.core.content.ContextCompat.getSystemService
import com.allyants.notifyme.NotifyMe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.Patient.Patient_List
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Edit_Schedule
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.appointment_list.*
import kotlinx.android.synthetic.main.appointment_list.mnClinic
import kotlinx.android.synthetic.main.appointment_list.mnPatient
import kotlinx.android.synthetic.main.create_appointment.*
import kotlinx.android.synthetic.main.edit_schedule.*
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


class Create_Appointment:AppCompatActivity() {
    companion object{
        val TAG:String = Create_Appointment::class.java.simpleName
    }

    private var db = Firebase.firestore
    lateinit var sharedPreferences: SharedPreferences
    var PREFS_KEY = "prefs"
    var USERID_KEY = "uid"
    var ROLE_KEY = "role"
    var sp_uid = ""
    var sp_role = ""
    @RequiresApi(Build.VERSION_CODES.O)
    val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    @RequiresApi(Build.VERSION_CODES.O)
    val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_appointment)
        AndroidThreeTen.init(this)

        //init setting
        tvTitle.text = "Appointment"
        ibApp.setImageResource(R.drawable.appointment_orange)


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
        val today = LocalDateTime.now().format(dateFormat)
        etAppDate.setText(today)
        var doctorNameList: ArrayList<String>
        var roomIDList: ArrayList<String>
        var timeItems = listOf("08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
            "12:00 PM", "12:30 PM", "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM",
            "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM", "07:00 PM", "07:30 PM")
        var serviceItems = listOf("Checkup (30mins)", "Scaling (30mins)", "Filling (1hr)", "Extraction (2hrs)")
        doctorNameList = arrayListOf()
        roomIDList = arrayListOf()
        var adapterTimeItems: ArrayAdapter<String>
        var adapterServiceItems: ArrayAdapter<String>
        var adapterDocNameItems: ArrayAdapter<String>
        var adapterRoomItems: ArrayAdapter<String>
        var selectedDate:LocalDate?=null
        var appStartTime:LocalTime?=null
        var selectedService:String?=null
        var duration:Int?=null
        var appEndTime:LocalTime?=null
        var selectedDoc:String?=null
        var selectedRoom:String?=null
        var selectedDoctorID:String?=null
        var btnCreateClick:Boolean
        var avai:Boolean

        if(sp_role=="Doctor"||sp_role=="Assistant"){
            mnPatient.visibility = View.INVISIBLE
        }
        else{
            mnClinic.visibility = View.INVISIBLE
        }

        val bundle: Bundle? = intent.extras

        bundle?.let {
            patientID = bundle.getString("msgPatientID")
            avai = bundle.getBoolean("msgAvai")
        }

        db = FirebaseFirestore.getInstance()

        if(sp_role=="Doctor"||sp_role=="Assistant"){
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
            tvRoom.visibility = View.GONE
            ddlRoom.visibility = View.GONE
            tilRoom.visibility = View.GONE
        }


        adapterTimeItems = ArrayAdapter(this, R.layout.list_item_ddl, timeItems)
        adapterServiceItems = ArrayAdapter(this, R.layout.list_item_ddl, serviceItems)
        adapterDocNameItems = ArrayAdapter(this, R.layout.list_item_ddl, doctorNameList)
        adapterRoomItems = ArrayAdapter(this, R.layout.list_item_ddl, roomIDList)
        ddlAppTime.setAdapter(adapterTimeItems)
        ddlService.setAdapter(adapterServiceItems)
        ddlDocInCharge.setAdapter(adapterDocNameItems)
        ddlRoom.setAdapter(adapterRoomItems)
        ddlAppTime.setText(adapterTimeItems.getItem(0), false)
        ddlService.setText(adapterServiceItems.getItem(0), false)


        db.collection("Room").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (room in it.documents) {
                        val roomID = room.data?.get("room_ID").toString()
                        roomIDList.add(roomID)

                        if(roomIDList!=null&&roomIDList.size>0){
                            val firstRoom = roomIDList[0].toString()
                            ddlRoom.setText(firstRoom,false)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.d(Create_Appointment.TAG, "failed retrieve room")
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
                                Log.d(TAG, "not null n >0")
                            }
                            if(doctorNameList!=null&&doctorNameList.size>0){
//
                                val firstDoc = doctorNameList[0]
                                Log.d(TAG, firstDoc)
                                ddlDocInCharge.setText(firstDoc,false)
                            }
                        }

                    }
                }
            }
            .addOnFailureListener {
                Log.d(Appointment_List.TAG, "failed retrieve user")
            }



//        if(btnCreate.isPressed){
//            if(avai==false){
//                Toast.makeText(this, "Failed Making Appointment. Please Check Schedule", Toast.LENGTH_SHORT).show()
//                Log.d(TAG, avai.toString())
//            }
//        }




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

        etAppDate.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
                if(mDay<10){
                    if (mMonth+1<10){
                        etAppDate.setText("0"+mDay+"-0"+(mMonth+1)+"-"+mYear)
                    }else if(mMonth+1>=10){
                        etAppDate.setText("0"+mDay+"-"+(mMonth+1)+"-"+mYear)
                    }
                }
                else{
                    if ((mMonth+1)<10){
                        etAppDate.setText(""+mDay+"-0"+(mMonth+1)+"-"+mYear)
                    }
                    else {
                        etAppDate.setText("" + mDay + "-" + (mMonth + 1) + "-" + mYear)
                    }
                }

                selectedDate = LocalDate.parse(etAppDate.text.toString(), dateFormat)
            },year, month, day)
            dpd.show()
        }

        btnCreate.setOnClickListener {
            //tilBtnCreate.helperText = "Failed Making Appointment. Please Check Schedule"
            appStartTime = LocalTime.parse(ddlAppTime.text.toString(), timeFormat)
            selectedService = ddlService.text.toString()
            selectedRoom = ddlRoom.text.toString()
            selectedDate = LocalDate.parse(etAppDate.text.toString(), dateFormat)
            if (selectedService=="Checkup (30mins)"||selectedService=="Scaling (30mins)"){
                appEndTime = appStartTime?.plusMinutes(30)
            }
            else if (selectedService=="Filling (1hr)"){
                appEndTime = appStartTime?.plusMinutes(60)
            }
            else if (selectedService=="Extraction (2hrs)"){
                appEndTime = appStartTime?.plusMinutes(120)
            }

            val compareDate = selectedDate?.compareTo(LocalDate.now())
            tilAppDate.helperText = compareDate?.let { it1 -> validSelectedDate(it1) }

            selectedDoc = ddlDocInCharge.text.toString()
            val strDoctorName = selectedDoc?.substring(3)

            db.collection("User").get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        for (user in it.documents) {
                            val role = user.data?.get("user_role").toString()
                            if(role=="Doctor"){
                                val doctorName = user.data?.get("user_first_name").toString()+" "+user.data?.get("user_last_name").toString()
                                if(doctorName==strDoctorName) {
                                    val userID = user.data?.get("user_ID").toString()
                                    db.collection("Doctor").get()
                                        .addOnSuccessListener {
                                            if (!it.isEmpty) {
                                                for (doctor in it.documents) {
                                                    if (userID == doctor.data?.get("user_ID").toString()
                                                    ) {
                                                        selectedDoctorID = doctor.data?.get("doctor_ID").toString()

                                                        //paste
                                                        var availableDate:LocalDate?=null
                                                        var availableStartTime:LocalTime?=null
                                                        var availableEndTime:LocalTime?=null
                                                        Log.d(Create_Appointment.TAG, "2. selected docID: "+selectedDoctorID)
                                                        db.collection("Schedule").get()
                                                            .addOnSuccessListener {
                                                                if (!it.isEmpty) {
                                                                    for (schedule in it.documents) {
                                                                        val sDoctorID = schedule.data?.get("doctor_ID").toString()
                                                                        if (sDoctorID == selectedDoctorID) {
                                                                            val sStrScheduleDate = schedule.data?.get("schedule_date").toString()
                                                                            availableDate = LocalDate.parse(sStrScheduleDate, dateFormat)
                                                                            if (availableDate == selectedDate) {
                                                                                if (schedule.data?.get("schedule_status")
                                                                                        .toString() == "Available"
                                                                                ) {
                                                                                    val strScheduleStartTime =
                                                                                        schedule.data?.get("schedule_start_time").toString()
                                                                                    availableStartTime =
                                                                                        LocalTime.parse(strScheduleStartTime, timeFormat)
                                                                                    val strScheduleEndTime =
                                                                                        schedule.data?.get("schedule_end_time").toString()
                                                                                    availableEndTime =
                                                                                        LocalTime.parse(strScheduleEndTime, timeFormat)

                                                                                    Log.d(Create_Appointment.TAG, "retrieve start time=>"+strScheduleStartTime)
                                                                                    Log.d(Create_Appointment.TAG, "retrieve end time=>"+strScheduleEndTime)

//                                                                                    var valid:Boolean=checkAppTimeAvailability(availableStartTime, availableEndTime, appStartTime, appEndTime)
                                                                                    if(checkAppTimeAvailability(availableStartTime, availableEndTime, appStartTime, appEndTime)){

                                                                                        val refRoom = db.collection("Room").document(selectedRoom.toString())
                                                                                        var status:String
                                                                                        Log.d(Create_Appointment.TAG, "selected room "+selectedRoom)
                                                                                        refRoom.get().addOnSuccessListener {
                                                                                            if(it!=null) {
                                                                                                status = it.data?.get("room_status").toString()
                                                                                                Log.d(Create_Appointment.TAG, "room status "+status)

                                                                                                if(status=="Available"){
                                                                                                    Log.d(Create_Appointment.TAG, "All is available after retrieve")
                                                                                                    //test
                                                                                                    val scheduleID = schedule.data?.get("schedule_ID").toString()
                                                                                                    Log.d(Create_Appointment.TAG, "date"+etAppDate.text.toString())
                                                                                                    Log.d(Create_Appointment.TAG, "start"+appStartTime?.format(timeFormat))
                                                                                                    createAppointment(etAppDate.text.toString(), appStartTime?.format(timeFormat),
                                                                                                        appEndTime?.format(timeFormat), selectedRoom, patientID, scheduleID, selectedService)
                                                                                                    updateSchedule(scheduleID, availableStartTime, availableEndTime, appStartTime, appEndTime, sStrScheduleDate, sDoctorID)
                                                                                                    setNotification(selectedDate)
                                                                                                    //end test
                                                                                                    avai = true
                                                                                                   // tilBtnCreate.helperText = ""
                                                                                                    Log.d(TAG, avai.toString())
                                                                                                }
                                                                                                else{
                                                                                                    Log.d(Create_Appointment.TAG, "room is not available")
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        break
                                                                                    }else{
                                                                                        Log.d(Create_Appointment.TAG, "time is not within")
                                                                                        Log.d(Create_Appointment.TAG, "S1 "+appStartTime+" vs S2 "+availableStartTime)
                                                                                        Log.d(Create_Appointment.TAG, "E1 "+appEndTime+" vs E2 "+availableEndTime)
                                                                                    }

                                                                                } else {
                                                                                    Log.d(Create_Appointment.TAG, "status not available")
                                                                                }
                                                                            } else {
                                                                                Log.d(Create_Appointment.TAG, "date not available")
                                                                                Log.d(Create_Appointment.TAG, "Date1 "+selectedDate.toString()+" vs Date2"+availableDate.toString())
                                                                            }
                                                                        } else {
                                                                            Log.d(Create_Appointment.TAG, sDoctorID+" vs "+selectedDoctorID)
                                                                            Log.d(Create_Appointment.TAG, "doctor not available")
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            .addOnFailureListener {
                                                                Log.d(Create_Appointment.TAG, "failed retrieve schedule")
                                                            }
                                                        //end paste
                                                    }
                                                }

                                            }
                                        }
                                        .addOnFailureListener {
                                            Log.d(Create_Appointment.TAG, "failed retrieve doctor")
                                        }

                                }
                            }
                        }
//                        if(avai==false){
//                            Toast.makeText(this, "Failed Making Appointment. Please Check Schedule", Toast.LENGTH_SHORT).show()
//                            Log.d(TAG, avai.toString())
//                        }

                    }

                }
                .addOnFailureListener {
                    Log.d(Create_Appointment.TAG, "failed retrieve user")
                }


//            if(avai==false){
//                Toast.makeText(this, "Failed Making Appointment. Please Check Schedule", Toast.LENGTH_SHORT).show()
//                Log.d(TAG, avai.toString())
//            }
//            val intent = Intent(this, Create_Appointment::class.java)
//            intent.putExtra("msgAvai", )
//            startActivity(intent)
//            overridePendingTransition(0, 0)

        }




        //menu bar button
        ibHome.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        ibApp.setOnClickListener{
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNotification(selectedDate: LocalDate?) {
//        Log.d(TAG, "building notification")
//        val notifyMe: NotifyMe.Builder = NotifyMe.Builder(applicationContext)
        var timer: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
        val year:Int = selectedDate!!.year
        val month:Int = selectedDate!!.monthValue
        val day:Int = selectedDate!!.dayOfMonth
        timer.set(Calendar.YEAR, year)
        timer.set(Calendar.MONTH, month)
        timer.set(Calendar.DAY_OF_MONTH, day-3)
        timer.set(Calendar.HOUR_OF_DAY, 0)
//        timer.set(Calendar.HOUR,9)
//        timer.set(Calendar.AM_PM, Calendar.PM)
        timer.set(Calendar.MINUTE, 0)
        timer.set(Calendar.SECOND, 0)
        Log.d(TAG, "timer: "+timer.toString())

        //three day before
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AppAlarm::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE )
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timer.timeInMillis, pendingIntent)

        //appointment day
        timer.set(Calendar.DAY_OF_MONTH, day)
        val alarmManager2 = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent2 = Intent(this, AppAlarm::class.java)
        val pendingIntent2 = PendingIntent.getBroadcast(this, 0, intent2, PendingIntent.FLAG_IMMUTABLE )
        alarmManager2.setExact(AlarmManager.RTC_WAKEUP, timer.timeInMillis, pendingIntent2)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateSchedule(
        scheduleID: String,
        availableStartTime: LocalTime?,
        availableEndTime: LocalTime?,
        appStartTime: LocalTime?,
        appEndTime: LocalTime?,
        sStrScheduleDate: String,
        sDoctorID: String
    ) {

        if(appStartTime?.compareTo(availableStartTime)==0 && appEndTime?.compareTo(availableEndTime)==0){
            val updateScheduleMap = mapOf(
                "schedule_status" to "Booked"
            )
            db.collection("Schedule").document(scheduleID).update(updateScheduleMap)
        }else if(appStartTime?.compareTo(availableStartTime)==0 && appEndTime?.compareTo(availableEndTime)!!<0){
            val updateScheduleMap = mapOf(
                "schedule_status" to "Booked",
                "schedule_end_time" to appEndTime.format(timeFormat)
            )
            db.collection("Schedule").document(scheduleID).update(updateScheduleMap)

            //create a new one
            val id = db.collection("collection_name").document().id

            val scheduleMap = hashMapOf(
                "schedule_ID" to id,
                "schedule_date" to sStrScheduleDate,
                "schedule_start_time" to appEndTime.format(timeFormat),
                "schedule_end_time" to availableEndTime?.format(timeFormat),
                "schedule_status" to "Available",
                "doctor_ID" to sDoctorID
            )
            db.collection("Schedule").document(id)
                .set(scheduleMap)
                .addOnSuccessListener { Log.d(Edit_Schedule.TAG, "Success add schedule") }
                .addOnFailureListener { e -> Log.w(Edit_Schedule.TAG, "Error") }

        }else if (appStartTime?.compareTo(availableStartTime)!!>0 && appEndTime?.compareTo(availableEndTime)!!<0){
            val updateScheduleMap = mapOf(
                "schedule_status" to "Booked",
                "schedule_start_time" to appStartTime.format(timeFormat),
                "schedule_end_time" to appEndTime.format(timeFormat)
            )
            db.collection("Schedule").document(scheduleID).update(updateScheduleMap)

            //create 2 new
            val id = db.collection("collection_name").document().id

            val scheduleMap = hashMapOf(
                "schedule_ID" to id,
                "schedule_date" to sStrScheduleDate,
                "schedule_start_time" to availableStartTime?.format(timeFormat),
                "schedule_end_time" to appStartTime?.format(timeFormat),
                "schedule_status" to "Available",
                "doctor_ID" to sDoctorID
            )
            db.collection("Schedule").document(id)
                .set(scheduleMap)
                .addOnSuccessListener { Log.d(Edit_Schedule.TAG, "Success add schedule") }
                .addOnFailureListener { e -> Log.w(Edit_Schedule.TAG, "Error") }


            val id2 = db.collection("collection_name").document().id

            val scheduleMap2 = hashMapOf(
                "schedule_ID" to id2,
                "schedule_date" to sStrScheduleDate,
                "schedule_start_time" to appEndTime.format(timeFormat),
                "schedule_end_time" to availableEndTime?.format(timeFormat),
                "schedule_status" to "Available",
                "doctor_ID" to sDoctorID
            )
            db.collection("Schedule").document(id2)
                .set(scheduleMap2)
                .addOnSuccessListener { Log.d(Edit_Schedule.TAG, "Success add schedule") }
                .addOnFailureListener { e -> Log.w(Edit_Schedule.TAG, "Error") }
        }else if(appStartTime?.compareTo(availableStartTime)!!>0 && appEndTime?.compareTo(availableEndTime)==0){
            val updateScheduleMap = mapOf(
                "schedule_status" to "Booked",
                "schedule_start_time" to appStartTime.format(timeFormat),
                "schedule_end_time" to appEndTime.format(timeFormat)
            )
            db.collection("Schedule").document(scheduleID).update(updateScheduleMap)

            val id = db.collection("collection_name").document().id

            val scheduleMap = hashMapOf(
                "schedule_ID" to id,
                "schedule_date" to sStrScheduleDate,
                "schedule_start_time" to availableStartTime?.format(timeFormat),
                "schedule_end_time" to appStartTime?.format(timeFormat),
                "schedule_status" to "Available",
                "doctor_ID" to sDoctorID
            )
            db.collection("Schedule").document(id)
                .set(scheduleMap)
                .addOnSuccessListener { Log.d(Edit_Schedule.TAG, "Success add schedule") }
                .addOnFailureListener { e -> Log.w(Edit_Schedule.TAG, "Error") }
        }
    }


    private fun validSelectedDate(compareDate:Int):String?{
        if (compareDate<0){
            return "Invalid Selected Time"
        }
        return null
    }
    private fun checkAppTimeAvailability(availableStartTime:LocalTime?, availableEndTime:LocalTime?, appStartTime:LocalTime?, appEndTime:LocalTime?):Boolean{
        val compareStartTime = (appStartTime?.compareTo(availableStartTime))
        val compareEndTime = (appEndTime?.compareTo(availableEndTime))

        if(compareStartTime!!>=0&&compareEndTime!!<=0) {
            return true
        }
        return false
    }

    private fun createAppointment(appDate: String?, appStartTime: String?,appEndTime: String?, selectedRoom:String?, patientID:String?, scheduleID:String?, service:String?){
        if(tilAppDate.helperText==null){

            val appID = db.collection("collection_name").document().id

            val appMap = hashMapOf(
                "appointment_ID" to appID,
                "appointment_status" to "Confirmed",
                "appointment_date" to appDate,
                "appointment_start_time" to appStartTime,
                "appointment_end_time" to appEndTime,
                "appointment_service" to service,
                "appointment_cancel_reason" to "",
                "room_ID" to selectedRoom,
                "patient_ID" to patientID,
                "schedule_ID" to scheduleID
            )
            val appMapP = hashMapOf(
                "appointment_ID" to appID,
                "appointment_status" to "Pending",
                "appointment_date" to appDate,
                "appointment_start_time" to appStartTime,
                "appointment_end_time" to appEndTime,
                "appointment_service" to service,
                "appointment_cancel_reason" to "",
                "room_ID" to "",
                "patient_ID" to patientID,
                "schedule_ID" to scheduleID
            )



            if(sp_role=="Doctor"||sp_role=="Assistant"){
                db.collection("Appointment").document(appID)
                    .set(appMap)
                    .addOnSuccessListener { Log.d(Create_Appointment.TAG, "Success add appointment")
                        //finish()
                        val intent = Intent(this, Appointment_List::class.java)
                        intent.putExtra("msgPatientID", patientID)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
//                    val updateSchedule
                    }
                    .addOnFailureListener { e -> Log.w(Create_Appointment.TAG, "failed create app") }
            }else{
                Log.d(TAG, "pat id :"+patientID)
                db.collection("Appointment").document(appID)
                    .set(appMapP)
                    .addOnSuccessListener { Log.d(Create_Appointment.TAG, "Success add appointment")
                        //finish()
                        Log.d(TAG, "pat id :"+patientID)
                        val intent = Intent(this, Appointment_List::class.java)
                        //intent.putExtra("msgPatientID", patientID)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e -> Log.w(Create_Appointment.TAG, "failed create app") }
            }


        }

    }
}