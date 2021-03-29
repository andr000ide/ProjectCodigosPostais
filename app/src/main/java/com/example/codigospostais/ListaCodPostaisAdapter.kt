package com.example.codigospostais

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.codigospostais.helpers.DBHelper
import com.example.codigospostais.model.CodigoPostal


class ListaCodPostaisAdapter(private var dataSet: MutableList<CodigoPostal>, var context: Context) :
    RecyclerView.Adapter<ListaCodPostaisAdapter.ViewHolder>(), Filterable {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val numCodPostal: TextView = view.findViewById(R.id.numCodPostal)
        val desigCodPostal: TextView = view.findViewById(R.id.desiCodPostal)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.numCodPostal.text = dataSet[position].numCodPostal + "-" + dataSet[position].extCodPostal
        viewHolder.desigCodPostal.text = dataSet[position].desigPostal
    }

    override fun getItemCount() = dataSet.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getFilter(): Filter {
        return myFilter
    }

    // declaração da variavel que vai fazer o filtro. De modo a que seja sempre a mesma instancia do filtro, para quando vier um novo filtro o anterior seja descartado
    // para nao acontecer o caso do segundo filtro acabar antes do primeiro fazendo com que depois mostre os resultados do primeiro porque acabou depois
    var myFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val oReturn = FilterResults()
            var dbHelper = DBHelper(context)
            var listaCodigosPostais = dbHelper.getDataQuery(constraint.toString())
            oReturn.values = listaCodigosPostais
            return oReturn
        }

        override fun publishResults(
            constraint: CharSequence,
            results: FilterResults
        ) {
            results.values?.let {
                dataSet = it as MutableList<CodigoPostal>
                notifyDataSetChanged()
            }?: run {
                dataSet = mutableListOf<CodigoPostal>()
            }
        }
    }

}