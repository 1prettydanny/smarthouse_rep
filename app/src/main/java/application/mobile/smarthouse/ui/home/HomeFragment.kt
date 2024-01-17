package application.mobile.smarthouse.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import application.mobile.smarthouse.BaseActivity
import application.mobile.smarthouse.CreateRoomActivity
import application.mobile.smarthouse.GlobalObj
import application.mobile.smarthouse.R
import application.mobile.smarthouse.Starter
import application.mobile.smarthouse.UserInfo
import application.mobile.smarthouse.databinding.FragmentHomeBinding

class HomeFragment : Fragment(), MenuProvider {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        BaseActivity.setHomeTitle()

        activity?.addMenuProvider(this@HomeFragment,viewLifecycleOwner, Lifecycle.State.RESUMED)

        return root
    }




    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.home_menu, menu)

    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.addRoom -> {
                val intent = Intent(requireContext(), CreateRoomActivity::class.java)
                intent.putExtra("home_id", UserInfo.homes.first())
                startActivity(intent)
                true
            }

            R.id.editRoom -> {
                true
            }

            else -> false
        }

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
    companion object{
        var home_name: String? = null
    }
}
