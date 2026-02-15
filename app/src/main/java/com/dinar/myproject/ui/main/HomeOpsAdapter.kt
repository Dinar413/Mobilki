package com.dinar.myproject.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dinar.myproject.R

class HomeOpsAdapter : RecyclerView.Adapter<HomeOpsAdapter.VH>() {

    private var items: List<HomeOpUi> = emptyList()

    fun submit(list: List<HomeOpUi>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_home_op, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val vIndicator: View = itemView.findViewById(R.id.vIndicator)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        fun bind(item: HomeOpUi) {
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle
            tvAmount.text = item.amountText

            vIndicator.setBackgroundColor(
                itemView.context.getColor(
                    if (item.isIncome) R.color.brand_green else android.R.color.holo_red_light
                )
            )
        }
    }
}
