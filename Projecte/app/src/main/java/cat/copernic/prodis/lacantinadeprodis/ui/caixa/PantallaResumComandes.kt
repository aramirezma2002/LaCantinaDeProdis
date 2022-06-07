package cat.copernic.prodis.lacantinadeprodis.ui.comandes

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.adapters.resum_comanda_adapter
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaResumComandesBinding
import cat.copernic.prodis.lacantinadeprodis.model.dtclss_cuiner_producte
import cat.copernic.prodis.lacantinadeprodis.model.dtclss_resum_comanda
import cat.copernic.prodis.lacantinadeprodis.ui.administrador.PantallaAdministradorNouUsuariArgs
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.properties.Delegates

class PantallaResumComandes : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var producteList: ArrayList<dtclss_resum_comanda>
    private lateinit var resumComandaAdpt: resum_comanda_adapter

    private lateinit var comandaId: String
    private lateinit var username: String
    private lateinit var documentId: String
    private var preuTotal by Delegates.notNull<Double>()

    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPantallaResumComandesBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_resum_comandes, container, false
        )

        val args = PantallaResumComandesArgs.fromBundle(requireArguments())
        comandaId = args.comandaId.toString()
        username = args.username.toString()
        documentId = args.documentId.toString()
        preuTotal = args.preuTotal

        binding.txtUsername.text = username
        binding.txtComanda.text = comandaId
        binding.txtVwPreuTotal.text = preuTotal.toString() + "€"

        recyclerView = binding.rycrVwResumComanda

        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.setHasFixedSize(true)
        producteList = arrayListOf()
        resumComandaAdpt = resum_comanda_adapter(producteList)
        eventChangeListener()

        binding.btnPagat?.setOnClickListener {
            db.collection("comandes").document(documentId).get().addOnSuccessListener { dc ->
                if (dc.get("preparat").toString() == "true"){
                    db.collection("comandes").document(documentId).update(
                        hashMapOf(
                            "comandaPagada" to true,
                            "visible" to false
                        ) as Map<String, Any>
                    )
                }else{
                    db.collection("comandes").document(documentId).update(
                        hashMapOf(
                            "comandaPagada" to true
                        ) as Map<String, Any>
                    )
                }
            }
            view?.findNavController()?.navigate(PantallaResumComandesDirections.actionPantallaResumComandesToPantallaSeleccioResumComanda())
        }

        binding.btnTornaEnrerre?.setOnClickListener {
            view?.findNavController()?.navigate(PantallaResumComandesDirections.actionPantallaResumComandesToPantallaSeleccioResumComanda())
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun eventChangeListener(){
        producteList.clear()
        db.collection("comandes").document(documentId).collection("productes").get().addOnSuccessListener { result ->
            for (document in result){
                db.collection("productes").get().addOnSuccessListener { rs ->
                    for (dc in rs){
                        if (document.get("idProducte").toString() == dc.get("idProducte").toString()){
                            val nom = dc.get("nom").toString()
                            val preu = dc.get("preu") as Double
                            producteList.add(dtclss_resum_comanda(nom, preu))
                        }
                    }
                    recyclerView.adapter = resumComandaAdpt
                }

            }
        }
    }
}