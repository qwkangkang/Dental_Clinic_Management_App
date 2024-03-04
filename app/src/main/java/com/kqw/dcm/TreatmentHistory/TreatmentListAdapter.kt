package com.kqw.dcm.TreatmentHistory

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kqw.dcm.Appointment.AppointmentListAdapter
import com.kqw.dcm.Appointment.Appointment_Data
import com.kqw.dcm.Appointment.View_Appointment
import com.kqw.dcm.R

class TreatmentListAdapter(private val treatmentList: List<Treatment_Data>): RecyclerView.Adapter<TreatmentListAdapter.TreatmentViewHolder>() {
    companion object{
        val TAG:String = TreatmentListAdapter::class.java.simpleName
    }

    inner class TreatmentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvDoctorTreatment: TextView = itemView.findViewById(R.id.tvDoctorTreatment)
        val tvTreatmentName: TextView = itemView.findViewById(R.id.tvTreatmentName)
        val tvTreatmentDate: TextView = itemView.findViewById(R.id.tvTreatmentDate)
        val tvTreatmentTime: TextView = itemView.findViewById(R.id.tvTreatmentTime)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreatmentListAdapter.TreatmentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.treatment_list_item, parent, false)
        return TreatmentViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: TreatmentListAdapter.TreatmentViewHolder, position: Int) {
        holder.tvDoctorTreatment.text = treatmentList[position].doctorName
        holder.tvTreatmentName.text = treatmentList[position].treatmentName
        holder.tvTreatmentDate.text = treatmentList[position].treatmentDate
        holder.tvTreatmentTime.text = treatmentList[position].treatmentTime

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewNCreate_Treatment_History::class.java)
            intent.putExtra("viewNEdit_message", "viewNEdit")
            intent.putExtra("treatmentID_message", treatmentList[position].treatmentID)
            intent.putExtra("patientID_message", treatmentList[position].patientID)
            intent.putExtra("treatmentName_message", treatmentList[position].treatmentName)
            intent.putExtra("treatmentDate_message", treatmentList[position].treatmentDate)
            intent.putExtra("treatmentTime_message", treatmentList[position].treatmentTime)
            intent.putExtra("treatmentDocName_message", treatmentList[position].doctorName)
            intent.putExtra("treatmentRemark_message", treatmentList[position].treatment_remark)
            intent.putExtra("treatmentPres_message", treatmentList[position].treatmentPrescription)
            intent.putExtra("treatmentDetail_message", treatmentList[position].treatmentDetail)
//            intent.putExtra("appDate_message", msgAppDate)
//            intent.putExtra("appTime_message", msgAppTime)
//            intent.putExtra("appService_message", msgService)
//            intent.putExtra("appDocName_message", msgDocName)
            val activity = holder.itemView.context as Activity
            activity.startActivity(intent)
            activity.overridePendingTransition(0,0)
        }

    }
    override fun getItemCount(): Int {
        return treatmentList.size
    }
}