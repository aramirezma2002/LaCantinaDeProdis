package cat.copernic.prodis.lacantinadeprodis.ui.comandes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaSeleccioAtributsBegudaBinding
import cat.copernic.prodis.lacantinadeprodis.utils.utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class PantallaSeleccioAtributsBeguda : Fragment() {

    lateinit var binding: FragmentPantallaSeleccioAtributsBegudaBinding
    private val db = FirebaseFirestore.getInstance()


    //Definició de les variables per determinar els atributs
    var atributs = "a"
    var perEmportar: Boolean = false

    lateinit var idProducte: String

    //Iniciem l'onCreateView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_seleccio_atributs_beguda, container, false
        )

        //Declarem i inicialitzem la variable args per agafar les dades pasades per parametres
        var args = PantallaSeleccioAtributsBegudaArgs.fromBundle(requireArguments())

        //Declarem i inicialtizem una variable per fer referencia al usuari actual
        val currentUser =
            FirebaseAuth.getInstance().currentUser

        //Declarem i incialitzem una variable per guardar el dni del usuari
        var dni: String

        //Declarem una corrutina per guardar els atributs de la comanda
        var tasca1: Job? = setAtributs()

        //Fem que la variable idProducte sigui els arguments que hem agafat abans
        idProducte = args.idProductes

        //Inicialitzem la corrutina que hem creat abans
        tasca1.let {}

        //Escoltem el botó de confirmar
        binding.btnConfirmar.setOnClickListener { view: View ->
            //Cridem a la funció per setejar els atributs
            setAtributs()

            //Anem a la colecció de users i agafem les dades
            db.collection("users").get().addOnSuccessListener { result ->
                //Mirem document a document
                for (document in result) {
                    //Fem que si l'email del usuari actual es igual al email de del document,
                    //el dni será l'id del document actual que estem mirant
                    if (currentUser?.email.toString() == document.get("email").toString()) {
                        dni = document.id
                        //Anem a la colecció comandes i agafem les dades
                        db.collection("comandes").get().addOnSuccessListener { result ->
                            //Mirem document a document
                            for (document in result) {
                                //Fem que si el valor "user" del document que estem mirant es igual al valor
                                // del dni que hem agafat abans, actualitzarem les dades de comanda
                                //amb els atributs que hem agafat amb la funció setAtributs
                                if (dni.equals(document.get("user"))) {
                                    db.collection("comandes").document(document.id)
                                        .collection("productes").document().set(
                                            hashMapOf(
                                                "idProducte" to idProducte,
                                                "emportar" to perEmportar,
                                                "scure" to atributs
                                            ) as Map<String, Any>
                                        )
                                }
                            }
                        }
                    }
                }
            }
            //Anem a la pantalla de selecció tipus producte
            view.findNavController()
                .navigate(
                    PantallaSeleccioAtributsBegudaDirections.actionPantallaSeleccioAtributsBegudaToPantallaSeleccioTipusProducte()
                )
        }

        //Escoltem al botó de tornar enrrere
        binding.btnTornaEnrerre.setOnClickListener {
            //Fem que torni a la pantalla anterior
            findNavController().popBackStack()
        }

        return binding.root
    }

    //Funció per setejar els atributs de la comanda
    fun setAtributs() = GlobalScope.launch(Dispatchers.Main) {
        withContext(Dispatchers.IO) {
            //Escoltem el radio group per saber quin atribut han siguit seleccionats
            binding.rarioGroupAtributs
                .setOnCheckedChangeListener { _, checkedId ->
                    when (checkedId) {
                        //Si el radio sense sucre ha siguit seleccionat, el valor d'atributs será "SS"
                        R.id.radioSenseSucre -> {
                            atributs = "SS"
                        }
                        //Si el radio de sucre blanc ha sigut seleccionat, el valor d'atributs será "SB"
                        R.id.radioSucreBlanc -> {
                            atributs = "SB"
                        }
                        //Si el radio de sucre moré ha sigut seleccionat, el valor d'atributs será "SM"
                        R.id.radioSucreMore -> {
                            atributs = "SM"
                        }
                        //Si el radio de sacarina ha siguit seleccionat, el valor d'atributs será "SC"
                        R.id.radioSacarina -> {
                            atributs = "SC"
                        }
                    }
                }
            //El valor de per emportar cambiará si el checkbox si está marcat o no
            perEmportar = binding.checkPerEmportar.isChecked
        }
    }
}
