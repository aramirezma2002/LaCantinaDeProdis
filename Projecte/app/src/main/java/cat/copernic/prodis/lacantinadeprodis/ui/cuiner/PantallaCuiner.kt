package cat.copernic.prodis.lacantinadeprodis.ui.cuiner

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.adapters.cuiner_adapter
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaCuinerBinding
import cat.copernic.prodis.lacantinadeprodis.model.dtclss_cuiner
import com.google.firebase.firestore.*

class PantallaCuiner : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cuinerAdapter: cuiner_adapter
    private lateinit var comandesList: ArrayList<dtclss_cuiner>
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("SwitchIntDef", "UseRequireInsteadOfGet", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPantallaCuinerBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_cuiner, container, false
        )
        //declaració de tots els elements del recyclerView (recyclerView, adapter i dataclass)

        recyclerView = binding.rcyclrVwCuiner

        //En funció de la orientació del dispositiu canviem la orientació del recycerView
        val gridLayout = GridLayoutManager(this.context, 2)
        comandesList = arrayListOf()
        cuinerAdapter = cuiner_adapter(comandesList)
        cuinerAdapter.notifyDataSetChanged()
        eventChangeListener()
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                recyclerView.layoutManager = gridLayout
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                recyclerView.layoutManager =
                    GridLayoutManager(this.context, 3, RecyclerView.VERTICAL, false)
            }
            Configuration.ORIENTATION_UNDEFINED -> {
                recyclerView.layoutManager =
                    GridLayoutManager(this.context, 2, RecyclerView.HORIZONTAL, false)
            }
        }

        recyclerView.setHasFixedSize(true)








        return binding.root
    }

    //funcio que agafa les dades de la BD i les introdueix al ArrayList per mostrar-les en el recyclerView
    @SuppressLint("NotifyDataSetChanged")
    private fun eventChangeListener() {
        //comandesList.clear()
        db.collection("comandes").get().addOnSuccessListener { result ->
            comandesList.clear()
            for (document in result) {
                if(document.get("visible").toString() == "true" && document.get("preparat")
                        .toString() == "false"){
                    val nom = document.get("user").toString()
                    val comandaId = document.get("comandaId").toString()
                    val documentId = document.id
                    comandesList.add(dtclss_cuiner(nom, comandaId, documentId))

                }
            }
            recyclerView.adapter = cuinerAdapter
            cuinerAdapter.notifyDataSetChanged()
        }


    }
}