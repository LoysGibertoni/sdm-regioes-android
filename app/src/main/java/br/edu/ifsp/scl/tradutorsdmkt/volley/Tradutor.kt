package br.edu.ifsp.scl.tradutorsdmkt.volley

import br.edu.ifsp.scl.tradutorsdmkt.Constantes.APP_ID_FIELD
import br.edu.ifsp.scl.tradutorsdmkt.Constantes.APP_ID_VALUE
import br.edu.ifsp.scl.tradutorsdmkt.Constantes.APP_KEY_FIELD
import br.edu.ifsp.scl.tradutorsdmkt.Constantes.APP_KEY_VALUE
import br.edu.ifsp.scl.tradutorsdmkt.Constantes.END_POINT_IDIOMAS
import br.edu.ifsp.scl.tradutorsdmkt.Constantes.END_POINT_REGIOES
import br.edu.ifsp.scl.tradutorsdmkt.Constantes.URL_BASE
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.codigosMensagen.RESPOSTA_IDIOMAS
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.codigosMensagen.RESPOSTA_REGIOES
import br.edu.ifsp.scl.tradutorsdmkt.model.Language
import br.edu.ifsp.scl.tradutorsdmkt.model.LanguageResult
import br.edu.ifsp.scl.tradutorsdmkt.model.Resposta
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.design.snackbar
import org.json.JSONException
import org.json.JSONObject

class Tradutor(val mainActivity: MainActivity) {

    fun getIdiomas() {
        val url = "$URL_BASE$END_POINT_IDIOMAS"
        // Cria uma fila de requisições Volley para enviar a requisição
        val filaRequisicao: RequestQueue = Volley.newRequestQueue(mainActivity)
        // Monta a requisição que será colocada na fila. Esse objeto é uma instância de uma classe anônima
        var requisicao: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.GET, // Método HTTP de requisição
                url, // URL
                null, // Objeto de requisição - somente em POST
                RespostaIdiomasListener(), // Listener para tratar resposta
                ErroListener() // Listener para tratar erro
            ) {
                // Corpo do objeto
                // Sobreescrevendo a função para passar cabeçalho na requisição
                override fun getHeaders(): MutableMap<String, String> {
                    // Cabeçalho composto por Map com app_id, app_key e seus valores
                    var parametros: MutableMap<String, String> = mutableMapOf()
                    parametros.put(APP_ID_FIELD, APP_ID_VALUE)
                    parametros.put(APP_KEY_FIELD, APP_KEY_VALUE)
                    return parametros
                }
            }
        // Adiciona a requisição a fila
        filaRequisicao.add(requisicao)
    }

    fun getRegioes(idioma: Language) {
        val url = "$URL_BASE$END_POINT_REGIOES/${idioma.id}"
        // Cria uma fila de requisições Volley para enviar a requisição
        val filaRequisicao: RequestQueue = Volley.newRequestQueue(mainActivity)
        // Monta a requisição que será colocada na fila. Esse objeto é uma instância de uma classe anônima
        var requisicao: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.GET, // Método HTTP de requisição
                url, // URL
                null, // Objeto de requisição - somente em POST
                RespostaRegioesListener(), // Listener para tratar resposta
                ErroListener() // Listener para tratar erro
            ) {
                // Corpo do objeto
                // Sobreescrevendo a função para passar cabeçalho na requisição
                override fun getHeaders(): MutableMap<String, String> {
                    // Cabeçalho composto por Map com app_id, app_key e seus valores
                    var parametros: MutableMap<String, String> = mutableMapOf()
                    parametros.put(APP_ID_FIELD, APP_ID_VALUE)
                    parametros.put(APP_KEY_FIELD, APP_KEY_VALUE)
                    return parametros
                }
            }
        // Adiciona a requisição a fila
        filaRequisicao.add(requisicao)
    }

    /* Trata a resposta de uma requisição quando o acesso ao WS foi realizado. Usa um Desserializador
    O(N^2) */
    abstract inner class RespostaListener<T>(val codigo: Int) : Response.Listener<JSONObject> {
        final override fun onResponse(response: JSONObject?) {
            try {
                mainActivity.tradutoHandler.obtainMessage(codigo, converter(response)).sendToTarget()
            } catch (je: JSONException) {
                mainActivity.mainLl.snackbar("Erro na conversão JSON")
            }
        }

        abstract fun converter(json: JSONObject?): T?
    }

    /* Trata a resposta de uma requisição quando o acesso ao WS foi realizado. Usa um Desserializador
    O(N^2) */
    inner class RespostaIdiomasListener : RespostaListener<List<Language>>(RESPOSTA_IDIOMAS) {
        override fun converter(json: JSONObject?): List<Language>? {
            val typeToken = object : TypeToken<Resposta<List<LanguageResult>>>() {}.type
            val respostaIdiomas: Resposta<List<LanguageResult>> = Gson().fromJson(json.toString(), typeToken)
            return respostaIdiomas.results?.mapNotNull { it.sourceLanguage }?.distinctBy { it.id }
        }
    }

    /* Trata a resposta de uma requisição quando o acesso ao WS foi realizado. Usa um Desserializador
    O(N^2) */
    inner class RespostaRegioesListener : RespostaListener<String>(RESPOSTA_REGIOES) {
        override fun converter(json: JSONObject?): String? {
            val typeToken = object : TypeToken<Resposta<Map<String, List<String>>>>() {}.type
            val respostaRegioes: Resposta<Map<String, List<String>>> = Gson().fromJson(json.toString(), typeToken)

            val stringBuffer: StringBuffer = StringBuffer()
            respostaRegioes.results?.entries?.forEach { stringBuffer.append("${it.value[1]}, ") }
            return stringBuffer.toString().substringBeforeLast(',')
        }
    }

    // Trata erros na requisição ao WS
    inner class ErroListener : Response.ErrorListener {
        override fun onErrorResponse(error: VolleyError?) {
            mainActivity.mainLl.snackbar("Erro na requisição: ${error.toString()}")
        }
    }
}