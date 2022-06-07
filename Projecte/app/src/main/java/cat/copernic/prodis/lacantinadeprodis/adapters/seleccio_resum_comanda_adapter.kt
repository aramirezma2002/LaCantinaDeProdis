package cat.copernic.prodis.lacantinadeprodis.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.model.dtclss_seleccio_resum_comanda
import cat.copernic.prodis.lacantinadeprodis.ui.caixa.PantallaSeleccioResumComandaDirections
import cat.copernic.prodis.lacantinadeprodis.ui.comandes.PantallaResumComandes
import cat.copernic.prodis.lacantinadeprodis.ui.comandes.PantallaSeleccioTipusProdcuteDirections

class seleccio_resum_comanda_adapter(private val comandaListSeleccio: ArrayList<dtclss_seleccio_resum_comanda>): RecyclerView.Adapter<seleccio_resum_comanda_adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.seleccio_resum_comandes_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comandaSeleccio: dtclss_seleccio_resum_comanda = comandaListSeleccio[position]

        holder.nomClient.text = comandaSeleccio.nom

        val comanda = "Comanda ${comandaSeleccio.comandaId}"
        holder.comandaId.text = comanda

        val comandaID = comandaSeleccio.comandaId
        val username = comandaSeleccio.nom
        val documentId = comandaSeleccio.documentId
        val preutotal = convertNullToZero(comandaSeleccio.preuTotal)

        holder.nomClient.setOnClickListener { view ->
            view.findNavController().navigate(PantallaSeleccioResumComandaDirections.actionPantallaSeleccioResumComandaToPantallaResumComandes(
                comandaID,
                username,
                documentId,
                preutotal
            ))
        }

        holder.comandaId.setOnClickListener { view ->
            view.findNavController().navigate(PantallaSeleccioResumComandaDirections.actionPantallaSeleccioResumComandaToPantallaResumComandes(
                comandaID,
                username,
                documentId,
                preutotal
            ))
        }
    }

    override fun getItemCount(): Int {
        return comandaListSeleccio.size
    }

    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        val nomClient: TextView = itemView.findViewById(R.id.itemNom)
        val comandaId: TextView = itemView.findViewById(R.id.itemComanda)
    }

    private fun convertNullToZero(num :Double ?= null): Double{
        if (num == null){
            return 0.0
        }else{
            return num
        }
    }
}