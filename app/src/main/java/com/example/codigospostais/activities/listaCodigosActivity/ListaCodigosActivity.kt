package com.example.codigospostais.activities.listaCodigosActivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.codigospostais.ListaCodPostaisAdapter
import com.example.codigospostais.R
import com.example.codigospostais.activities.mainActivity.MainActivity
import com.example.codigospostais.helpers.DBHelper
import com.example.codigospostais.model.LoaderInfo

class ListaCodigosActivity : AppCompatActivity() {
    private lateinit var linearLayoutManager: LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_codigos)

        // desativa o loader
        MainActivity.showLoader.value = LoaderInfo(false,"")

        // popula e cria a recycler view
        populateRecyclerView()

    }

    private fun populateRecyclerView(){
        val dbHelper = DBHelper(applicationContext)
        val listaCodigosPostais = dbHelper.readData()
        val codPostaisAdapter =
                ListaCodPostaisAdapter(
                        listaCodigosPostais,
                        applicationContext
                )

        linearLayoutManager = LinearLayoutManager(this)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view_cod_postais)
        val searchView: SearchView = findViewById(R.id.searchView)
        // chama o filtro no adapter cada vez que existe uma alteração no texto da searchview ou quando se carrega no botão de "submit"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                codPostaisAdapter.filter.filter(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                codPostaisAdapter.filter.filter(query)
                return true
            }
        })
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = codPostaisAdapter
    }

}