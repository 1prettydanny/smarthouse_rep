package application.mobile.smarthouse.ui.Profile

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import application.mobile.smarthouse.GlobalObj
import application.mobile.smarthouse.Starter
import application.mobile.smarthouse.UserInfo
import application.mobile.smarthouse.databinding.FragmentProfileBinding
import com.squareup.picasso.Picasso
import java.io.File


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

   private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {


        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        sharedPreferences = requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)

        binding.profileName.text = UserInfo.name

        if(UserInfo.profile_ph == "") {

            val image = GlobalObj.storage.child("profilePhoto/${UserInfo.user_id}/profileph.jpg")

            UserInfo.saveProfileImage(requireContext(),image)

            image.downloadUrl.addOnSuccessListener { uri ->
               Picasso.get().load(uri).into(binding.profileImage)
            }.addOnFailureListener {}


        }
        else
        {
            Picasso.get().load(File(UserInfo.profile_ph)).into(binding.profileImage)
        }

        binding.singOutBtn.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            GlobalObj.auth.signOut()
            UserInfo.clearInfo()
            val intent = Intent(requireContext(), Starter::class.java)
            startActivity(intent)
        }

        val root: View = binding.root
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}