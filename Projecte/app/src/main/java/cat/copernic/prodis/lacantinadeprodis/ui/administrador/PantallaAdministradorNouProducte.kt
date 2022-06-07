package cat.copernic.prodis.lacantinadeprodis.ui.administrador

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaAdministradorNouProducteBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class PantallaAdministradorNouProducte : Fragment(), AdapterView.OnItemSelectedListener {

    //Definim les variables globals
    lateinit var tipusProducte: String

    lateinit var binding: FragmentPantallaAdministradorNouProducteBinding

    private var latestTmpUri: Uri? = null

    val takeImageResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    binding.imgProducte.setImageURI(uri)
                }
            }
        }

    private var preu: Double = 0.0

    private val db = Firebase.firestore

    lateinit var storageRef: StorageReference

    private lateinit var adapter: ArrayAdapter<*>

    var arrayTipusProducte = ArrayList<String>()

    //Comennça el onCreateView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pantalla_administrador_nou_producte, container, false
        )
        //Fem que l'edit text i l'imatge del producte s'amaguin.
        binding.editTextNumberDecimal2.isGone = true
        binding.imgProducte.isGone = true

        //Inicialitzem l'storageRef.
        storageRef = FirebaseStorage.getInstance().getReference()
        //Inicialitzem el tipus de producte.
        tipusProducte = ""

        //Cridem a la funció per carregar l'spin de tipus de productes
        carregarSpinTipusProducte()

        //Cridem la funció per obrir la camara o galeria
        obrirCamGaleria()

        //Fem que l'imatge del producte sigui "enabled" false
        binding.imgProducte.isEnabled = false

        //Cridem a la funció setPreu
        setPreu()

        //Cridem a la funció per guardar dades
        guardadDades()

        return binding.root
    }

    //Fem que la variable tipusProducte sigui el item seleccionat.
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        tipusProducte = parent.getItemAtPosition(pos).toString()
    }

    //Si no es selecciona res sorirá un alert dient que s'ha de seleccionar una opció
    override fun onNothingSelected(parent: AdapterView<*>) {
        showAlert(getString(R.string.selecciona_el_producte))
    }

    //Fem que surti un alert amb el text que li pasem per parametres
    private fun showAlert(message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this.requireContext())
        builder.setTitle(getString(R.string.error))
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.acceptar), null)
        val dialog: androidx.appcompat.app.AlertDialog = builder.create()
        dialog.show()
    }

    //Aquesta fá que l'imatge seleccionada en la galeria sigui la foto del producte
    private val startForActivityGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.data
            //setImageUri només funciona per rutes locals, no a internet
            binding.imgProducte.setImageURI(data)
        }
    }

    //Funció que fa que sobri la galeria
    private fun obrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startForActivityGallery.launch(intent)
    }

    //Funció que fa que s'obri la camera
    private fun obrirCamera() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri

                takeImageResult.launch(uri)
            }
        }
    }

    //Amb aquesta funció agafarem l'Uri de l'imatge
    private fun getTmpFileUri(): Uri {
        val tmpFile =
            File.createTempFile("tmp_image_file", ".png", requireContext().cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }

        return FileProvider.getUriForFile(
            requireContext().applicationContext,
            "cat.copernic.prodis.lacantinadeprodis.provider",
            tmpFile
        )
    }

    //Funció per setejar el preu
    fun setPreu() {
        //Escoltarem el radioFroup on son els diferents radio buttons per seleccionar el preu.
        binding.radioGroup
            .setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    //Fem que si el radio de 1 euro está checked; el preu será 1, el camp de text: será invisible, no hi haura text i desapareixerá
                    R.id.radio1Euro -> {
                        preu = 1.0
                        binding.editTextNumberDecimal2.visibility = View.INVISIBLE
                        binding.editTextNumberDecimal2.setText("")
                        binding.editTextNumberDecimal2.isGone = true
                    }
                    //Fem que si el radio de 2 euros está checked; el preu será 1, el camp de text: será invisible, no hi haura text i desapareixerá
                    R.id.radio2Euro -> {
                        preu = 2.0
                        binding.editTextNumberDecimal2.visibility = View.INVISIBLE
                        binding.editTextNumberDecimal2.setText("")
                        binding.editTextNumberDecimal2.isGone = true
                    }
                    //Fem que si el radio d'un altre preu está checked; el preu será el que s'introdueixi en el camp de text, será invisible i apareixerá
                    R.id.radioAltrePreu -> {
                        binding.editTextNumberDecimal2.visibility = View.VISIBLE
                        binding.editTextNumberDecimal2.isGone = false
                    }
                }
            }
    }

    //Aquesta funció fa que el format del nom del producte sigui correcte
    private fun formatCorrecte(): String {
        //Agafem el text del nom del producte i ho guardem a la variable string
        var string = binding.editTextNomProducte.text.toString()

        //Fem que string estigui en minuscules
        string = string.lowercase(Locale.getDefault())

        //Remplaçem l'string per adaptarlo a com el volem guardar
        string = string.replace(
            " de ",
            ""
        )

        string = string.replace(
            " ",
            ""
        )

        string = string.replace(
            " amb ",
            ""
        )

        string = string.replace(
            " i ",
            ""
        )

        //Fem un trm a l'string
        string = string.trim()

        //Retornem l'string
        return string
    }

    // Pujar imatge al Cloud Storage de Firebase
    fun pujarImatge(view: View) {
        // Creem una referència amb el path i el nom de la imatge per pujar la imatge
        val pathReference =
            storageRef.child("productes/" + binding.editTextNomProducte.text.toString() + ".png")
        val bitmap =
            (binding.imgProducte.drawable as BitmapDrawable).bitmap // agafem la imatge del imageView
        val baos = ByteArrayOutputStream() // declarem i inicialitzem un outputstream

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            baos
        ) // Convertim el bitmap en outputstream
        val data = baos.toByteArray() //convertim el outputstream en array de bytes.

        val uploadTask = pathReference.putBytes(data)
        uploadTask.addOnFailureListener {
            Snackbar.make(view, getString(R.string.error_al_pujar_la_foto), Snackbar.LENGTH_LONG).show()
            it.printStackTrace()

        }.addOnSuccessListener {
            Snackbar.make(view, getString(R.string.exit_al_pujar_la_foto), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun obrirCamGaleria(){
        //Fem que al clickar al botó de la camera:
        //  - Cridem a la funció obrir camera
        //  - Fem que l'imatge del producte sigui visible
        //  - Fem que l'imatge del producte sigui "gone" false, si fem que sigui "gone" true l'imatge no ocupará espai.
        binding.imgFotoCamera.setOnClickListener() {
            obrirCamera()
            binding.imgProducte.visibility = View.VISIBLE
            binding.imgProducte.isGone = false

        }

        //Fem que al clickar al botó de la galeria:
        //  - Cridem a la funció obrir camera
        //  - Fem que l'imatge del producte sigui visible
        //  - Fem que l'imatge del producte sigui "gone" false, si fem que sigui "gone" true l'imatge no ocupará espai.
        binding.imgFotoGaleria.setOnClickListener() {
            obrirGaleria()
            binding.imgProducte.visibility = View.VISIBLE
            binding.imgProducte.isGone = false
        }

    }

    private fun guardadDades(){
        //Definim i inicialitzem la funció num.
        var num: Int
        num = 0

        //Escoltem al botó per guardar.
        binding.btnPAdministradorNouProducteGuardar.setOnClickListener {
            //Cridem a la funció pujarImatge per pujar l'imatge de perfil del usuari.
            pujarImatge(it)

            //Fem que si el editText on irá l'opció de un altre pres no es buit, el preu será el que hi ha en el camp de text.
            if (!binding.editTextNumberDecimal2.text.toString().isEmpty()) {
                preu = binding.editTextNumberDecimal2.text.toString().toDouble()
            }

            //Fem que llegeixi la col·lecció de productes
            db.collection("productes").get().addOnSuccessListener { result ->
                //Per cada document sumará un a la variable num.
                for (document in result) {
                    num++
                }
                //Fem que a la col·lecció productes crei un document amb:
                //  - Un nom que será el camp de text del nom del producte
                //  - Un preu que será la variable preu
                //  - Un tipus que será la variable tipusProducte
                //  - Una atribut visible que s'inicicialitzará a true
                //  - Un nomid que será el resultant de cridar a la funció formatCorrecte()
                //  - Un img que será un string per saber on es guarda l'imatge del producte
                //  - Un idProducte que será la variable num
                db.collection("productes").document().set(
                    hashMapOf(
                        "nom" to binding.editTextNomProducte.text.toString(),
                        "preu" to preu,
                        "tipus" to tipusProducte,
                        "visible" to true,
                        "nomid" to formatCorrecte(),
                        "img" to "productes/" + binding.editTextNomProducte.text.toString() + ".jpg",
                        "idProducte" to num
                    ) as Map<String, Any>
                )
            }

            //Cuan s'acabi de crear el document sortirá un toast indicant que el producte s'ha afegit amb éxit
            Toast.makeText(
                this.requireContext(),
                getString(R.string.s_ha_afegit_el_producte_amb_exit),
                Toast.LENGTH_SHORT
            ).show()
            //Tormarem a inicialitzar la variable num a 0 per si es vol afegir un altre producte
            num = 0
        }
    }

    private fun carregarSpinTipusProducte(){
        //Declarem i inicialitzem l'spinner
        val spinner: Spinner = binding.spinTipusProducte

        val context = this.requireContext()
        //Declarem i inicialitzem la variable args per agafar les dades que pasam per parametres
        val args = PantallaAdministradorNouProducteArgs.fromBundle(requireArguments())

        //Fem que si l'array de tipus de productes es buit, li pasarem els args en forma de ArrayList
        if (arrayTipusProducte.isEmpty()) {
            arrayTipusProducte = args.arrayTipusProducte as ArrayList<String>
        }

        //Inicialitzem l'adapter amb l'array de tispus de productes
        adapter =
            ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, arrayTipusProducte)

        //Fem que l'adapter sigui un sigui un item deplegable
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        //Fem que l'adapter del spiner
        spinner.adapter = adapter

        //Fem que l'item que es seleccioni en l'spinner sigui en aquest context
        spinner.onItemSelectedListener = this
    }
}