package com.example.calculator4android

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.calculator4android.databinding.HistoryRowBinding

class HistoryAdapter(private var HistoryElements: List<HistoryElement>,private val onItemClick: (HistoryElement) -> Unit):
    RecyclerView.Adapter<HistoryAdapter.MyViewHolder>() {

    inner class MyViewHolder(binding:HistoryRowBinding) : ViewHolder(binding.root){
        val equation = binding.equation
        val result = binding.result
        val date = binding.date

        fun bind(item: HistoryElement){
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val historyRowBinding = HistoryRowBinding.inflate(inflater,parent,false)
        return MyViewHolder(historyRowBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.equation.text = HistoryElements[position].equation
        holder.result.text = HistoryElements[position].result
        holder.date.text = HistoryElements[position].date
        holder.bind(HistoryElements[position])
    }

    override fun getItemCount(): Int {
        return HistoryElements.size
    }
    fun updateHistory(newList: List<HistoryElement>){
        HistoryElements = newList as MutableList<HistoryElement>
        this.notifyDataSetChanged()
    }

}