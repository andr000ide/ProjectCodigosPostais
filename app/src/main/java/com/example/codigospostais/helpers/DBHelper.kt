package com.example.codigospostais.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.codigospostais.model.CodigoPostal

val Database_Name = "CodigosPostaisDB"

// databaseHelper que cria a base de dados para os codigos postais
class DBHelper(context: Context) : SQLiteOpenHelper (context, Database_Name, null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        //val createTable = "CREATE TABLE codigosPostais ( cod_distrito varchar(5), cod_concelho varchar(5) , cod_localidade varchar(5), nome_localidade varchar(255) , cod_arteria varchar(255), tipo_arteria varchar(255), prep1 varchar(255), titulo_arteria varchar(255), prep2 varchar(255), nome_arteria varchar(255), local_arteria varchar(255), troco varchar(255), porta varchar(255), cliente varchar(255), num_cod_postal varchar(255), ext_cod_postal varchar(255), desig_postal varchar(255));"
        // criar a tabela apenas com as três entradas necessarias para o projeto
        val createTable = "CREATE TABLE codigosPostais ( num_cod_postal varchar(4), ext_cod_postal varchar(3), desig_postal varchar(255));"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    // função que faz query à base de dados para ir buscar o numéro do codigo postal, a extensão e a designação
    // e coloca tudo numa lista de codigos postais
    fun readData() : MutableList<CodigoPostal>{
        val list : MutableList<CodigoPostal> = ArrayList()

        val db = this.readableDatabase
        val query = "Select num_cod_postal, ext_cod_postal, desig_postal from codigosPostais"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()){
            do{
                val codigoPostal = CodigoPostal()
                codigoPostal.numCodPostal = result.getString(0)
                codigoPostal.extCodPostal = result.getString(1)
                codigoPostal.desigPostal = result.getString(2)
                list.add(codigoPostal)
            } while( result.moveToNext())
        }
        return list
    }

    // função que faz query à base de dados com filtro
    fun getDataQuery(text: String) : MutableList<CodigoPostal>{
        val list : MutableList<CodigoPostal> = ArrayList()

        val db = this.readableDatabase

        //var query = "Select num_cod_postal, ext_cod_postal, desig_postal from codigosPostais where num_cod_postal  LIKE '%$text%' "

        var queryWhere = ""
        // retira os espaços a mais da pesquisa
        var texto = text.trim()
        // divide a pesquisa pelos espaços
        var arrayAux = texto.split(" ")

        // codigo do filtro efetuado
        // percorre cada palavra ou numero da pesquisa ( tendo sido separado pelos espaços )
        for (aux in arrayAux){
            var adicionar = ""
            // caso contenha o tracinho separamos em duas partes, antes e depois do tracinho
            // caso antes do tracinho tenha 4 numeros pesquisamos esses 4 numeros no numero do codigo postal
            // caso depois do tracinho tenha entre 1 e 3 numeros pesquisamos esses numeros na extensão do código postal
            if(aux.contains("-")){
                val splitTracinho = aux.split("-")
                if("^[0-9]{4}$".toRegex().matches(splitTracinho[0])){
                    val numCod = splitTracinho[0]
                    adicionar += " num_cod_postal LIKE '$numCod' AND"
                }
                if("^[0-9]{1,3}$".toRegex().matches(splitTracinho[1])){
                    val extCod = splitTracinho[1]
                    adicionar += " ext_cod_postal LIKE '%$extCod%' AND"
                }
            }
            // caso sejam 4 numero exatos pesquisa no numero do codigo postal
            else if("^[0-9]{4}$".toRegex().matches(aux)){
                adicionar = " num_cod_postal LIKE '$aux' AND"
            }
            // caso sejam 3 numeros exatos pesquisa no numero do codigo postal
            else if ("^[0-9]{3}$".toRegex().matches(aux)){
                adicionar = " ext_cod_postal LIKE '$aux' AND"
            }
            // caso sejam entre 1 e 2 numero pesquisa tanto no numero como na extensao do codigo postal
            else if ("^[0-9]{1,2}$".toRegex().matches(aux)){
                adicionar = "( ext_cod_postal LIKE '%$aux%' or num_cod_postal LIKE '%$aux%' ) AND"
            }
            // caso nao sejam nenhum dos caso anteriores pesquisa na designacao do codigo postal
            else {
                adicionar = " desig_postal LIKE '%$aux%' AND"
            }
            queryWhere += adicionar
        }
        // retira o AND do final da query
        queryWhere = queryWhere.substring(0,queryWhere.length-3)

        var query = "Select num_cod_postal, ext_cod_postal, desig_postal from codigosPostais where $queryWhere"


        val result = db.rawQuery(query, null)
        if (result.moveToFirst()){
            do{
                var codigoPostal = CodigoPostal()
                codigoPostal.numCodPostal = result.getString(0)
                codigoPostal.extCodPostal = result.getString(1)
                codigoPostal.desigPostal = result.getString(2)
                list.add(codigoPostal)
            } while( result.moveToNext())
        }
        return list
    }


}
