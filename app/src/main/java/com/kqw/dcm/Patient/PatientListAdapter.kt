package com.kqw.dcm.Patient

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.internal.Constants
import com.kqw.dcm.AccountSetting.Login
import com.kqw.dcm.R
import android.content.Intent as Intent

class PatientListAdapter(private val patientList: ArrayList<Patient_Data>): RecyclerView.Adapter<PatientListAdapter.PatientViewHolder>() {

    companion object{
        val TAG:String = PatientListAdapter::class.java.simpleName
    }

    inner class PatientViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val tvPatientName : TextView = itemView.findViewById(R.id.tvPatientName)
        val tvGender : TextView = itemView.findViewById(R.id.tvGender)
        val tvAge : TextView = itemView.findViewById(R.id.tvAge)
        val tvContactNo: TextView = itemView.findViewById(R.id.tvContact)

        private var selectedPatient:Patient_Data? = null
        private var currentPosition:Int = 0

//        init {
//            itemView.setOnClickListener{
//                if(selectedPatient==null){
//                    Log.d(PatientListAdapter.TAG, "null")
//                }else {
//                    Log.d(PatientListAdapter.TAG, selectedPatient!!.patientName + "clicked")
//                    selectedPatient?.let {
//                        Toast.makeText(
//                            itemView.context,
//                            selectedPatient!!.patientID + " is selected",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        Log.d(PatientListAdapter.TAG, selectedPatient!!.patientID + " is selected")
//                    }
//                }
//            }
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.patient_list_item, parent, false)
        return PatientViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.tvPatientName.text = patientList[position].patientName
        holder.tvGender.text = patientList[position].gender
        holder.tvAge.text = patientList[position].age.toString()
        holder.tvContactNo.text = patientList[position].contactNo


        holder.itemView.setOnClickListener {
            Log.d(PatientListAdapter.TAG, patientList[position].patientID + " is clicked")
            val msg : String = patientList[position].patientID
            val intent = Intent(holder.itemView.context, Patient::class.java)
            intent.putExtra("user_message", msg)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return patientList.size
    }

    fun refreshDataSet(){

    }
}