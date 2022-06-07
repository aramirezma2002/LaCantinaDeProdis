package cat.copernic.prodis.lacantinadeprodis.ui.administrador

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaAdministradorPrincipalBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList


class PantallaAdministradorPrincipal : Fragment() {

    //Declarem les variables globals

    private val db = Firebase.firestore
    private var arrUser = ArrayList<String>()
    private var arrUserId = ArrayList<String>()
    private var arrUsertype = ArrayList<String>()
    private var arrayTipusProducte = ArrayList<String>()
    private var arrayProductes = ArrayList<String>()

    //Iniciem l'onCreateView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentPantallaAdministradorPrincipalBinding>(
            inflater,
            R.layout.fragment_pantalla_administrador_principal, container, false
        )
        //Natejem tots els arrays per poder ficar noves dades
        arrUsertype.clear()
        arrUser.clear()
        arrUserId.clear()
        arrayProductes.clear()
        arrayTipusProducte.clear()

        //Entrem a la colecció de user al document de usertypes i agafem les dades del document
        db.collection("users").document("usertypes").get().addOnSuccessListener { document ->
            //Declarem i inicialitzem una variable per a cada tipus de usuari i guardem les dades de cada tipus
            val admin = document.get("admin").toString()
            val caixer = document.get("caixer").toString()
            val cambrer = document.get("cambrer").toString()
            val client = document.get("client").toString()
            val clientR = document.get("clientR").toString()
            val cuiner = document.get("cuiner").toString()

            //Afegim al array d'usuaris els valor anteriorment agafats
            arrUsertype.add(admin)
            arrUsertype.add(caixer)
            arrUsertype.add(cambrer)
            arrUsertype.add(client)
            arrUsertype.add(clientR)
            arrUsertype.add(cuiner)
        }
        //Entrem en la colecció de productes al document de categories i agafem les dades del document
        db.collection("productes").document("categories").get().addOnSuccessListener { document ->
            //Declarem i inicialitzem una variable per a cada tipus de categoria i guardem les dades
            val bCalenta = document.get("bCalenta").toString()
            val bFreda = document.get("bFreda").toString()
            val bocata = document.get("bocata").toString()
            //Fem que si l'array de tipus de productes es buit, s'afegiran les dades que acabem d'agafar a l'array
            //de productes
            if (arrayTipusProducte.isEmpty()) {
                arrayTipusProducte.add(bCalenta)
                arrayTipusProducte.add(bFreda)
                arrayTipusProducte.add(bocata)
            }
        }
        //Entrem en la colecció de productes i agafem les dades
        db.collection("productes").get().addOnSuccessListener { result ->
            //Si l'array de productes es buit mirarem cada document i agafarem el seu nom si el
            //nom del document no es "categories". Si tot aixó es compleix es guardará el nom
            //del document dins de l'array de productes.
            if (arrayProductes.isEmpty()) {
                for (document in result) {
                    if (document.id != "categories") {
                        arrayProductes.add(document.get("nom").toString())
                    }
                }
            }
        }

        //Fem que al clickar el botó per anar a la pantalla administrador nou usuari, ens porti
        //a a questa pantalla i li pasem per parametres l'array de tipus d'usuaris.
        binding.btnPAdministradorPrincipalNouUsuari.setOnClickListener {
            view?.findNavController()
                ?.navigate(
                    PantallaAdministradorPrincipalDirections.actionPantallaAdministradorPrincipalToPantallaAdministradorNouUsuari(
                        arrUsertype
                    )
                )
        }

        //Fem que al fer clic el botó per anar a la pantalla eliminar/modificar usuari...
        binding.btnPAdministradorPrincipalElimiarModificarUsuari.setOnClickListener {
            //Entrem a la colecció users i agafem l'id de cada document
            db.collection("users").get().addOnSuccessListener { result ->
                for (document in result) {
                    //Si la id del document no es usertype agafarem el valor de username més
                        // el valor del usersurname
                    if (document.id != "usertypes") {
                        val user =
                            document.get("username").toString() + " " + document.get("usersurname")
                                .toString()
                        //Agafem aquestos últims valors i els afegirm a l'array d'usuaris
                        arrUser.add(user)
                        //També agafem el document id del document que estem mirant i ho guardem
                        //al array de id d'usuaris
                        arrUserId.add(document.id)
                    }
                }
                //Per últim anem a la pantalla modificar usuari pasant els arrays:
                //  - Array d'usuaris
                //  - Array d'ids d'usuari
                //  - Array de tipus d'usuaris
                view?.findNavController()?.navigate(
                    PantallaAdministradorPrincipalDirections.actionPantallaAdministradorPrincipalToPantallaAdministradorModificarUsuari(
                        arrUser, arrUserId, arrUsertype
                    )
                )

            }

        }

        //Fem que al fer clic al botó per anar a la pantalla nou producte, anem a aquesta pantalla
        //pasant l'array de tipus de producte
        binding.btnPAdministradorPrincipalNouProducte.setOnClickListener { view: View ->
            view.findNavController()
                .navigate(
                    PantallaAdministradorPrincipalDirections.actionPantallaAdministradorPrincipalToPantallaAdministradorNouProducte(
                        arrayTipusProducte
                    )
                )
        }

        //Fem que al fer clic al botó per anar a la pantalla d'administrar productes, anem a aquesta pantalla
        //pasant els arrays de tipus de producte i l'array de productes
        binding.btnPAdministradorPrincipalAdministrarProductes.setOnClickListener { view: View ->
            view.findNavController()
                .navigate(
                    PantallaAdministradorPrincipalDirections.actionPantallaAdministradorPrincipalToPantallaAdministradorAdministrarProducte(
                        arrayTipusProducte,
                        arrayProductes
                    )
                )
        }
        return binding.root
    }
}