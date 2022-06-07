package cat.copernic.prodis.lacantinadeprodis.ui.comandes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaSeleccioAtributsBocataBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PantallaSeleccioAtributsBocata : Fragment() {

    //Definim les variables globals
    var senseTomaquet: Boolean = false

    var perEemportar: Boolean = false

    private val db = FirebaseFirestore.getInstance()

    lateinit var idProducte: String

    //Iniciem l'onCreateWiew
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPantallaSeleccioAtributsBocataBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_seleccio_atributs_bocata, container, false
        )

        //Declarem i incialitzem una variable per agafar els arguments pasats per parametre de la pantalla anterior
        val args = PantallaSeleccioAtributsBegudaArgs.fromBundle(requireArguments())

        //Fem que la variable idProducte siguin els args "idProductes" pasats per parametres
        idProducte = args.idProductes

        //Escoltem al botó per tornar enrrere
        binding.btnTornaEnrerre.setOnClickListener {
            //Fem que torni a la pantalla anterior
            findNavController().popBackStack()
        }

        //Escoltem el botó de confirmar
        binding.btnConfirmar.setOnClickListener { view: View ->

            //Anem a la pantalla de seleccio tipus de producte
            view.findNavController()
                .navigate(R.id.action_pantalla_seleccio_atributs_bocata_to_pantalla_seleccio_tipus_producte)

            //El valor de la variable senseTomaquet sera true o false a l'inversa depenent si el checkbox
            // de sense tomaquet está marcat o no
            senseTomaquet = !binding.checkBoxSenseTomaquet.isChecked

            //El valor de la variable perEmportar será truo o false depenent si el checkbox de per emportar está
            //marcat o no
            perEemportar = binding.checkBoxPerEmportar.isChecked

            //Declarem i inicialitzem una variable per saber el usuari actual
            val currentUser =
                FirebaseAuth.getInstance().currentUser

            //Declarem una variable per agafar el dni
            var dni: String

            //Entrem a la colecció de "users" i agafem les dades
            db.collection("users").get().addOnSuccessListener { result ->
                //Mirem document a document
                for (document in result) {
                    //Si el valor del camp "email" document que estem mirant es el mateix al email del usuari actual..
                    if (currentUser?.email.toString() == document.get("email").toString()) {
                        //El valor del dni será el id del document
                        dni = document.id
                        //Entrem en la colecció comandes i agafem les dades
                        db.collection("comandes").get().addOnSuccessListener { result ->
                            //Mirem document a document
                            for (document in result) {
                                //Fem que si el valor "user" del document que estem mirant es igual al valor
                                //del dni que hem agafat abans, actualitzarem les dades de comanda
                                //amb els atributs que hem agafat
                                if (dni.equals(document.get("user"))) {
                                    db.collection("comandes").document(document.id)
                                        .collection("productes").document().set(
                                            hashMapOf(
                                                "idProducte" to idProducte,
                                                "emportar" to perEemportar,
                                                "tomaquet" to senseTomaquet
                                            ) as Map<String, Any>
                                        )
                                }
                            }
                        }
                    }
                }
            }

        }
        return binding.root
    }
}
