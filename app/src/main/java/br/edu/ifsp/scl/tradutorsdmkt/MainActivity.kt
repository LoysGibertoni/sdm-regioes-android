package br.edu.ifsp.scl.tradutorsdmkt

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.codigosMensagen.RESPOSTA_IDIOMAS
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.codigosMensagen.RESPOSTA_REGIOES
import br.edu.ifsp.scl.tradutorsdmkt.model.Language
import br.edu.ifsp.scl.tradutorsdmkt.volley.Tradutor
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    object codigosMensagen {
        // Constante usada para envio de mensagens ao Handler
        val RESPOSTA_IDIOMAS = 0
        val RESPOSTA_REGIOES = 1
    }

    // Idiomas de origem e destino. Dependem da API do Oxford Dict.
    var idiomas: List<Language>? = null
    // Handler da thread de UI
    lateinit var tradutoHandler: TradutoHandler

    inner class TradutoHandler : Handler() {
        override fun handleMessage(msg: Message?) {
            if (msg?.what == RESPOSTA_IDIOMAS) {
                idiomas = msg.obj as List<Language>?
                idiomasSp.adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, idiomas)
                idiomasSp.setSelection(0)
                buscarRegioesBt.isEnabled = true
            } else if (msg?.what == RESPOSTA_REGIOES) {
                // Alterar o conteúdo do TextView
                regioesTv.text = msg.obj.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Instancia o handler da thread de UI usado pelo tradutor
        tradutoHandler = TradutoHandler()

        val tradutor = Tradutor(this)
        tradutor.getIdiomas()
        buscarRegioesBt.setOnClickListener {
            val selectedItem = idiomasSp.selectedItem
            if (selectedItem is Language) {
                tradutor.getRegioes(selectedItem)
            }
        }
    }
}