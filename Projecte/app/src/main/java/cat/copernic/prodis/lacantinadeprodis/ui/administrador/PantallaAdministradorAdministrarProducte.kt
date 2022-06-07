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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaAdministradorAdministrarProducteBinding
import cat.copernic.prodis.lacantinadeprodis.viewmodel.PantallaAdministradorAdministrarProducteViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class PantallaAdministradorAdministrarProducte : Fragment(), AdapterView.OnItemSelectedListener {

    //Declarem les variables golbals
    lateinit var tipusProducte: String
    lateinit var producte: String
    private var preu: Double = 1.0
    lateinit var prevProducte: String

    private lateinit var viewModel: PantallaAdministradorAdministrarProducteViewModel

    var arrayTipusProducte = ArrayList<String>()
    var arrayProductes = ArrayList<String>()

    var producteVisible: Boolean = true

    private lateinit var adapter: ArrayAdapter<*>

    lateinit var binding: FragmentPantallaAdministradorAdministrarProducteBinding

    private val db = Firebase.firestore
    lateinit var storageRef: StorageReference

    private var documentId: String? = null

    private var latestTmpUri: Uri? = null

    val takeImageResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    binding.imgProducte2.setImageURI(uri)
                }
            }
        }

    //Iniciem el onCreateView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pantalla_administrador_administrar_producte,
            container,
            false
        )
        //Fem que l'imatge del producte desapareixi
        binding.imgProducte2.isGone = true
        //Fem que l'el camp de text del numero decimal del preu del producte desapareixi
        binding.editTextNumberDecimal3.isGone = true
        //Declarem i inicialitzem una referencia al storage
        storageRef = FirebaseStorage.getInstance().getReference()
        //Declarem i inicialitzem el view model
        viewModel =
            ViewModelProvider(this).get(PantallaAdministradorAdministrarProducteViewModel::class.java)

        //Cridem a la funció per carregar l'spinner de tipus de producte
        carregarSpinTipusProductes()
        //Cridem a la funció per carregar l'spinner de producte
        carregarSpinProductes()
        //Cridem a la funció per posar el preu
        setPreu()
        //Cridem a la funció per actualitzar la foto del producte
        actualitzaFoto()
        //Cridem a la funció per indicar si el producte es visible o no
        producteEsVisible()

        return binding.root
    }

    //Funció per indicar quin es el item seleccionat en els spinners
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        //Fem que si el parent del spinner es igual al spinner de tipus de producte, el tipus de producte será el item seleccionat
        if (parent == binding.spinTipusProducte) {
            tipusProducte = parent.getItemAtPosition(pos).toString()

        //Fem que si el parent del spinner es igual al spinner del producte, el producte será el item seleccionat
        } else if (parent == binding.spinSeleccionaProducte) {
            producte = parent.getItemAtPosition(pos).toString()

            //Fem que al camp de text del nom del producte sigui el item seleccionat del spinner
            binding.editTextTextProductName.setText(binding.spinSeleccionaProducte.selectedItem.toString())
        }

        //Agafem l'id del document a través de la base de dades per poder utilitzarlos dins de funcions
        db.collection("productes").get().addOnSuccessListener { result ->
            for (document in result) {
                if (document.get("nom").toString().equals(producte)) {
                    documentId = document.id
                }
                //Cridem la funció actualitzaDades. La cridem desde aqui ja que necesitem l'id del producte per guardarla dins del document.
                //Si ho fem fora del for el documentId donará null
                actualitzaDades()

                //Cridem la funció per indicar si el tick de producte visible sigui marcat o no
                setVisibleTick(documentId)

            }

        }

    }

    //Si en l'spinner no hi ha res sortirá un alert dient que has de seleccionar un tipus de producte
    override fun onNothingSelected(parent: AdapterView<*>) {
        showAlert(getString(R.string.has_de_seleccionar_un_tipus_de_producte))
    }

    //Aquesta funció crea un alert amb el missatge que es pasa per parametres
    private fun showAlert(message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this.requireContext())
        builder.setTitle(getString(R.string.error))
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.acceptar), null)
        val dialog: androidx.appcompat.app.AlertDialog = builder.create()
        dialog.show()
    }

    //Funció per carregar l'spinnner de tipus de productes
    private fun carregarSpinTipusProductes() {

        //Declarem i inicialitzem una variable per guardar el context
        val context = this.requireContext()

        //Declarem i inicialitzem l'spinner
        val spinner: Spinner = binding.spinTipusProducte

        //Agafem els arguments que pasem per parametres
        val args = PantallaAdministradorAdministrarProducteArgs.fromBundle(requireArguments())

        //Fem que si el'array es buit, introduïm dades com si fosi un Array List
        if (arrayTipusProducte.isEmpty()) {
            arrayTipusProducte = args.arrayTipusProducte as ArrayList<String>
        }

        //Fem que l'adpater sigui un item desplegable simple amb l'array de tipus de productes
        adapter =
            ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, arrayTipusProducte)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        //Fem que l'adapter del spinner sigui l'adapter que hem creat
        spinner.adapter = adapter

        //Fem que l'item seleccionat del spinner estigui en aquest context
        spinner.onItemSelectedListener = this
    }

    //Funció per carregar l'spin de productes
    private fun carregarSpinProductes() {

        //Declarem i inicialitzem una variable per guardar el context
        val context = this.requireContext()

        //Declarem i inicialitzem l'spinner
        val spinner: Spinner = binding.spinSeleccionaProducte

        //Agafem els arguments que pasem per parametres
        val args = PantallaAdministradorAdministrarProducteArgs.fromBundle(requireArguments())

        //Fem que si el'array es buit, introduïm dades com si fosi un Array List
        if (arrayProductes.isEmpty()) {
            arrayProductes = args.arrayProductes as ArrayList<String>
        }

        //Fem que l'adpater sigui un item desplegable simple amb l'array de tipus de productes
        adapter =
            ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, arrayProductes)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        //Fem que l'adapter del spinner sigui l'adapter que hem creat
        spinner.adapter = adapter

        //Fem que l'item seleccionat del spinner estigui en aquest context
        spinner.onItemSelectedListener = this
    }

    //Aquesta funció indica si la variable producteVisible es true o false
    private fun producteEsVisible() {
        binding.btnGuardar.setOnClickListener() {
            producteVisible = binding.checkBoxProducteVisible.isChecked
        }
    }

    //Aquesta funió agafa el nom del producte i ho posa en el format correcte per guardarlo a la base de dades
    private fun formatCorrecte(): String {
        var string = binding.editTextTextProductName.text.toString()

        //Pasem l'string a minuscules
        string = string.lowercase(Locale.getDefault())

        //Sustiuim el string per treure els valors que no ens interesen
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

        string = string.trim()

        return string
    }

    // Funció per actualitzar le dades a la base de dades
    private fun actualitzaDades() {
        //Escoltem el botó per guardar
        binding.btnGuardar.setOnClickListener() {
            //Si el camp de text per posar el preu es buit la variable preu será el valor d'aquest camp
            if (!binding.editTextNumberDecimal3.text.toString().isEmpty()) {
                preu = binding.editTextNumberDecimal3.text.toString().toDouble()
            }

            //Guardem el producte previ per poder borrar l'imatge del producte anterior
            prevProducte = binding.spinSeleccionaProducte.selectedItem.toString()

            //Si l'imatge del producte no ha desaparegut, es borrará l'imatge del producte anterior i es pujara una imatge amb el nou nom del producte
            if (!binding.imgProducte2.isGone) {
                borrarImatge()
                pujarImatge(it)
            }

            //Actualitzem les dades del prodcute
            db.collection("productes").document(documentId.toString()).update(
                hashMapOf(
                    "tipus" to binding.spinTipusProducte.selectedItem.toString(),
                    "nom" to binding.editTextTextProductName.text.toString(),
                    "nomid" to formatCorrecte(),
                    "preu" to preu,
                    "img" to "productes/" + binding.editTextTextProductName.text.toString() + ".jpg",
                    "visible" to binding.checkBoxProducteVisible.isChecked
                ) as Map<String, Any>
            )

        }
    }

    //Funció per indicar el preu
    fun setPreu() {
        //Escoltem al radio group on son els diferents preus dels productes
        binding.radioGroup2
            .setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    //Si el radio d'un euro está marcat; el preu será 1, el el camp de text será invible,
                    //desapareixerà i es treurá el text
                    R.id.radio1Euro2 -> {
                        preu = 1.0
                        binding.editTextNumberDecimal3.visibility = View.INVISIBLE
                        binding.editTextNumberDecimal3.setText("")
                        binding.editTextNumberDecimal3.isGone = true

                    }
                    //Si el radio d'un euro está marcat; el preu será 2, el el camp de text será invible,
                    //desapareixerà i es treurá el text
                    R.id.radio2Euro2 -> {
                        preu = 2.0
                        binding.editTextNumberDecimal3.visibility = View.INVISIBLE
                        binding.editTextNumberDecimal3.setText("")
                        binding.editTextNumberDecimal3.isGone = true

                    }
                    //Si el radio d'un euro está marcat; el el camp de text será visible i apareixerá
                    R.id.radioAltrePreu2 -> {
                        binding.editTextNumberDecimal3.visibility = View.VISIBLE
                        binding.editTextNumberDecimal3.isGone = false
                    }
                }
            }
    }

    //Aquesta fá que l'imatge seleccionada en la galeria sigui la foto del producte
    private val startForActivityGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.data
            //setImageUri només funciona per rutes locals, no a internet
            binding.imgProducte2.setImageURI(data)
        }
    }

    //Funció que fa que sobri la galeria
    private fun obrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startForActivityGallery.launch(intent)
    }

    //Amb aquesta funció agafarem l'Uri de l'imatge
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

    //Aquesta funcio fa que la foto del producte s'actualitzi
    private fun actualitzaFoto() {

        //Escoltem el botó per seleccionar l'imatge desda la càmera
        binding.imgCamera.setOnClickListener() {
            //Fem que l'imatge del prodcute apareixi
            binding.imgProducte2.isGone = false
            //Cridem a la funció per obrir la càmera
            obrirCamera()
        }

        //Escoltem el botó per pujar l'imatge
        binding.imgPujarImatge.setOnClickListener() {
            //Fem que l'imatge del producte apareixi
            binding.imgProducte2.isGone = false
            //Cridem a la funció per obrir la galeria
            obrirGaleria()
        }
    }

    //Funció per borrar l'imatge amb el nom antic del producte
    private fun borrarImatge() {
        //Fem una refeencia al storeage de l'imatge del producte aterior amb la variable de prevProducte
        val pathReference =
            storageRef.child("productes/" + prevProducte + ".png")

        //Fem que es borri la referencia
        pathReference.delete()
    }

    fun pujarImatge(view: View) {
        // pujar imatge al Cloud Storage de Firebase
        // https://firebase.google.com/docs/storage/android/upload-files?hl=es

        // Creem una referència amb el path i el nom de la imatge per pujar la imatge
        val pathReference =
            storageRef.child("productes/" + binding.editTextTextProductName.text.toString() + ".png")
        val bitmap =
            (binding.imgProducte2.drawable as BitmapDrawable).bitmap // agafem la imatge del imageView
        val baos = ByteArrayOutputStream() // declarem i inicialitzem un outputstream

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            baos
        ) // convertim el bitmap en outputstream
        val data = baos.toByteArray() //convertim el outputstream en array de bytes.

        val uploadTask = pathReference.putBytes(data)
        uploadTask.addOnFailureListener {
            Snackbar.make(view, getString(R.string.error_al_pujar_la_foto), Snackbar.LENGTH_LONG).show()
            it.printStackTrace()

        }.addOnSuccessListener {
            Snackbar.make(view, getString(R.string.exit_al_pujar_la_foto), Snackbar.LENGTH_LONG).show()
        }
    }

    // Funció per indicar si el tick del producte visible estigui marcat o no
    private fun setVisibleTick(idDocument: String?) {
            // Llegim si la variable visible del document
            db.collection("productes").document(idDocument.toString()).get().addOnSuccessListener { document ->
                // En el cas de que sigui true, el checkbox que indica si el producte es visible estará marcat
                if (document.get("visible") == true) {
                    binding.checkBoxProducteVisible.isChecked = true
                }
            }

    }
}