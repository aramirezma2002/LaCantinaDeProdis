package cat.copernic.prodis.lacantinadeprodis.ui.caixa

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.lacantinadeprodis.MainActivity
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.adapters.seleccio_resum_comanda_adapter
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaResumComandesBinding
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaSeleccioResumComandaBinding
import cat.copernic.prodis.lacantinadeprodis.model.dtclss_seleccio_resum_comanda
import cat.copernic.prodis.lacantinadeprodis.ui.activities.CaixerActivity
import com.google.firebase.firestore.FirebaseFirestore

class PantallaSeleccioResumComanda : Fragment() {

    private lateinit var recycerView: RecyclerView
    private lateinit var comandaList: ArrayList<dtclss_seleccio_resum_comanda>
    private lateinit var seleccioResumComandaAdpt: seleccio_resum_comanda_adapter
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("SwitchIntDef", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPantallaSeleccioResumComandaBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_seleccio_resum_comanda, container, false
        )

        recycerView = binding.rcyclrVwSeleccioComandes!!

        val gridLayout = GridLayoutManager(this.context, 2)
        comandaList = arrayListOf()
        seleccioResumComandaAdpt = seleccio_resum_comanda_adapter(comandaList)
        seleccioResumComandaAdpt.notifyDataSetChanged()
        eventChangeListener()

        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                recycerView.layoutManager = gridLayout
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                recycerView.layoutManager =
                    GridLayoutManager(this.context, 3, RecyclerView.VERTICAL, false)
            }
            Configuration.ORIENTATION_UNDEFINED -> {
                recycerView.layoutManager =
                    GridLayoutManager(this.context, 2, RecyclerView.HORIZONTAL, false)
            }
        }


        recycerView.setHasFixedSize(true)



        recycerView.adapter = seleccioResumComandaAdpt

        binding.imageButton.setOnClickListener {
            val intent = Intent(this.context, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }


        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun eventChangeListener() {
        db.collection("comandes").get().addOnSuccessListener { result ->
            comandaList.clear()
            for (document in result) {
                if (document.get("visible").toString() == "true" && document.get("comandaPagada")
                        .toString() == "false"
                ) {
                    val nom = document.get("user").toString()
                    val comandaId = document.get("comandaId").toString()
                    val documentId = document.id
                    val preuTotal: Double = document.get("preuTotal").toString().toDouble()
                    comandaList.add(dtclss_seleccio_resum_comanda(nom, comandaId, documentId, preuTotal))
                }
            }
            seleccioResumComandaAdpt.notifyDataSetChanged()
        }
    }
}