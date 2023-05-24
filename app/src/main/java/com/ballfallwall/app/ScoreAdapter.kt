package com.ballfallwall.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ballfallwall.app.databinding.ListItemBinding

class ScoreAdapter(val data: MutableList<String>): RecyclerView.Adapter<ScoreAdapter.Companion.ScoreHolder>() {

    companion object {
        class ScoreHolder(val binding: ListItemBinding): RecyclerView.ViewHolder(binding.root) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreHolder {
        return ScoreHolder(ListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ScoreHolder, position: Int) {
        holder.binding.textView6.text = "${position+1}. ${data[position]}"
    }

    override fun getItemCount(): Int {
        return data.size
    }
}