package cat.copernic.prodis.lacantinadeprodis.ui.comandes

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaSeleccioNomClientComandaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PantallaSeleccioNomCientComanda : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var userArr: ArrayList<*>
    private lateinit var spinner: Spinner
    private lateinit var adapter: ArrayAdapter<*>
    private lateinit var dni: String
    private lateinit var docId: String
    private lateinit var binding: FragmentPantallaSeleccioNomClientComandaBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_seleccio_nom_client_comanda, container, false
        )
        val args = PantallaSeleccioNomCientComandaArgs.fromBundle(requireArguments())
        userArr = args.arrUser
        dni = args.dni
        docId = args.docId
        spinner = binding.spnNomClient

        val context = this.requireContext()

        //declaració de tots els elements del spinner
        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, userArr)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        binding.btnTornaEnrerre.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnConfirmar.setOnClickListener {
            var preu: Double = 0.0
            //Si la opció seleccionada és extern asignarem com a nom de client el nom  que l'usuari hagi introduit en el camp de text
            if(spinner.selectedItemPosition == userArr.size-1){
                db.collection("comandes").get().addOnSuccessListener { result ->
                    for (dc in result) {
                        // si el dni de l'usuari es el mateix que el de la BD assignem comandaComençada a false i visible a true
                        if (dc.get("comandaComencada").toString() == "true" && dc.get("user")
                                .toString() == dni
                        ) {
                            db.collection("comandes").document(dc.id).collection("productes").get()
                                .addOnSuccessListener { result ->
                                    for (document in result) {
                                        var idProducte = document.get("idProducte")
                                        db.collection("productes").get().addOnSuccessListener {
                                            for (document in it) {
                                                if (document.get("idProducte").toString() == idProducte) {
                                                    preu += document.get("preu") as Double
                                                }

                                                db.collection("comandes").document(dc.id).update(
                                                    hashMapOf(
                                                        "preuTotal" to preu
                                                    ) as Map<String, Any>
                                                )
                                            }
                                        }
                                    }
                                    db.collection("comandes").document(docId).update(
                                        hashMapOf(
                                            "comandaComencada" to false,
                                            "visible" to true,
                                            "user" to binding.dtTxtNomUsuariExtern.text.toString(),
                                        ) as Map<String, Any>
                                    )

                                    db.collection("comandes").document(docId).get().addOnSuccessListener { doc ->
                                        val builder = AlertDialog.Builder(context)
                                        val msg ="${getString(R.string.comanda_enviada,binding.dtTxtNomUsuariExtern.text)} ${getString(R.string.preu_total)} ${doc.get("preuTotal").toString().toDouble()} €"
                                        builder.setMessage(msg)
                                        builder.setPositiveButton(R.string.acceptar) {_,_ ->
                                            findNavController().navigate(PantallaSeleccioNomCientComandaDirections.actionPantallaSeleccioNomClientComandaToPantallaSeleccioTipusProducte())
                                        }
                                        val dialog: AlertDialog = builder.create()
                                        dialog.show()

                                    }

                                }
                        }
                    }
                }
            }else{
                //Si la opció seleccionada NO és extern asignarem com a nom de client el nom de la posició del spinner
                db.collection("comandes").get().addOnSuccessListener { result ->
                    for (dc in result) {
                        if (dc.get("comandaComencada").toString() == "true" && dc.get("user")
                                .toString() == dni
                        ) {
                            db.collection("comandes").document(dc.id).collection("productes").get()
                                .addOnSuccessListener { result ->
                                    for (document in result) {
                                        var idProducte = document.get("idProducte")
                                        db.collection("productes").get().addOnSuccessListener {
                                            for (document in it) {
                                                if (document.get("idProducte").toString() == idProducte) {
                                                    preu += document.get("preu") as Double
                                                }

                                                db.collection("comandes").document(dc.id).update(
                                                    hashMapOf(
                                                        "preuTotal" to preu
                                                    ) as Map<String, Any>
                                                )
                                            }
                                        }
                                    }
                                    db.collection("comandes").document(docId).update(
                                        hashMapOf(
                                            "comandaComencada" to false,
                                            "visible" to true,
                                            "user" to spinner.selectedItem.toString(),
                                        ) as Map<String, Any>
                                    )

                                    db.collection("comandes").document(docId).get().addOnSuccessListener { doc ->
                                        val builder = AlertDialog.Builder(context)
                                        val msg ="${getString(R.string.comanda_enviada, spinner.selectedItem)} ${getString(R.string.preu_total)} ${doc.get("preuTotal").toString().toDouble()} €"
                                        builder.setMessage(msg)
                                        builder.setPositiveButton(R.string.acceptar) {_,_ ->
                                            findNavController().navigate(PantallaSeleccioNomCientComandaDirections.actionPantallaSeleccioNomClientComandaToPantallaSeleccioTipusProducte())
                                        }
                                        val dialog: AlertDialog = builder.create()
                                        dialog.show()

                                    }

                                }


                            break
                        }
                    }
                }
            }


        }


        return binding.root
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // Si la opció seleccionada és Extern, mostrem el camp de text
        if (position == userArr.size - 1) {
            binding.dtTxtNomUsuariExtern.visibility = View.VISIBLE
        } else {
            // Si la opció selecciona no és Extern, ocultem el camp de text
            binding.dtTxtNomUsuariExtern.visibility = View.INVISIBLE
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

}
