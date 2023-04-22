package com.kqw.dcm.Appointment

import com.kqw.dcm.R
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kqw.dcm.Patient.PatientListAdapter



class AppointmentListAdapter(private val appList: List<Appointment_Data>): RecyclerView.Adapter<AppointmentListAdapter.AppointmentViewHolder>() {
    companion object{
        val TAG:String = AppointmentListAdapter::class.java.simpleName
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
            val msgPatientID :String = appList[position].patientID
            val msgAppID : String = appList[position].appID
            val msgAppDate : String = appList[position].appDate
            val msgAppTime : String = appList[position].appStartTime
            val msgService : String = appList[position].service
            val msgDocName : String = appList[position].docName
            val msgRoom : String = appList[position].room
            val msgStatus : String = appList[position].status
            val msgCancelReason :String = appList[position].cancelReason
            val intent = Intent(holder.itemView.context, View_Appointment::class.java)
            intent.putExtra("msgPatientID", msgPatientID)
            intent.putExtra("appID_message", msgAppID)
            intent.putExtra("appDate_message", msgAppDate)
            intent.putExtra("appTime_message", msgAppTime)
            intent.putExtra("appService_message", msgService)
            intent.putExtra("appDocName_message", msgDocName)
            intent.putExtra("appRoom_message", msgRoom)
            intent.putExtra("appStatus_message", msgStatus)
            intent.putExtra("appCancelReason_message", msgCancelReason)
            //holder.itemView.context.startActivity(intent)
            val activity = holder.itemView.context as Activity
            activity.startActivity(intent)
            activity.overridePendingTransition(0,0)
        }
    }
    override fun getItemCount(): Int {
        return appList.size
    }

}