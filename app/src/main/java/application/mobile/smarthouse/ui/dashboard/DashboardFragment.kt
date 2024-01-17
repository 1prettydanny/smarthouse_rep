package application.mobile.smarthouse.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import application.mobile.smarthouse.R
import application.mobile.smarthouse.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.textDashboard.text = "132123123"

        val title = activity?.findViewById<TextView>(R.id.toolbar_title)
        title?.text = getString(R.string.devices)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}