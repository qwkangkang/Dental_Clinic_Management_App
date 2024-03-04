package com.kqw.dcm.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kqw.dcm.R
import kotlinx.android.synthetic.main.fragment_delete_schedule.view.*
import kotlinx.android.synthetic.main.fragment_pop_up.view.*

class PopUpFragmentDeleteSchedule : DialogFragment() {

    private var db = Firebase.firestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView: View = inflater.inflate(R.layout.fragment_delete_schedule, container, false)

        val foundID = arguments?.getString("keyFoundID")

        rootView.btnDeleteSchedule.setOnClickListener {

            db.collection("Schedule").document(foundID.toString()).delete()
            dismiss()
        }
        rootView.btnCancelDelete.setOnClickListener {

            dismiss()
        }

        return rootView
    }

}