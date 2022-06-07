package cat.copernic.prodis.lacantinadeprodis.ui.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cat.copernic.prodis.lacantinadeprodis.R
import cat.copernic.prodis.lacantinadeprodis.databinding.FragmentPantallaEdicioPerfilBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import androidx.core.view.isGone
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cat.copernic.prodis.lacantinadeprodis.viewmodel.PantallaEdicioPerfilViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import cat.copernic.prodis.lacantinadeprodis.MainActivity
import cat.copernic.prodis.lacantinadeprodis.utils.utils
import java.util.*

class PantallaEdicioPerfil : AppCompatActivity(), LifecycleOwner {

    //Definim les variables globals
    private var dni = ""

    private val db = FirebaseFirestore.getInstance()

    lateinit var binding: FragmentPantallaEdicioPerfilBinding
    private var latestTmpUri: Uri? = null
    val takeImageResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    binding.userIcon.setImageURI(uri)
                }
            }
        }

    private val NOTIFICATION_ID = 0

    private lateinit var notificationChannel: NotificationChannel
    private lateinit var build: Notification.Builder

    lateinit var storageRef: StorageReference

    private lateinit var viewModel: PantallaEdicioPerfilViewModel

    private var idiomaR = ""

    //Comennça el onCreate
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<FragmentPantallaEdicioPerfilBinding>(
            this,
            R.layout.fragment_pantalla_edicio_perfil
        )

        title = "Edició Perfil"

        if(utils().isTabablet(applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        //Fem que al prémer el botó de per cambiar la foto cridi a la funció per triar si volem agafar la foto desde la càmera o desde la galeria
        binding.btnCambiarFoto.setOnClickListener() { view: View ->
            triaCamGaleria()
        }

        //Declrem el view model
        viewModel = ViewModelProvider(this)[PantallaEdicioPerfilViewModel::class.java]

        viewModel.dni.observe(this, Observer {
            dni = it
            //Cridem a la funció per agafar l'imatge del usuari
            agafarImatgeUsuari(dni)

        })


        //Fem que desde el view model s'obvervi els canvis del camp de text del nom i del cognom
        viewModel.nom.observe(this, Observer {
            binding.editTxtNom.setText(it.toString())

        })

        viewModel.cognom.observe(this, Observer {
            binding.editTxtCognom.setText(it.toString())

        })

        //Cridem a la funció per guardar les dades
        guardarDades()

        //Funció per saber quin idioma ha sigut seleccionat
        seleccionaIdioma()

        supportActionBar?.title = ""
    }

    //Aquesta funció fará que es comprovi si hi han dades en els camps indicats
    private fun datavalids(nom: String, cognom: String): Boolean {
        var error = ""
        var bool = true
        if (nom.isEmpty()) {
            error += getString(R.string.has_d_introduir_el_nom) + "\r"
            bool = false
        }
        if (cognom.isEmpty()) {
            error += getString(R.string.has_d_introduir_el_cognom) + "\r"
            bool = false
        }
        if (error != "") {
            showAlert(error)
        }
        return bool
    }

    //Aquesta funció mirará si el email té el format correcte
    private fun checkEmailFormat(email: String): Boolean {
        val EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    //Aquesta funció crea un alert amb els errors de la funció dataValids
    private fun showAlert(message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.error))
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.acceptar), null)
        val dialog: androidx.appcompat.app.AlertDialog = builder.create()
        dialog.show()
    }

    //Aquesta fá que l'imatge seleccionada en la galeria sigui la foto del usuari
    private val startForActivityGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.data
            //setImageUri només funciona per rutes locals, no a internet
            binding.userIcon.setImageURI(data)
        }
    }

    //Aquesta funció fa que s'obri la galeria
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
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            applicationContext,
            "cat.copernic.prodis.lacantinadeprodis.provider",
            tmpFile
        )
    }

    //Es crea un dialog amb dos botons per seleccionar d'on ses vol triar l'imatge
    fun triaCamGaleria() {

        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(getString(R.string.d_on_vols_treure_la_foto))
        alertDialog.setMessage(getString(R.string.selecciona_un))

        //Indiquem al botó positiu que será el que obrirá la galeria
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, getString(R.string.galeria)
        ) { dialog, which -> obrirGaleria() }

        //Indiquem al botó negatiu que será el que obrirá la càmera
        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, getString(R.string.camara)
        ) { dialog, which -> obrirCamera() }
        alertDialog.show()


        //Incialitzem i posem els botons dins del alert
        val btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        val layoutParams = btnPositive.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        btnPositive.layoutParams = layoutParams
        btnNegative.layoutParams = layoutParams
    }

    fun pujarImatge(view: View) {
        // pujar imatge al Cloud Storage de Firebase
        // https://firebase.google.com/docs/storage/android/upload-files?hl=es

        // Creem una referència amb el path i el nom de la imatge per pujar la imatge
        val pathReference = storageRef.child("users/" + dni + ".jpg")


        val bitmap =
            (binding.userIcon.drawable as BitmapDrawable).bitmap // agafem la imatge del imageView
        val baos = ByteArrayOutputStream() // declarem i inicialitzem un outputstream

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            baos
        ) // convertim el bitmap en outputstream
        val data = baos.toByteArray() //convertim el outputstream en array de bytes.

        val uploadTask = pathReference.putBytes(data)
        uploadTask.addOnFailureListener {
            Snackbar.make(
                view,
                getString(R.string.error_al_pujar_la_foto),
                Snackbar.LENGTH_LONG
            )
                .show()
            it.printStackTrace()

        }.addOnSuccessListener {
            Snackbar.make(view, getString(R.string.exit_al_pujar_la_foto), Snackbar.LENGTH_LONG)
                .show()
        }
    }

    //Aquesta funció agafará l'imatge del usuari desde la base de dades
    private fun agafarImatgeUsuari(dni: String) {
        //Declarem la referencia del fire strorage
        storageRef = FirebaseStorage.getInstance().getReference()

        //Creem una referencia a l'imatge del usuari
        var imgRef = Firebase.storage.reference.child("users/" + dni + ".jpg")

        //Agafem l'imatge i la posem en l'imatge del usuari
        imgRef.downloadUrl.addOnSuccessListener { Uri ->
            val imgUrl = Uri.toString()

            Glide.with(this).load(imgUrl).into(binding.userIcon)
        }

    }

    //Funció per guardar les dades en la base de dades
    private fun guardarDades() {
        //Escoltem al botó de guardar
        binding.btnGuardar.setOnClickListener() { view: View ->
            //Cridem a la funció per pujar la foto del usuari
            if (binding.userIcon.drawable.constantState != ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_baseline_account_circle_24
                )?.constantState
            ) {
                pujarImatge(view)
            }
            //Comprovem si les dades del nom i del cognom son correctes
            if (datavalids(
                    binding.editTxtNom.text.toString(),
                    binding.editTxtCognom.text.toString()
                )
            ) {
                //Anem a l acolecció d'usuaris i cambiem les dades del usuari on el dni es el que agafem per parametres
                db.collection("users").document(dni).update(
                    hashMapOf(
                        "username" to binding.editTxtNom.text.toString(),
                        "usersurname" to binding.editTxtCognom.text.toString(),
                    ) as Map<String, Any>
                )

                //Quan acaba d'actualizar les dades surt un toast indicant que els canvis s'han fet amb èxit
                Toast.makeText(this, getString(R.string.canvis_amb_exit), Toast.LENGTH_SHORT)
                    .show()

            }

            //Cridem a la funció createChannel per crear un canal per poder enviar una notificació en el cas de que l'api sigui major a 26
            createChannel(
                //Agagem del fitxer de strings un id i un nom per el nostre canal
                getString(R.string.channel_id),
                getString(R.string.channel_name)
            )

            //Definim i inicialitzem una variable per pasarli el NotificationManager
            val notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            ) as NotificationManager

            //Indiquem que notificationManager enviï una notificació amb un text que agafara del fitxer de strings i en aquest context
            notificationManager.sendNotification(
                this.getString(R.string.enhorabona_canvis),
                this
            )

            //Cridem a la funció posaIdioma
            posaIdioma()
        }
    }


    //Funció per el Notifaction manager que tindrá per parametres el missatge de la notificació i el context de l'app
    private fun NotificationManager.sendNotification(
        messageBody: String,
        applicationContext: Context
    ) {

        //Builder per crear la notificació més tard
        val builder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.channel_id)
        )
            //Indiquem quin será l'icona que sortirá en la notificació
            .setSmallIcon(R.drawable.logo_foreground)
            //Indiquem quin será el text principal de la notificació
            .setContentTitle(
                applicationContext
                    .getString(R.string.els_canvis_s_han_fet_amb_exit)
            )
            //Aquest será el text de la notificiacó
            .setContentText(messageBody)

        //Creem la notificació amb un id i amb el builder que hem creat abans
        notify(NOTIFICATION_ID, builder.build())


    }

    //Funció per crear el canal que tindrá in channelId i un channelName
    private fun createChannel(channelId: String, channelName: String) {
        //Fem un if per comprobar si ela versió del sdk es correcte
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Indiquem que el canal de la notificació sera de tipus NotificationChannel agafant els valors de channelId, de channelName i agafant l'importancia de la
            // notificació
            notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )

            //Indiquem que s'activi la llum del nostre dispositiu al rebre la notificació
            notificationChannel.enableLights(true)
            //Indiquem el color de la llum del nostre dispositiu, en aquest cas será blanc
            notificationChannel.lightColor = Color.WHITE
            //Indiquem que volem que el nostre dispositiu vibri al rebre la notificació
            notificationChannel.enableVibration(true)
            //Indiquem la descripció de la notificació
            notificationChannel.description = getString(R.string.descripcio_notificacio)

            //Definim i inicialitzem una variable notificationManager que será de tipus NotificationManager
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )

            //Amb la variable que acabem de crear li indicarem que creï un canal amb els paràmetres que li hem indicat abans
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    //Funció per canviar l'idioma
    private fun idioma(lenguage: String, country: String) {
        val localitzacio = Locale(lenguage, country)

        Locale.setDefault(localitzacio)

        var config = Configuration()

        config.locale = localitzacio
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    //Funció per saber quin idioma s'ha seleccionat
    private fun seleccionaIdioma() {
        //Escoltem el radioGroup de l'idioma
        binding.radioGroup
            .setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    //Fem que si el radioCat está marcat el valor d'idiomaR será "cat"
                    R.id.radioCat -> {
                        idiomaR = "cat"
                    }
                    //Fem que si el radioEsp está marcat el valor d'idiomaR será "esp"
                    R.id.radioEsp -> {
                        idiomaR = "esp"
                    }
                    //Fem que si el radioEng está marcat el valor d'idiomaR será "eng"
                    R.id.radioEng -> {
                        idiomaR = "eng"
                    }
                }
            }
    }

    //Funció per posar l'idioma al que es canviará
    private fun posaIdioma() {
        //Si el valor de idiomaR es "cat" el valors que es pasaran per canviar d'idomoa serán "ca" i "ES"
        if (idiomaR == "cat") {
            idioma("ca", "ES")
            val intent = Intent(this, PantallaEdicioPerfil::class.java).apply {
            }
            finish()
            startActivity(intent)
            //Si el valor de idiomaR es "es" el valors que es pasaran per canviar d'idomoa serán "es" i "ES"
        } else if (idiomaR == "esp") {
            idioma("es", "ES")
            val intent = Intent(this, PantallaEdicioPerfil::class.java).apply {
            }
            finish()
            startActivity(intent)
            //Si el valor de idiomaR es "eng" el valors que es pasaran per canviar d'idomoa serán "eng" i ""
        } else if (idiomaR == "eng") {
            idioma("en", "")
            val intent = Intent(this, PantallaEdicioPerfil::class.java).apply {
            }
            finish()
            startActivity(intent)
        }
    }
}