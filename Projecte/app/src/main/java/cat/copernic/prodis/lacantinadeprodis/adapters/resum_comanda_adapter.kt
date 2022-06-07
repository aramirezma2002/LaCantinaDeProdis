package cat.copernic.prodis.lacantinadeprodis.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.model.dtclss_resum_comanda

class resum_comanda_adapter(private val producte: ArrayList<dtclss_resum_comanda>) :
    RecyclerView.Adapter<resum_comanda_adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.resum_comanda_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producte: dtclss_resum_comanda = producte[position]

        holder.itemNom.text = producte.nomProducte
        val preu = "${producte.preuProducte.toString()}€"
        holder.itemPreu.text = preu
    }

    override fun getItemCount(): Int {
        return producte.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNom: TextView = itemView.findViewById(R.id.itemProducte)
        val itemPreu: TextView = itemView.findViewById(R.id.itemPreu)
    }
}