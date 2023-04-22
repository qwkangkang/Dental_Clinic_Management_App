package com.kqw.dcm.Appointment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.R
import kotlinx.android.synthetic.main.fragment_pop_up.*
import kotlinx.android.synthetic.main.fragment_pop_up.view.*


class PopUpFragment : DialogFragment() {

    private var db = Firebase.firestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView: View = inflater.inflate(R.layout.fragment_pop_up, container, false)

//        val bundle: Bundle? = intent.context.extras
//
//        bundle?.let {
//            appID = bundle.getString("appID_message")
        val appID = arguments?.getString("keyAppID")
        Log.d(View_Appointment.TAG, "kepAppID is "+appID!!)
        var cancelReason:String?=null

        rootView.btnConfirm.setOnClickListener {
            cancelReason = etReason.text.toString()
            Log.d(View_Appointment.TAG, "reason is "+cancelReason!!)
            Log.d(View_Appointment.TAG, "kepAppID is "+appID!!)
            val updateAppMap = mapOf(
                "appointment_status" to "Cancelled",
                "appointment_cancel_reason" to cancelReason
            )

            db.collection("Appointment").document(appID).update(updateAppMap)
            val intent = Intent(this.context, Appointment_List::class.java)
            startActivity(intent)
            dismiss()
        }

        rootView.btnCancelCanceling.setOnClickListener {
            dismiss()
        }

        return rootView
    }

}