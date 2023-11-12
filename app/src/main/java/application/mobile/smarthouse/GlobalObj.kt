package application.mobile.smarthouse

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

object GlobalObj {
   val db = Firebase.firestore
   val storage = Firebase.storage.reference
}

