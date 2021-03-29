package com.example.codigospostais.activities.mainActivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.codigospostais.R
import com.example.codigospostais.activities.listaCodigosActivity.ListaCodigosActivity
import com.example.codigospostais.api.FileDownloadClient
import com.example.codigospostais.helpers.DBHelper
import com.example.codigospostais.helpers.PreferenceHelper
import com.example.codigospostais.model.LoaderInfo
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*


class MainActivity : AppCompatActivity() {

    companion object {
        val showLoader = MutableLiveData<LoaderInfo>()
    }
    private val downloadedFile = "downloadFile"
    private val dataBDInfo = "databaseInfo"
    private val fileName = "codigos_postais.csv"
    private lateinit var preferenceHelper: PreferenceHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // chama a funcao que inicializa tudo
        Handler(Looper.getMainLooper()).postDelayed({ init()}, 100)

    }

    // função que inicializa as variáveis que vao ser usadas
    // inicializa um observer a um objeto ligado ao loader, para que possa ser ativado e mudar o texto do mesmo em qualquer lado
    // vai buscar ao sharedPreferences se já foi feito o download do ficheiro e se já foi feito a criação e população da base de dados
    // designa que parte do código deve correr, caso ainda não tenha feito o download do ficheiro chama a funçao que faz o download
    // caso tenha feito o download mas a base de dados não esteja preenchida chama a funçao que preenche a base de dados
    // caso já tenha feito as duas coisas chama a função que inicia uma nova atividade com a lista dos códigos
    private fun init(){
        // inicializa as variaveis de layouts que vão ser usadas
        val loaderLayoutActivity = findViewById<LinearLayout>(R.id.loaderContainerActivity)
        val loaderLayout = findViewById<ConstraintLayout>(R.id.loaderContainer)
        val loaderText = findViewById<TextView>(R.id.loader_text)

        preferenceHelper = PreferenceHelper(applicationContext)

        val fileDownloaded = preferenceHelper.getValueBoolean(downloadedFile, false)
        val baseDadosPreenchida = preferenceHelper.getValueBoolean(dataBDInfo, false)

        showLoader.value = LoaderInfo(true, "")
        showLoader.observe(this, Observer {
            loaderText.text = it.loaderText
            if (it.showLoader){
                loaderLayoutActivity.visibility = View.VISIBLE
                loaderLayout.visibility = View.VISIBLE
            }else{
                loaderLayoutActivity.visibility = View.GONE
                loaderLayout.visibility = View.GONE
            }
        })


        if(!fileDownloaded){
            showLoader.value = LoaderInfo(true, getString(R.string.download_ficheiro))
            downloadFile()
        }
        else if(fileDownloaded && !baseDadosPreenchida){
            showLoader.value = LoaderInfo(true, getString(R.string.criando_bd))
            Handler(Looper.getMainLooper()).postDelayed({ fileToDB()}, 100)
        }
        else{
            listaCodigosActivity()
        }
    }


    // efetua o download do ficheiro através do retrofit2
    // quando acaba o download chama a funcao que guarda o ficheiro
    // se guardar o ficheiro for bem sucedido chama a funcao de preencher a base de dados com o uso do ficheiro
    // mudando também o conteúdo do text do loader
    private fun downloadFile(){
        val retrofit = Retrofit.Builder().baseUrl("https://raw.githubusercontent.com/centraldedados/codigos_postais/").build()

        val service = retrofit.create(FileDownloadClient::class.java)
        val call = service.downloadFile()
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.d("MainActivity", "Failed")
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val caminhoDoFicheiro = application.filesDir.absolutePath+fileName
                    val fileSaveVerification = saveFile(response.body(),caminhoDoFicheiro)
                    if(fileSaveVerification){
                        showLoader.value = LoaderInfo(true, getString(R.string.criando_bd))
                        Handler(Looper.getMainLooper()).postDelayed({ fileToDB()}, 100)
                    }
                } else {
                    Log.d("MainActivity", "server contact failed")
                }
            }
        })
    }

    // função que inicializa a activia da lista de códigos postais
    private fun listaCodigosActivity(){
        val intent = Intent(this, ListaCodigosActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    // função que preenche a base de dados tendo em conta os dados do ficheiro
    // chama o DBHelper que cria a base de dados
    // e começa a ler do ficheiro a fazer as querys de insert
    // verifica que não esta na primeira linha do ficheiro que contem os nomes das colunas
    fun fileToDB(){
        val caminhoFicheiro = application.filesDir.absolutePath+fileName
        val file = File(caminhoFicheiro)
        val buffer = BufferedReader(FileReader(file))

        val dbHelper = DBHelper(applicationContext)
        val db = dbHelper.writableDatabase

        var line: String? = null
        val tableName = "codigosPostais"
        val columns = "num_cod_postal, ext_cod_postal, desig_postal"
        val stringQueryInsertFirstPart = "INSERT INTO $tableName ($columns) values("
        var first = true
        db.beginTransaction()
        while ({line = buffer.readLine(); line }() != null) {
            if ( !first) {
                val sb = StringBuilder(stringQueryInsertFirstPart)
                val arrayInfoCodPostais = line!!.split(",").toTypedArray()
                val size= arrayInfoCodPostais.size

                sb.append("'" + arrayInfoCodPostais[size-3] + "',")
                sb.append("'" + arrayInfoCodPostais[size-2] + "',")
                // previne o erro de uma das designações do codigo postal que tem um apostrofe no nome
                if (arrayInfoCodPostais[size-1].contains("'")){
                    arrayInfoCodPostais[size-1] = arrayInfoCodPostais[size-1].replace("'","''")
                }
                sb.append("'" + arrayInfoCodPostais[size-1] + "');")
                db.execSQL(sb.toString())
            }
            else {
                first = false
            }
        }
        db.setTransactionSuccessful()
        db.endTransaction()
        preferenceHelper.save(dataBDInfo, true)
        listaCodigosActivity()
    }

    // função que grava o ficheiro no caminho fornecido
    // ao guardar o ficheiro muda no sharedpreferences para termos a informação de que o ficheiro já esta guardado
    fun saveFile(body: ResponseBody?, caminhoDoFicheiro: String):Boolean{
        if (body==null)
            return false
        var input: InputStream? = null
        try {
            input = body.byteStream()
            val fos = FileOutputStream(caminhoDoFicheiro)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            return true
        }catch (e:Exception){
            Log.e("saveFile",e.toString())
        }
        finally {
            preferenceHelper.save(downloadedFile, true)
            input?.close()
        }
        return false
    }

}