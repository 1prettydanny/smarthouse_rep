package application.mobile.smarthouse.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import application.mobile.smarthouse.CreateRoomActivity
import application.mobile.smarthouse.R
import application.mobile.smarthouse.Starter
import application.mobile.smarthouse.UserInfo
import application.mobile.smarthouse.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)

        val addRoomItem = menu.findItem(R.id.addRoom)
        val editRoomItem = menu.findItem(R.id.editRoom)

        addRoomItem?.setOnMenuItemClickListener {
            val intent = Intent(requireContext(), CreateRoomActivity::class.java)
            intent.putExtra("home_id",UserInfo.homes.first())
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }

        editRoomItem?.setOnMenuItemClickListener {
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}