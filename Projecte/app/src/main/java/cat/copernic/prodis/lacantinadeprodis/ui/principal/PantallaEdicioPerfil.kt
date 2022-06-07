package cat.copernic.prodis.lacantinadeprodis.ui.comandes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaEdicioPerfilBinding

class PantallaEdicioPerfil : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding: FragmentPantallaEdicioPerfilBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_edicio_perfil, container, false
        )
        return binding.root
    }
}