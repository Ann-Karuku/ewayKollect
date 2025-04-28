package com.example.ewaykollect

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubmissionAdapter(
    private var submissions: List<SubmissionItem>
) : RecyclerView.Adapter<SubmissionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.textSubmissionTitle)
        val descriptionTextView: TextView = view.findViewById(R.id.textSubmissionDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_submission, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val submission = submissions[position]
        holder.titleTextView.text = submission.title
        holder.descriptionTextView.text = submission.description
    }

    override fun getItemCount() = submissions.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<SubmissionItem>) {
        submissions = newList
        notifyDataSetChanged()
    }
}