package com.kqw.dcm.Appointment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity

//
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.kqw.dcm.Home.MainActivity_Clinic
import com.kqw.dcm.R
import kotlinx.android.synthetic.main.checkin_scanner.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

//
//
class CheckIn_Scanner : AppCompatActivity(){

    companion object{
        val TAG:String = CheckIn_Scanner::class.java.simpleName
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
        setContentView(R.layout.checkin_scanner)
        scanCode()
//        btnScan.setOnClickListener {
//            scanCode()
//        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scanCode() {
        Log.d(TAG, "Scan code")
        val options = ScanOptions()
        options.setPrompt("Volume Up To Flash On")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        options.setCaptureActivity(CaptureAct::class.java)
        barcodeLauncher.launch(options)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private val barcodeLauncher: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            val originalIntent = result.originalIntent
            if (originalIntent == null) {
                Log.d("CheckIn_Scanner", "Cancelled scan")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                finish()
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Log.d("CheckIn_Scanner", "Cancelled scan due to missing camera permission")
                Toast.makeText(
                    this,
                    "Cancelled due to missing camera permission",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        } else {
            Log.d("CheckIn_Scanner", "Scanned"+ result.contents)
//            Toast.makeText(
//                this,
//                "Scanned: " + result.contents,
//                Toast.LENGTH_LONG
//            ).show()
            val appID = result.contents.toString().trim()
            checkIn(appID)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkIn(appID:String) {
        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val timeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        var today: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
        var ldToday: LocalDate = LocalDateTime.ofInstant(today.toInstant(), today.getTimeZone().toZoneId()).toLocalDate()
        val strToday = ldToday.format(dateFormat)
        var ltNow:LocalTime = LocalDateTime.ofInstant(today.toInstant(), today.getTimeZone().toZoneId()).toLocalTime()
        val strNow = ltNow.format(timeFormat)


        val refApp = db.collection("Appointment").document(appID)
        refApp.get().addOnSuccessListener {
            if (it != null) {
                val appDate = it.data?.get("appointment_date").toString()
                val appTime = it.data?.get("appointment_start_time").toString()
                val appStatus = it.data?.get("appointment_status").toString()


                var timeAppTime = LocalTime.parse(appTime, timeFormat)
                //val timeCurrentTime = LocalTime.parse(strNow, timeFormat)
                timeAppTime = timeAppTime.minusMinutes(30)
                val compareAppTime = (ltNow?.compareTo(timeAppTime))
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
                        .addOnSuccessListener { Log.d(View_Appointment.TAG, "Success") }
                        .addOnFailureListener { e -> Log.w(View_Appointment.TAG, "Error") }
                    Toast.makeText(this, "Check In Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity_Clinic::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }else if(appStatus=="Checked In"){
                    Toast.makeText(this, "Checked In Already", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Invalid Check In", Toast.LENGTH_SHORT).show()
                }

            }

        }
    }

}