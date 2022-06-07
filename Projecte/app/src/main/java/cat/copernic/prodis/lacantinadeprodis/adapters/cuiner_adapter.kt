package cat.copernic.prodis.lacantinadeprodis.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.model.dtclss_cuiner
import cat.copernic.prodis.lacantinadeprodis.model.dtclss_cuiner_producte
import cat.copernic.prodis.lacantinadeprodis.model.dtclss_seleccio_resum_comanda
import cat.copernic.prodis.lacantinadeprodis.ui.caixa.PantallaSeleccioResumComandaDirections
import cat.copernic.prodis.lacantinadeprodis.ui.cuiner.PantallaCuinerDirections
import com.google.firebase.firestore.FirebaseFirestore


class cuiner_adapter(private val comandesList: ArrayList<dtclss_cuiner>) :
    RecyclerView.Adapter<cuiner_adapter.ViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.seleccio_resum_comandes_card, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comandaSeleccio: dtclss_cuiner = comandesList[position]

        holder.nomClient.text = comandaSeleccio.nom

        val comanda = "Comanda ${comandaSeleccio.comandaId}"
        holder.comandaId.text = comanda

        val comandaID = comandaSeleccio.comandaId.toString()
        val username = comandaSeleccio.nom.toString()
        val documentId = comandaSeleccio.documentId.toString()

        holder.nomClient.setOnClickListener { view ->
            view.findNavController().navigate(
                PantallaCuinerDirections.actionPantallaCuinerToPantallaCuinerComanda(
                    username,
                    comandaID,
                    documentId
                )
            )
        }

        holder.comandaId.setOnClickListener { view ->
            view.findNavController().navigate(
                PantallaCuinerDirections.actionPantallaCuinerToPantallaCuinerComanda(
                    username,
                    comandaID,
                    documentId
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return comandesList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomClient: TextView = itemView.findViewById(R.id.itemNom)
        val comandaId: TextView = itemView.findViewById(R.id.itemComanda)
    }
}