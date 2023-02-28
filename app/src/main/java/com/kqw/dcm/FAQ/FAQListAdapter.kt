package com.kqw.dcm.FAQ

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.kqw.dcm.Patient.Patient
import com.kqw.dcm.Patient.PatientListAdapter
import com.kqw.dcm.R
import kotlinx.android.synthetic.main.faq_list_item.view.*


class FAQListAdapter(private val faqList: ArrayList<FAQ_Data>): RecyclerView.Adapter<FAQListAdapter.FAQViewHolder>()  {
    companion object{
        val TAG:String = FAQListAdapter::class.java.simpleName
    }
    inner class FAQViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val etQues: EditText = itemView.findViewById(R.id.etQues)
        val etAns: EditText = itemView.findViewById(R.id.etAns)
        val ibQuesArrow: ImageButton = itemView.findViewById(R.id.ibQuesArrow)
//        val ibAnsArrow: ImageButton = itemView.findViewById(R.id.ibAnsArrow)
        val cvQues:CardView = itemView.findViewById(R.id.cvQues)
        val cvAns:CardView = itemView.findViewById(R.id.cvAns)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQListAdapter.FAQViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.faq_list_item, parent, false)
        return FAQViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: FAQListAdapter.FAQViewHolder, position: Int) {
        holder.etQues.setText(faqList[position].faqQues)
        holder.etAns.setText(faqList[position].faqAns)
        holder.cvAns.visibility = View.GONE

        holder.itemView.ibQuesArrow.setOnClickListener {
            Log.d(TAG, "clicked")
            if(holder.itemView.cvAns.visibility==View.GONE) {
                Log.d(TAG, "gone")
                holder.itemView.cvAns.visibility = View.VISIBLE
                holder.itemView.ibQuesArrow.setImageResource(R.drawable.arrow_up)
            }
            else if(holder.itemView.cvAns.visibility==View.VISIBLE) {
                Log.d(TAG, "visible")
                holder.itemView.cvAns.visibility = View.GONE
                holder.itemView.ibQuesArrow.setImageResource(R.drawable.arrow_down)
            }
        }

        holder.itemView.etQues.setOnClickListener {
            Log.d(TAG, "ques clicked")
            val msgFAQID : String = faqList[position].faqID
            val msgEdit : String = "Edit"
            val intent = Intent(holder.itemView.context, Create_FAQ::class.java)
            intent.putExtra("faqID_message", msgFAQID)
            intent.putExtra("edit_message", msgEdit)
            holder.itemView.context.startActivity(intent)
        }
        holder.itemView.setOnClickListener {
            Log.d(TAG, "ques cv clicked")
            val msgFAQID : String = faqList[position].faqID
            val msgEdit : String = "Edit"
            val intent = Intent(holder.itemView.context, Create_FAQ::class.java)
            intent.putExtra("faqID_message", msgFAQID)
            intent.putExtra("edit_message", msgEdit)
            holder.itemView.context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return faqList.size
    }
}