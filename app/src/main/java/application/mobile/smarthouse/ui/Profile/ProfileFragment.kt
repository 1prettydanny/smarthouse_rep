package application.mobile.smarthouse.ui.Profile

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import application.mobile.smarthouse.CreateHomeActivity
import application.mobile.smarthouse.GlobalFun
import application.mobile.smarthouse.GlobalObj
import application.mobile.smarthouse.R
import application.mobile.smarthouse.Starter
import application.mobile.smarthouse.UserInfo
import application.mobile.smarthouse.databinding.FragmentProfileBinding
import com.squareup.picasso.Picasso
import java.io.File


data class Home(val id: String, var name: String)
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.profileName.text = UserInfo.name


        val title = activity?.findViewById<TextView>(R.id.toolbar_title)
        title?.text = getString(R.string.profile)

        if (UserInfo.profile_ph == "") {

            val image = GlobalObj.storage.child("profilePhoto/${UserInfo.user_id}/profileph.jpg")

            UserInfo.saveProfileImage(requireContext(), image) {}
            image.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri).into(binding.profileImage)
            }

        } else {
            Picasso.get().load(File(UserInfo.profile_ph)).into(binding.profileImage)
        }

        binding.singOutBtn.setOnClickListener {
            GlobalObj.auth.signOut()
            UserInfo.clearInfo()
            val intent = Intent(requireContext(), Starter::class.java)
            startActivity(intent)
        }



        val arrHome = mutableListOf<Home>()

        GlobalObj.db.collection("homes")
            .whereEqualTo("user_id", UserInfo.user_id)
            .get()
            .addOnSuccessListener { res ->
                for(document in res){
                    val id = document["home_id"].toString()
                    if(id!=UserInfo.selected_home_id) {
                        val name = document["home_name"].toString()
                        arrHome.add(Home(id, name))
                    }
                }
                arrHome.add(0, Home(UserInfo.selected_home_id, UserInfo.selected_home_name))
                arrHome.add(Home("","Create new home"))
                val adapter = HomeAdapter(requireActivity(), arrHome)
                val homeSpinner = binding.homeSpinner
                homeSpinner.adapter = adapter

                binding.collapseButton.setOnClickListener {
                    homeSpinner.performClick()
                }

                homeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(
                        parentView: AdapterView<*>?, selectedItemView: View?,
                        position: Int, id: Long
                    ) {
                        when (position) {
                            in 1 until arrHome.size - 1 -> {
                                UserInfo.selected_home_id = arrHome[position].id
                                UserInfo.selected_home_name = arrHome[position].name
                                UserInfo.changeSelectedHome()

                                val selectedRoomType = arrHome.removeAt(position)
                                arrHome.add(0, selectedRoomType)
                                adapter.notifyDataSetChanged()
                                homeSpinner.setSelection(0)

                                val navController = requireActivity().findNavController(R.id.nav_host_fragment_activity_base)
                                navController.navigate(R.id.navigation_home)
                            }
                            arrHome.size - 1 -> {
                                val intent = Intent(context, CreateHomeActivity::class.java)
                                val anim = ActivityOptions.makeCustomAnimation(
                                    context,
                                    R.anim.slide_in_down,
                                    R.anim.stay
                                )
                                startActivity(intent, anim.toBundle())
                            }
                        }

                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {
                    }
                }


            }


        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class HomeAdapter(context: Context, arrHome: MutableList<Home>) :
        ArrayAdapter<Home>(context, 0, arrHome) {

            val mContext = context
            val mArrHome = arrHome
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutInflater.from(mContext).inflate(R.layout.spinner_item_home, parent, false)
            val homeLayout = view.findViewById<LinearLayout>(R.id.home_layout)
            val textView = view.findViewById<TextView>(R.id.home_name)
            val button = view.findViewById<ImageView>(R.id.other_btn)

            val home = getItem(position) as Home

            textView.text = home.name

            if(position == count - 1){
                button.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.add_device_ico))
            }
            else {
                button.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.other_ico))

                button.setOnClickListener { view ->

                    val popup = PopupMenu(view.context, view, Gravity.END)
                    MenuInflater(view.context).inflate(R.menu.home_menu, popup.menu)
                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {

                            R.id.renameHome -> {
                                val text = ContextCompat.getString(
                                    mContext,
                                    R.string.dialog_rename_home_text
                                )
                                GlobalFun.renameItem(
                                    mContext as Activity,
                                    home.name,
                                    text
                                ) { newName ->
                                    if (newName != home.name) {
                                        GlobalFun.updateHomeName(home.id, newName) {
                                            home.name = newName
                                            notifyDataSetChanged()
                                            if(position==0)
                                                UserInfo.selected_home_name = home.name
                                        }
                                    }
                                }
                                true
                            }

                            R.id.deleteHome -> {
                                val text = ContextCompat.getString(
                                    mContext,
                                    R.string.dialog_delete_room_text
                                ) + " \"${home.name}\" ?"
                                GlobalFun.dialogDelete(mContext as Activity, text) { delete ->
                                    if (delete) {
                                        GlobalObj.db.collection("rooms")
                                            .whereEqualTo("home_id", home.id)
                                            .get()
                                            .addOnSuccessListener {documents ->
                                                for(document in documents) {
                                                    val id = document["room_id"].toString()
                                                    GlobalFun.deleteDevices(id)
                                                    GlobalFun.deleteRoom(id){}
                                                }
                                                GlobalFun.deleteHome(home.id){
                                                    mArrHome.remove(home)
                                                    notifyDataSetChanged()

                                                    if(count == 1){
                                                        UserInfo.selected_home_id = ""
                                                        UserInfo.selected_home_name = ""

                                                        UserInfo.changeSelectedHome()
                                                        val intent = Intent(mContext, CreateHomeActivity::class.java)
                                                            val anim = ActivityOptions.makeCustomAnimation(
                                                                    mContext,
                                                                    R.anim.slide_in_down,
                                                                    R.anim.stay,
                                                                )
                                                            mContext.startActivity(intent, anim.toBundle())

                                                    }
                                                    else{
                                                        if(mArrHome[0].id != UserInfo.selected_home_id) {
                                                            UserInfo.selected_home_id = mArrHome[0].id
                                                            UserInfo.selected_home_name = mArrHome[0].name
                                                            UserInfo.changeSelectedHome()
                                                            val navController = mContext.findNavController(R.id.nav_host_fragment_activity_base)
                                                            navController.navigate(R.id.navigation_home)
                                                        }
                                                    }
                                                }
                                            }

                                    }
                                }
                                true
                            }

                            else -> false
                        }
                    }

                    try {
                        val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                        fieldMPopup.isAccessible = true
                        val mPopup = fieldMPopup.get(popup)
                        mPopup.javaClass
                            .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                            .invoke(mPopup, true)
                    } catch (_: Exception) {

                    } finally {
                        popup.show()
                    }

                }
            }

                if (position == 0) {
                    homeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.select_green))
                } else {
                    homeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                }



            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = getView(position , convertView, parent)

            if(position == 0) {
                val roomTypeLayout = view.findViewById<LinearLayout>(R.id.home_layout)
                roomTypeLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.select_green
                    )
                )
            }
            return view
        }
    }
}
