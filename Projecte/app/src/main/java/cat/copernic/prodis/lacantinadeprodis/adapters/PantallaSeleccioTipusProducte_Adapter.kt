package cat.copernic.prodis.lacantinadeprodis.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.adapters.PantallaSeleccioTipusProducte_Adapter.*
import cat.copernic.prodis.lacantinadeprodis.model.dataclass
import cat.copernic.prodis.lacantinadeprodis.ui.comandes.PantallaSeleccioTipusProdcuteDirections
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class PantallaSeleccioTipusProducte_Adapter(private val producteList: List<dataclass>) :
    RecyclerView.Adapter<ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.card_layout, viewGroup, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val tProducte: dataclass = producteList[i]
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("productes/${tProducte.photo}")
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(viewHolder.itemView)
                .load(uri)
                .centerCrop()
                .error(R.drawable.photo_library_foreground)
                .into(viewHolder.imageView)
        }
        viewHolder.itemNom.text = tProducte.producte

        viewHolder.imageView.setOnClickListener { view ->
            when (i) {
                0 -> {
                    view.findNavController().navigate(PantallaSeleccioTipusProdcuteDirections.actionPantallaSeleccioTipusProducteToPantallaSeleccioBocata())
                }
                1 -> {
                    view.findNavController().navigate(PantallaSeleccioTipusProdcuteDirections.actionPantallaSeleccioTipusProducteToPantallaSeleccioBegudaFreda())
                }
                2 -> {
                    view.findNavController().navigate(PantallaSeleccioTipusProdcuteDirections.actionPantallaSeleccioTipusProducteToPantallaSeleccioBegudaCalenta())
                }
            }
        }

        viewHolder.itemNom.setOnClickListener{ view ->
            when (i) {
                0 -> {
                    view.findNavController().navigate(PantallaSeleccioTipusProdcuteDirections.actionPantallaSeleccioTipusProducteToPantallaSeleccioBocata())
                }
                1 -> {
                    view.findNavController().navigate(PantallaSeleccioTipusProdcuteDirections.actionPantallaSeleccioTipusProducteToPantallaSeleccioBegudaCalenta())
                }
                2 -> {
                    view.findNavController().navigate(PantallaSeleccioTipusProdcuteDirections.actionPantallaSeleccioTipusProducteToPantallaSeleccioBegudaFreda())
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return producteList.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.item_image)
        val itemNom: TextView = itemView.findViewById(R.id.item_nomTipusProducte)
    }

}