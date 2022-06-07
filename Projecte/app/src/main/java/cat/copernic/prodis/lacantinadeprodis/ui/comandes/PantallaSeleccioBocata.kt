package cat.copernic.prodis.lacantinadeprodis.ui.comandes

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.adapters.bocates_adapter
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaSeleccioBocataBinding
import cat.copernic.prodis.lacantinadeprodis.model.dataclass
import cat.copernic.prodis.lacantinadeprodis.model.dtclss_productes
import com.google.firebase.firestore.*

class PantallaSeleccioBocata : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productesArrayList: ArrayList<dtclss_productes>
    private lateinit var bocatesAdapter: bocates_adapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding: FragmentPantallaSeleccioBocataBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_seleccio_bocata, container, false
        )

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }
        //declaració de tots els elements del recyclerView (recyclerView, adapter i dataclass)
        recyclerView = binding.rcyclrVwBocata
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.setHasFixedSize(true)

        productesArrayList = arrayListOf()

        bocatesAdapter = bocates_adapter(productesArrayList)

        recyclerView.adapter = bocatesAdapter

        eventChangeListener()

        return binding.root
    }

    //funcio que agafa les dades de la BD i les introdueix al ArrayList per mostrar-les en el recyclerView
    private fun eventChangeListener() {
        db.collection("productes").addSnapshotListener(object : EventListener<QuerySnapshot>{
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null){
                    Log.e("Firestore Error",error.message.toString())
                    return
                }
                db.collection("productes").get().addOnSuccessListener { result ->
                    for (document in result){
                        if (document.id != "categories"){
                            if (document.get("tipus") == "Bocates" && document.get("visible") == true){
                                productesArrayList.add(document.toObject(dtclss_productes::class.java))
                            }
                        }
                    }

                    bocatesAdapter.notifyDataSetChanged()
                }
            }

        })
    }
}