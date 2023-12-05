package application.mobile.smarthouse

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import application.mobile.smarthouse.databinding.ActivityStarterBinding
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.ByteArrayOutputStream


class Starter : AppCompatActivity() {

    private lateinit var binding: ActivityStarterBinding
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        UserInfo.initializationUser(this) {
            if (currentUser == null) {

                binding = ActivityStarterBinding.inflate(layoutInflater)
                sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

                setContentView(binding.root)
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestServerAuthCode(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

                val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
                mGoogleSignInClient.signOut()

                with(binding) {

                    getStartedGoogle.setOnClickListener {
                        val signInIntent = mGoogleSignInClient.signInIntent
                        startActivityForResult(signInIntent, RS_SIGN_IN)
                    }

                    // val callbackManager = CallbackManager.Factory.create()
                    getStartedFacebook.setOnClickListener {

                    }
                }
            } else {
                val nextIntent: Intent
                if (UserInfo.homes.isEmpty()) {
                    nextIntent = Intent(this@Starter, CreateHomeActivity::class.java)
                }
                else
                {
                    nextIntent = Intent(this@Starter, BaseActivity::class.java)
                }
                startActivity(nextIntent)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when(requestCode) {
            RS_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleGoogleSignInResult(task)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Toast.makeText(this,"signInResult:failed code=" + e.statusCode,Toast.LENGTH_SHORT).show()
        }
    }


        private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        saveUserDataToDatabase(user, account)
                    } else {
                        Toast.makeText(this, ""+task.exception, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        private fun saveUserDataToDatabase(user: FirebaseUser?, account: GoogleSignInAccount) {
            if (user != null) {


                val image =
                    GlobalObj.storage.child("profilePhoto/${account.id.toString()}/profileph.jpg")
                uploadProfileImage(user.uid, account.photoUrl.toString())

                UserInfo.saveProfileImage(this, image)
                val editor = sharedPreferences.edit()
                editor.putString("userId", user.uid)
                editor.putString("userName", account.displayName)
                editor.putString("userEmail", account.email)
                editor.putString("imagePath", UserInfo.profile_ph)
                editor.apply()


                val userReference = GlobalObj.db.collection("users").document(user.uid)

                userReference.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (!documentSnapshot.exists()) {
                            val userfordb = hashMapOf(
                                "user_id" to user.uid,
                                "name" to account.displayName,
                                "email" to account.email,
                            )
                            GlobalObj.db.collection("users").document(user.uid)
                                .set(userfordb)
                        }
                    }
                    .addOnFailureListener {
                        // Ошибка при чтении данных из Firestore
                        // ...
                    }

                UserInfo.initializationUser(this) {
                    val nextIntent: Intent
                    if (UserInfo.homes.isEmpty()) {
                        nextIntent = Intent(this@Starter, CreateHomeActivity::class.java)
                    } else {
                        nextIntent = Intent(this@Starter, BaseActivity::class.java)
                    }

                    startActivity(nextIntent)
                }
            }
        }



//        UserInfo.initializationUser(this@Starter) {
//
//            if (UserInfo.user_id != "" && UserInfo.name != "" && UserInfo.email != "") {
//                if(UserInfo.homes.isNotEmpty()) {
//                    val intent = Intent(this@Starter, BaseActivity::class.java)
//                    startActivity(intent)
//                }
//                else
//                {
//                    val intent = Intent(this@Starter, CreateHomeActivity::class.java)
//                    startActivity(intent)
//                }
//            }
//            else {
//
//                setContentView(binding.root)
////                supportActionBar?.setDisplayShowTitleEnabled(false)
////                supportActionBar?.hide()
//
//                val callbackManager = CallbackManager.Factory.create()
//
//
//                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                    .requestIdToken(getString(R.string.default_web_client_id))
//                    .requestServerAuthCode(getString(R.string.default_web_client_id))
//                    .requestEmail()
//                    .build()
//
//
//                val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
//                LoginManager.getInstance().logOut()
//                mGoogleSignInClient.signOut()
//
//                binding.getStartedGoogle.setOnClickListener {
//                    val signInIntent = mGoogleSignInClient.signInIntent
//                    startActivityForResult(signInIntent, 1)
//                }
//
//                binding.getStartedFacebook.registerCallback(
//                    callbackManager,
//                    object : FacebookCallback<LoginResult> {
//
//                        override fun onSuccess(result: LoginResult) {
//
//                            val accessToken = result.accessToken
//                            val request =  GraphRequest.newMeRequest(accessToken) { jsonObject, _ ->
//                                val userId = jsonObject?.getString("id")
//                                val userName = jsonObject?.getString("name")
//                                val userEmail = jsonObject?.getString("email")
//                                val imageUrl = "https://graph.facebook.com/$userId/picture?type=large"
//
//                                val image = GlobalObj.storage.child("profilePhoto/${userId.toString()}/profileph.jpg")
//
//                                val editor = sharedPreferences.edit()
//                                editor.putString("userId", userId)
//                                editor.putString("userName", userName)
//                                editor.putString("userEmail", userEmail)
//                                editor.apply()
//
//                                uploadProfileImage(userId.toString(),imageUrl)
//
//                                UserInfo.saveProfileImage(this@Starter, image){}
//
//
//                                UserInfo.initializationUser(this@Starter) {
//
//                                    if(UserInfo.user_id =="" && UserInfo.name == "" && UserInfo.email == "") {
//
//                                        val user = hashMapOf(
//                                            "user_id" to userId,
//                                            "name" to userName,
//                                            "email" to userEmail,
//                                        )
//
//                                        GlobalObj.db.collection("users").add(user)
//
//                                        UserInfo.initializationUser(this@Starter) {
//                                            val intent =
//                                                Intent(this@Starter, CreateHomeActivity::class.java)
//                                            startActivity(intent)
//                                        }
//                                    }
//                                    else{
//                                        val intent =
//                                            Intent(this@Starter, BaseActivity::class.java)
//                                        startActivity(intent)
//                                    }
//                                }
//                            }
//                            val parameters = Bundle()
//                            parameters.putString("fields", "id,name,email,picture.type(large)")
//                            request.parameters = parameters
//                            request.executeAsync()
//                        }
//
//
//                        override fun onCancel() {
//                        }
//
//                        override fun onError(error: FacebookException) {
//                            Toast.makeText(this@Starter, "" + error, Toast.LENGTH_SHORT).show()
//                        }
//                    })
//
//
//            }
//        }

 //   }


    private fun handleSignInResul(completedTask: Task<GoogleSignInAccount>) {
        try {

            val account = completedTask.getResult(ApiException::class.java)
            val image = GlobalObj.storage.child("profilePhoto/${account.id.toString()}/profileph.jpg")

            uploadProfileImage(account.id.toString(),account.photoUrl.toString())

            UserInfo.saveProfileImage(this, image)

                val editor = sharedPreferences.edit()
                editor.putString("userId", account.id)
                editor.putString("userName", account.displayName)
                editor.putString("userEmail", account.email)
                editor.putString("imagePath", UserInfo.profile_ph)
                editor.apply()


                UserInfo.initializationUser(this@Starter){}

                    if (UserInfo.user_id == "" && UserInfo.name == "" && UserInfo.email == "") {
                        val user = hashMapOf(
                            "user_id" to account.id,
                            "name" to account.displayName,
                            "email" to account.email,
                        )
                        GlobalObj.db.collection("users").add(user)

                        UserInfo.initializationUser(this){}
                            val intent =
                                Intent(this, CreateHomeActivity::class.java)
                            startActivity(intent)

                    } else {
                        if (UserInfo.homes.isNotEmpty()) {
                            val intent =
                                Intent(this, BaseActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent =
                                Intent(this, CreateHomeActivity::class.java)
                            startActivity(intent)
                        }
                    }

        } catch (e: ApiException) {
            Toast.makeText(this,"signInResult:failed code=" + e.statusCode,Toast.LENGTH_SHORT).show()
        }

    }

    fun uploadProfileImage(userId: String, imageUrl: String) {
        Picasso.get()
            .load(imageUrl)
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    if (bitmap != null) {
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val imageData: ByteArray = baos.toByteArray()

                        val photoRef = GlobalObj.storage.child("profilePhoto/$userId/profileph.jpg")

                        photoRef.putBytes(imageData)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener {

                            }
                    }
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                }
            })
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
        }
    }
    companion object {
        const val RS_SIGN_IN = 1
    }
}