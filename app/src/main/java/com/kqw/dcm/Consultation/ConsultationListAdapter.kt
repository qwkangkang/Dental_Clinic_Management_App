package com.kqw.dcm.Consultation

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.kqw.dcm.FAQ.Create_FAQ
import com.kqw.dcm.FAQ.FAQListAdapter
import com.kqw.dcm.R
import kotlinx.android.synthetic.main.faq_list_item.view.*

class ConsultationListAdapter(private val conList: ArrayList<Consultation_Data>): RecyclerView.Adapter<ConsultationListAdapter.ConsultationViewHolder>() {
    companion object{
        val TAG:String = ConsultationListAdapter::class.java.simpleName
    }
    inner class ConsultationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvConQues: TextView = itemView.findViewById(R.id.tvConQues)
        val tvConDate: TextView = itemView.findViewById(R.id.tvConDate)
        val tvConTime: TextView = itemView.findViewById(R.id.tvConTime)
        val tvConStatus: TextView = itemView.findViewById(R.id.tvConStatus)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultationListAdapter.ConsultationViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.consultation_list_item, parent, false)
        return ConsultationViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: ConsultationListAdapter.ConsultationViewHolder, position: Int) {
        holder.tvConQues.text = conList[position].conQues.substring(0,12)+"..."
        holder.tvConDate.text = conList[position].conDate
        holder.tvConTime.text = conList[position].conTime
        holder.tvConStatus.text = conList[position].conStatus

        holder.itemView.setOnClickListener {
            //Log.d(FAQListAdapter.TAG, "ques clicked")
            val msgConID : String = conList[position].conID
            //val msgView : String = "View"
            val intent = Intent(holder.itemView.context, Consultation_Reply::class.java)
            intent.putExtra("conID_message", msgConID)
            //intent.putExtra("edit_message", msgEdit)
            holder.itemView.context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return conList.size
    }
}