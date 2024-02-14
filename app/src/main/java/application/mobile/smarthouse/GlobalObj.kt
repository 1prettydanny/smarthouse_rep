package application.mobile.smarthouse

import android.annotation.SuppressLint
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

object GlobalObj {
   @SuppressLint("StaticFieldLeak")
   val db = Firebase.firestore
   val storage = Firebase.storage.reference
   val auth = Firebase.auth

}

