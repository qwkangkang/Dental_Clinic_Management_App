package com.kqw.dcm.Appointment

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.kqw.dcm.AccountSetting.Account_Setting
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.Consultation.Consultation_List
import com.kqw.dcm.Home.MainActivity
import com.kqw.dcm.R
import com.kqw.dcm.TreatmentHistory.Treatment_History_List
import com.kqw.dcm.schedule.Schedule_List
import kotlinx.android.synthetic.main.checkin_qrcode.*
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.title_bar.*

class CheckIn_QRcode:AppCompatActivity() {
    companion object{
        val TAG:String = CheckIn_QRcode::class.java.simpleName
    }

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.checkin_qrcode)

        //init setting
        tvTitle.text = "Appointment"
        ibApp.setImageResource(R.drawable.appointment_orange)

        //variables
        var appID:String?=null
        val bundle: Bundle? = intent.extras

        bundle?.let {
            appID = bundle.getString("msgAppID")
        }

        val data = appID

        if(data==null){
            Log.d(TAG, "app id is null")
        }else{
            val writer = QRCodeWriter()
            try{
                val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width){
                    for (y in 0 until height){
                        bmp.setPixel(x,y, if(bitMatrix[x,y]) Color.BLACK else Color.WHITE)
                    }
                }
                ivQRCode.setImageBitmap(bmp)
            }catch (e: WriterException){
                e.printStackTrace()
            }
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
    }
}