package cat.copernic.prodis.lacantinadeprodis.ui.principal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaRecuperarContrasenya1Binding
import cat.copernic.prodis.lacantinadeprodis.utils.utils
import com.google.firebase.firestore.FirebaseFirestore

class PantallaRecuperarContrasenya1 : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var usertype: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPantallaRecuperarContrasenya1Binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_recuperar_contrasenya1, container, false
        )
        val args = PantallaRecuperarContrasenya1Args.fromBundle(requireArguments())
        usertype = args.usertype

        //Botó que iniciarà el següent fragment si la recuperació de dades es satisfactoria
        binding.btnPRecuperarContrasenya1Continuar.setOnClickListener {

            if (binding.dtTxtPRecuperarContrasenya1DNI.text.toString().isNotEmpty()) {
                db.collection("users")
                    .document(binding.dtTxtPRecuperarContrasenya1DNI.text.toString()).get()
                    .addOnSuccessListener { result ->
                        view?.findNavController()?.navigate(
                            PantallaRecuperarContrasenya1Directions.actionPantallaRecuperarContrasenya1ToPantallaRecuperarContrasenya2(
                                usertype,
                                result.get("dni").toString()
                            )
                        )
                    }
                        //En cas que l'usuari no es trobi a la base de dades, es mostrarà un missatge a l'usuari
                    .addOnFailureListener{
                        utils().showAlert(getString(R.string.error), getString(R.string.l_usuari_no_esta_registrat), this.context)
                    }

            } else {
                // En cas que el camp estigui buit, es mostrarà un missatge a l'usuari
                Toast.makeText(this.context, getString(R.string.el_camp_dni_esta_buit), Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

}
