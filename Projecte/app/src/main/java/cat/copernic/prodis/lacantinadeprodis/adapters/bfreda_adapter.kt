package cat.copernic.prodis.lacantinadeprodis.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.model.dtclss_productes
import cat.copernic.prodis.lacantinadeprodis.ui.comandes.PantallaSeleccioBegudaFredaDirections
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class bfreda_adapter(private val productesList: ArrayList<dtclss_productes>): RecyclerView.Adapter<bfreda_adapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): bfreda_adapter.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.card_layout, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val nomProducte: dtclss_productes = productesList[i]
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("productes/${nomProducte.nom}.png")
        viewHolder.itemNom.text = nomProducte.nom
        viewHolder.itemPreu.visibility = View.VISIBLE
        val preu = "${nomProducte.preu} €"
        viewHolder.itemPreu.text = preu
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(viewHolder.itemView)
                .load(uri)
                .centerCrop()
                .into(viewHolder.imageView)
        }

        viewHolder.itemNom.setOnClickListener { view ->
            view.findNavController().navigate(PantallaSeleccioBegudaFredaDirections.actionPantallaSeleccioBegudaFredaToPantallaSeleccioAtributsBeguda(nomProducte.idProducte.toString()))
        }

        viewHolder.imageView.setOnClickListener { view ->
            view.findNavController().navigate(PantallaSeleccioBegudaFredaDirections.actionPantallaSeleccioBegudaFredaToPantallaSeleccioAtributsBeguda(nomProducte.idProducte.toString()))
        }
    }

    override fun getItemCount(): Int {
        return  productesList.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val itemNom: TextView = itemView.findViewById(R.id.item_nomTipusProducte)
        val itemPreu: TextView = itemView.findViewById(R.id.item_preuProducte)
    }


}