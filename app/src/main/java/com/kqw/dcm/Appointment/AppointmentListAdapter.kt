package com.kqw.dcm.Appointment

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kqw.dcm.Patient.Patient
import com.kqw.dcm.Patient.PatientListAdapter
import com.kqw.dcm.Patient.Patient_Data
import com.kqw.dcm.R

class AppointmentListAdapter(private val appList: ArrayList<Appointment_Data>): RecyclerView.Adapter<AppointmentListAdapter.AppointmentViewHolder>() {
    companion object{
        val TAG:String = PatientListAdapter::class.java.simpleName
    }

    inner class AppointmentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        //val tvPatientName : TextView = itemView.findViewById(R.id.tvPatientName)
        val tvDoctor: TextView = itemView.findViewById(R.id.tvDoctor)
        val tvService: TextView = itemView.findViewById(R.id.tvService)
        val tvAppDate: TextView = itemView.findViewById(R.id.tvAppDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentListAdapter.AppointmentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.appointment_list_item, parent, false)
        return AppointmentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AppointmentListAdapter.AppointmentViewHolder, position: Int) {
        //holder.tvPatientName.text = patientList[position].patientName

        holder.tvDoctor.text = appList[position].docName
        holder.tvService.text = appList[position].service.toString()
        holder.tvAppDate.text = appList[position].appDate.toString()
        holder.tvTime.text = appList[position].appStartTime.toString()
        holder.tvStatus.text = appList[position].status.toString()

        holder.itemView.setOnClickListener {
            Log.d(PatientListAdapter.TAG, appList[position].appID + " is clicked")
            val msgAppID : String = appList[position].appID
            val msgAppDate : String = appList[position].appDate
            val msgAppTime : String = appList[position].appStartTime
            val msgService : String = appList[position].service
            val msgDocName : String = appList[position].docName
            val intent = Intent(holder.itemView.context, View_Appointment::class.java)
            intent.putExtra("appID_message", msgAppID)
            intent.putExtra("appDate_message", msgAppDate)
            intent.putExtra("appTime_message", msgAppTime)
            intent.putExtra("appService_message", msgService)
            intent.putExtra("appDocName_message", msgDocName)
            holder.itemView.context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return appList.size
    }

}