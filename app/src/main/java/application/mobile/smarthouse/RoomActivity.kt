package application.mobile.smarthouse

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import application.mobile.smarthouse.databinding.ActivityRoomBinding
import org.checkerframework.checker.units.qual.C
import java.util.Locale.Category

data class Device(val id: String, var name: String, var order: Int, val category: String){
    companion object {
        private val countersMap = mutableMapOf<String, Int>()

        fun getNextId(name: String): Int {
            val counter = countersMap.getOrDefault(name, 0) + 1
            countersMap[name] = counter
            return counter
        }
        fun getLastId(name: String): Int {
            val counter = countersMap.getOrDefault(name, 0) - 1
            countersMap[name] = counter
            return counter
        }

        fun countersClear(){
            countersMap.clear()
        }
    }
}

data class Category(val name: String, val icon: Int, val devices: List<Device>)
class RoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomBinding

    @SuppressLint("DiscouragedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        val roomId = intent.getStringExtra("room_id").toString()
        var roomImage = intent.getStringExtra("room_image").toString()
        var roomName = intent.getStringExtra("room_name").toString()
        val roomType = intent.getStringExtra("room_type").toString()


        binding.roomName.text = roomName
        binding.roomTypeIcon.setImageDrawable(ContextCompat.getDrawable(this, GlobalFun.getTypeIcon(roomType)))

        binding.roomHeaderImage.setImageDrawable(ContextCompat.getDrawable(this, GlobalFun.getImage(roomImage)))


        val categoryArr = mutableListOf<String>()
        val devicesArr = mutableListOf<Device>()

        GlobalObj.db.collection("devices")
            .whereEqualTo("room_id", roomId)
            .get()
            .addOnSuccessListener {documents ->
                for(document in documents){
                    val id = document["device_id"].toString()
                    val name = document["device_name"].toString()
                    val devCategory = document["device_category"].toString()
                    val order = document["device_order"].toString().toInt()

                    devicesArr.add(Device(id, name, order, devCategory))

                    if (!categoryArr.contains(devCategory)) {
                        categoryArr.add(devCategory)
                    }

                    val orderList = mutableListOf("Lighting", "Climate", "Energy", "Security", "Entertainment")

                    val sorted = categoryArr.sortedWith(compareBy({ it !in orderList }, { orderList.indexOf(it) }))

                    val categories = mutableListOf<application.mobile.smarthouse.Category>()
                        sorted.forEach {categoryName->
                            categories.add(
                                Category(
                                    categoryName,
                                    GlobalFun.getIconCategory(categoryName),
                                    devicesArr.filter { categoryName == it.category }
                                    )
                                )
                            }
                    val categoriesAdapter = CategoryAdapter(categories, this)
                    binding.categoryRv.adapter = categoriesAdapter
                }
            }


        binding.cancelBtn.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        binding.roomOtherBtn.setOnClickListener { view ->
            val popup = PopupMenu(this, view, Gravity.END)
            MenuInflater(this).inflate(R.menu.room_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {

                    R.id.changePicture -> {

                        var image: String
                        do {
                            image = GlobalFun.randomImage(roomType)
                        } while (image == roomImage)

                        GlobalFun.updateRoomImage(roomId, image){
                            roomImage = image
                            binding.roomHeaderImage.setImageDrawable(ContextCompat.getDrawable(this, GlobalFun.getImage(roomImage)))
                        }
                        true
                    }

                    R.id.renameRoom -> {
                        val text = ContextCompat.getString(this,R.string.dialog_rename_room_text)
                        GlobalFun.renameItem(this, roomName,text){ newName ->
                            if(newName != roomName){
                                GlobalFun.updateRoomName(roomId, newName){
                                    roomName = newName
                                    binding.roomName.text = roomName
                                }
                            }
                        }
                        true
                    }

                    R.id.deleteRoom -> {
                        val text = ContextCompat.getString(this, R.string.dialog_delete_room_text) + " \"$roomName\" ?"
                        GlobalFun.dialogDelete(this, text) {delete->
                            if (delete) {
                                GlobalFun.deleteDevices(roomId)
                                GlobalFun.deleteRoom(roomId) {
                                    onBackPressedCallback.handleOnBackPressed()
                                    finish()
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

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(this@RoomActivity, BaseActivity::class.java)
            val anim = ActivityOptions.makeCustomAnimation(this@RoomActivity, R.anim.slide_in_down, R.anim.stay)
            startActivity(intent, anim.toBundle())
        }
    }
}

class CategoryAdapter(private val categories: MutableList<application.mobile.smarthouse.Category>, private val listener: Context) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(category: application.mobile.smarthouse.Category) {
            val categoryName: TextView = itemView.findViewById(R.id.category_name)
            val devicesRecyclerView: RecyclerView = itemView.findViewById(R.id.devices_rv)
            val image: ImageView = itemView.findViewById((R.id.category_image))



            categoryName.text = category.name
            image.setImageDrawable(ContextCompat.getDrawable(itemView.context, GlobalFun.getIconCategory(category.name)))
            val deviceConnectionAdapter = DeviceAdapter(category.devices, listener)
            devicesRecyclerView.layoutManager = GridLayoutManager(itemView.context, 3)
            devicesRecyclerView.adapter = deviceConnectionAdapter
        }
    }
}
class DeviceAdapter(private val devices: List<Device>, private val listener: Context) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        private val deviceImageView: ImageView = itemView.findViewById(R.id.deviceImageView)
        fun bind(device: Device) {
            Device.getNextId(device.name)
            val fullName = device.name + " ${device.order}"
            deviceName.text =  fullName

            deviceImageView.setImageDrawable(ContextCompat.getDrawable(itemView.context, GlobalFun.getIconDevice(device.name)))

            itemView.setOnClickListener {
                val intent = Intent(listener, DeviceActivity::class.java)
                val anim = ActivityOptions.makeCustomAnimation(listener, R.anim.slide_in_right, R.anim.slide_out_left)
                listener.startActivity(intent, anim.toBundle())
            }

        }

    }


}

