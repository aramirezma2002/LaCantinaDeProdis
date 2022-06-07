package cat.copernic.prodis.lacantinadeprodis.ui.cuiner

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.adapters.cuiner_producte_adapter
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaCuinerComandaBinding
import cat.copernic.prodis.lacantinadeprodis.model.dtclss_cuiner_producte
import com.google.firebase.firestore.FirebaseFirestore

class PantallaCuinerComandes: Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var cuinerProducteAdapter: cuiner_producte_adapter
    private lateinit var producteList: ArrayList<dtclss_cuiner_producte>

    private lateinit var comandaId: String
    private lateinit var username: String
    private lateinit var documentId: String
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("SwitchIntDef")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding: FragmentPantallaCuinerComandaBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pantalla_cuiner_comanda,
            container,
            false
        )

        val args = PantallaCuinerComandesArgs.fromBundle(requireArguments())
        comandaId = args.username.toString()
        username =  args.documentId.toString()
        documentId = args.comandaId.toString()

        binding.txtUsername.text = username
        binding.txtComanda.text = comandaId

        recyclerView = binding.rycrVwCuinResumComanda
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                recyclerView.layoutManager = LinearLayoutManager(this.context)
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                recyclerView.layoutManager = LinearLayoutManager(
                    this.context,
                    RecyclerView.HORIZONTAL,
                    false
                )
            }
            Configuration.ORIENTATION_UNDEFINED -> {
                recyclerView.layoutManager = LinearLayoutManager(
                    this.context,
                    RecyclerView.HORIZONTAL,
                    false
                )
            }
        }

        recyclerView.setHasFixedSize(true)
        producteList = arrayListOf()
        cuinerProducteAdapter = cuiner_producte_adapter(producteList)
        eventChangeListener()

        binding.btnCuinat.setOnClickListener {
            db.collection("comandes").document(documentId).get().addOnSuccessListener { dc ->
                if (dc.get("comandaPagada").toString() == "true"){
                    db.collection("comandes").document(documentId).update(
                        hashMapOf(
                            "preparat" to true,
                            "visible" to false
                        ) as Map<String, Any>
                    )
                }else{
                    db.collection("comandes").document(documentId).update(
                        hashMapOf(
                            "preparat" to true
                        ) as Map<String, Any>
                    )
                }
            }
            view?.findNavController()?.navigate(PantallaCuinerComandesDirections.actionPantallaCuinerComandaToPantallaCuiner())

        }

        binding.btnTornaEnrerre.setOnClickListener {
            view?.findNavController()?.navigate(PantallaCuinerComandesDirections.actionPantallaCuinerComandaToPantallaCuiner())
        }



        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun eventChangeListener(){
        producteList.clear()
        db.collection("comandes").document(documentId).collection("productes").get().addOnSuccessListener { result ->
            for (document in result) {

                    db.collection("productes").get().addOnSuccessListener { rs ->
                        for (dc in rs) {
                            if (document.get("idProducte").toString() == dc.get("idProducte")
                                    .toString()
                            ) {
                                if (dc.get("tipus").toString() == "Bocates") {
                                    val idProducte = dc.get("nom").toString()
                                    val emportar = document.get("emportar") as Boolean
                                    val tomaquet = document.get("tomaquet") as Boolean
                                    producteList.add(
                                        dtclss_cuiner_producte(
                                            idProducte,
                                            emportar,
                                            null,
                                            tomaquet,
                                            "Bocates"
                                        )
                                    )
                                } else {
                                    val idProducte = dc.get("nom").toString()
                                    val emportar = document.get("emportar") as Boolean
                                    val scure = document.get("scure").toString()
                                    producteList.add(
                                        dtclss_cuiner_producte(
                                            idProducte,
                                            emportar,
                                            scure,
                                            null,
                                            "Beguda"
                                        )
                                    )
                                }
                            }

                        }
                        recyclerView.adapter = cuinerProducteAdapter
                    }


            }
        }
    }
}