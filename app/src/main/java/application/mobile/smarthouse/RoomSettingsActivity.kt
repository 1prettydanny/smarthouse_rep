package application.mobile.smarthouse

import android.app.Activity
import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.os.Bundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import application.mobile.smarthouse.databinding.ActivityRoomSettingsBinding
import java.util.UUID

data class Category(val name: String, val icon: Int, val devices: MutableList<ConnectDevice> = mutableListOf())
data class ConnectDevice(var name: String, var icon: Int, val category: String)
class RoomSettingsActivity : AppCompatActivity(),DeviceConnection {

    private lateinit var binding: ActivityRoomSettingsBinding
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var roomDevicesRV: RecyclerView
    private lateinit var roomId: String
    private lateinit var roomName: String
    private lateinit var roomType: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomSettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)


        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        Device.countersClear()
        roomType = intent.getStringExtra("room_type").toString()
        val roomPicture = intent.getStringExtra("room_image").toString()
        roomId = intent.getStringExtra("room_id").toString()
        roomName = intent.getStringExtra("room_name").toString()

      binding.headerImage.setImageDrawable(ContextCompat.getDrawable(this@RoomSettingsActivity, GlobalFun.getImage(roomPicture)))

        with(binding) {

            roomTitle.text = roomName
            intent.getStringExtra("room_name")
            val roomTypeIcon = GlobalFun.getTypeIcon(roomType)
            roomImage.setImageDrawable(ContextCompat.getDrawable(this@RoomSettingsActivity, roomTypeIcon))
                var typeCategory: Category? = null
                when(roomType){
                    "Kitchen" ->{
                    typeCategory = Category(roomType, roomTypeIcon, createDevList(roomType))
                }
                    "Bathroom" ->{
                    typeCategory = Category(roomType, roomTypeIcon, createDevList(roomType))
                }
            }

            val categories = mutableListOf(
                Category("Lighting", R.drawable.category_sun_ico, createDevList("Lighting")),
                Category("Climate", R.drawable.category_temp_ico, createDevList("Climate")),
                Category("Energy", R.drawable.category_lightning_ico, createDevList("Energy")),
                Category("Security", R.drawable.category_security_ico, createDevList("Security")),
                Category("Entertainment",R.drawable.category_tv_ico, createDevList("Entertainment")),
                Category("Other", R.drawable.category_other_ico, createDevList("Other"))
            )

            if (typeCategory!=null){
                categories.add(0,typeCategory)
            }


            val categoriesAdapter = CategoryAdapter(categories, this@RoomSettingsActivity)
            categoriesRecyclerView.adapter = categoriesAdapter

            roomContainer.setOnDragListener(dragListener)

            roomDevicesRV = roomDevicesRecyclerView
            val roomDevices = mutableListOf<Device>()

            GlobalObj.db.collection("devices")
                .whereEqualTo("room_id", roomId)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val id = document["device_id"].toString()
                        val name = document["device_name"].toString()
                        val category = document["device_category"].toString()
                        roomDevices.add(Device(id,name + " ${Device.getNextId(name)}", category))
                    }
                    roomAdapter = RoomAdapter(roomDevices, this@RoomSettingsActivity)
                    roomDevicesRV.layoutManager = GridLayoutManager(this@RoomSettingsActivity, 4)
                    roomDevicesRV.adapter = roomAdapter
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@RoomSettingsActivity, "err: " + exception.message, Toast.LENGTH_SHORT)
                        .show()
                }


            backBtn.setOnClickListener {
                onBackPressedCallback.handleOnBackPressed()
            }

            cancelBtn.setOnClickListener {
                GlobalFun.deleteRoom(roomId) {
                        val intent = Intent(this@RoomSettingsActivity, BaseActivity::class.java)
                        val animator = ActivityOptions.makeCustomAnimation(this@RoomSettingsActivity,
                            R.anim.slide_in_left, R.anim.stay).toBundle()
                        startActivity(intent,animator)
                        finish()
                    }
            }

            roomCollapseButton.setOnClickListener {

                //val uniqueDeviceNames = HashSet<String>()

                roomAdapter.roomDevices.forEach {
                        val device = hashMapOf(
                            "device_id" to it.id,
                            "room_id" to roomId,
                            "device_name" to it.name.substringBeforeLast(" "),
                            "device_category" to it.category
                        )
                        GlobalObj.db.collection("devices").add(device)
                }

                val intent = Intent(this@RoomSettingsActivity, BaseActivity::class.java)
                startActivity(intent)
            }

        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }



    private fun createDevList(name: String): MutableList<ConnectDevice> {
        val devicesList = mutableListOf<ConnectDevice>()
        when(name){
            "Lighting" -> {
                devicesList.addAll(mutableListOf(
                    ConnectDevice("Desktop lamp", R.drawable.new_lamp_desk_ico, name),
                    ConnectDevice("Chandelier",R.drawable.new_chandelier_ico, name),
                    ConnectDevice("Bulb",R.drawable.new_bulb_ico, name),
                    ConnectDevice("Spotlight", R.drawable.new_spotlight_ico, name)
                    )
                )
            }
            "Climate" ->{
                devicesList.addAll(mutableListOf(
                    ConnectDevice("Radiator",R.drawable.new_radiator_ico, name),
                    ConnectDevice("Cooling system", R.drawable.new_conditioner_ico, name),
                    ConnectDevice("Heating floor", R.drawable.new_heating_floor_ico, name),
                    ConnectDevice("Ceiling fan",R.drawable.new_ceiling_fan_ico, name),
                    ConnectDevice("Desktop fan",R.drawable.new_desk_fan_ico, name),
                    ConnectDevice("Thermo regulator",R.drawable.new_thermostat_ico, name),
                    ConnectDevice("Humidifier", R.drawable.new_humidifier_ico, name)
                    )
                )
            }
            "Energy" -> {
                devicesList.addAll(mutableListOf(
                    ConnectDevice("Switch",R.drawable.new_switch_ico, name),
                    ConnectDevice("Plug", R.drawable.new_plug_ico, name),
                    ConnectDevice("Socket", R.drawable.new_socket_ico, name),
                    )
                )
            }
            "entertainment" -> {
                devicesList.addAll(mutableListOf(
                    ConnectDevice("TV",R.drawable.new_tv_ico, name),
                    ConnectDevice("Radio", R.drawable.new_radio_ico, name),
                    ConnectDevice("Speaker", R.drawable.new_speaker_ico, name),
                    ConnectDevice("Projector",R.drawable.new_projector_ico, name),
                    )
                )
            }
            "security" -> {
                devicesList.addAll(mutableListOf(
                    ConnectDevice("Camera",R.drawable.new_videocamera_ico, name),
                    ConnectDevice("Lock", R.drawable.new_lock_ico, name),
                    )
                )
            }
            "Kitchen" ->{
                devicesList.addAll(mutableListOf(
                    ConnectDevice("Fridge",R.drawable.new_fridge_ico, name),
                    ConnectDevice("Coffee machine", R.drawable.new_coffee_machine_ico, name),
                    ConnectDevice("Stove",R.drawable.new_stove_ico, name),
                    )
                )
            }
            "Bathroom" ->{
                devicesList.addAll(mutableListOf(
                    ConnectDevice("Washing machine",R.drawable.new_washing_machine_ico, name),
                    ConnectDevice("Tap", R.drawable.new_tap_ico, name),
                    ConnectDevice("Shower", R.drawable.new_shower_ico, name)
                    )
                )
            }
            "other" -> {
                devicesList.addAll(mutableListOf(
                    ConnectDevice("Wi-Fi",R.drawable.new_wi_fi_ico, name),
                    ConnectDevice("Moped", R.drawable.new_moped_ico, name),
                    ConnectDevice("Scooter", R.drawable.new_scooter_ico, name),
                    ConnectDevice("Printer", R.drawable.new_printer_ico, name)
                    )
                )
            }
        }
        return devicesList
    }

    private val dragListener = View.OnDragListener { view, event ->
        when(event.action){
            DragEvent.ACTION_DRAG_STARTED -> {
                event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                view.invalidate()
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> true
            DragEvent.ACTION_DRAG_EXITED -> {
                view.invalidate()
                true
            }
            DragEvent.ACTION_DROP ->{
                val name = event.clipData.getItemAt(0).toString()
                val category = event.clipData.getItemAt(1).toString()
                addDevice(name, category)
                true
            }
            DragEvent.ACTION_DRAG_ENDED ->{

                true
            }
            else -> false
        }
    }

//    private fun deviceWithIcon(devIcon: Int){
//        when(devIcon){
//            R.drawable.new_bulb_ico -> addDevice("Bulb", devIcon, )
//            R.drawable.new_chandelier_ico -> addDevice("Chandelier",devIcon)
//            R.drawable.new_lamp_desk_ico -> addDevice("Desktop lamp", devIcon)
//            R.drawable.new_spotlight_ico -> addDevice("Spotlight", devIcon)
//
//            R.drawable.new_radiator_ico -> addDevice("Radiator",devIcon)
//            R.drawable.new_conditioner_ico -> addDevice("Cooling system",devIcon)
//            R.drawable.new_heating_floor_ico -> addDevice("Heating floor",devIcon)
//            R.drawable.new_ceiling_fan_ico -> addDevice("Ceiling fan",devIcon)
//            R.drawable.new_desk_fan_ico -> addDevice("Desktop fan",devIcon)
//            R.drawable.new_thermostat_ico -> addDevice("Thermo regulator", devIcon)
//            R.drawable.new_humidifier_ico -> addDevice("Humidifier",devIcon)
//
//            R.drawable.new_switch_ico -> addDevice("Switch",devIcon)
//            R.drawable.new_plug_ico -> addDevice("Plug", devIcon)
//            R.drawable.new_socket_ico-> addDevice("Socket", devIcon)
//
//            R.drawable.new_tv_ico -> addDevice("TV",devIcon)
//            R.drawable.new_radio_ico -> addDevice("Radio", devIcon)
//            R.drawable.new_speaker_ico -> addDevice("Speaker", devIcon)
//            R.drawable.new_projector_ico -> addDevice("Projector",devIcon)
//
//            R.drawable.new_videocamera_ico -> addDevice("Camera",devIcon)
//            R.drawable.new_lock_ico -> addDevice("Lock",devIcon)
//
//            R.drawable.new_fridge_ico -> addDevice("Fridge",devIcon)
//            R.drawable.new_coffee_machine_ico -> addDevice("Coffee machine",devIcon)
//            R.drawable.new_stove_ico -> addDevice("Stove",devIcon)
//
//            R.drawable.new_washing_machine_ico -> addDevice("Washing machine",devIcon)
//            R.drawable.new_tap_ico -> addDevice("Tap", devIcon)
//            R.drawable.new_shower_ico -> addDevice("Shower", devIcon)
//
//            R.drawable.new_wi_fi_ico -> addDevice("Wi-Fi",devIcon)
//            R.drawable.new_moped_ico -> addDevice("Moped", devIcon)
//            R.drawable.new_scooter_ico -> addDevice("Scooter", devIcon)
//            R.drawable.new_printer_ico -> addDevice("Printer", devIcon)
//        }
//    }

    override fun addIndent() {
        if (roomAdapter.itemCount == 1){
            val button = findViewById<ImageButton>(R.id.room_collapse_button)
            button.visibility = View.GONE
        }
        val textView = findViewById<TextView>(R.id.drag_device_text)
        textView.visibility = View.VISIBLE
    }
    private fun connect() {
        val intent = Intent(this, ConnectionActivity::class.java)
        startActivity(intent)
    }

    override fun addDevice(name: String, category: String) {
       connect()
        if(roomAdapter.itemCount == 0){
            val textView = findViewById<TextView>(R.id.drag_device_text)
            textView.visibility = View.GONE
            val button = findViewById<ImageButton>(R.id.room_collapse_button)
            button.visibility = View.VISIBLE
        }
        val deviceId = UUID.randomUUID().toString()
        roomAdapter.addNewDeviceToRoom(Device(deviceId,name + " ${Device.getNextId(name)}", category), 0)
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(this@RoomSettingsActivity, CreateRoomActivity::class.java)

            val animationBundle = ActivityOptions.makeCustomAnimation(
                this@RoomSettingsActivity,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            ).toBundle()
            intent.putExtra("room_id", roomId)
            intent.putExtra("room_name", roomName)
            intent.putExtra("room_type", roomType)
            startActivity(intent, animationBundle)
        }
    }

}


class CategoryAdapter(private val categories: MutableList<Category>, private val listener: DeviceConnection) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

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

        fun bind(category: Category) {
            val categoryName: TextView = itemView.findViewById(R.id.category_title)
            val devicesRecyclerView: RecyclerView = itemView.findViewById(R.id.devices_recycler_view)
            val collapse: ImageButton = itemView.findViewById(R.id.collapse_button)
            val image: ImageView = itemView.findViewById((R.id.category_image))



            categoryName.text = category.name
            image.setImageDrawable(ContextCompat.getDrawable(itemView.context, category.icon))
            val deviceAdapter = DeviceAdapter(category.devices,listener)
            devicesRecyclerView.layoutManager = GridLayoutManager(itemView.context, 4)
            devicesRecyclerView.adapter = deviceAdapter


            collapse.setOnClickListener{
                if(devicesRecyclerView.visibility == View.GONE) {
                    devicesRecyclerView.visibility = View.VISIBLE
                    collapse.setImageDrawable(ContextCompat.getDrawable(itemView.context ,R.drawable.arrow_up))
                }
                else {
                    devicesRecyclerView.visibility = View.GONE
                    collapse.setImageDrawable(ContextCompat.getDrawable(itemView.context ,R.drawable.arrow_down))
                }
            }
            itemView.setOnClickListener{
                collapse.performClick()
            }
        }
        
    }

}

class RoomAdapter(val roomDevices: MutableList<Device>, val listener: DeviceConnection) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_room_device, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(roomDevices[position])
    }

    override fun getItemCount(): Int {
        return roomDevices.size
    }
    fun addNewDeviceToRoom(device: Device, position: Int) {
            roomDevices.add(position,device)
            notifyItemInserted(position)
    }
    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        private val deviceImageView: ImageView = itemView.findViewById(R.id.deviceImageView)
        fun bind(device: Device) {
            deviceName.text = device.name

            deviceImageView.setImageDrawable(ContextCompat.getDrawable(itemView.context, GlobalFun.iconDevice(device.name.substringBeforeLast(" "))))


            itemView.setOnClickListener{
                val text = ContextCompat.getString(itemView.context, R.string.dialog_delete_device_text) + " \"${device.name}\" ?"
                GlobalFun.dialogDelete(itemView.context as Activity, text) {delete->
                    if(delete) {
                        Device.getLastId(device.name)
                        if (roomDevices.size == 1) {
                            listener.addIndent()
                        } else {
                            roomDevices.subList(0, bindingAdapterPosition)
                                .forEachIndexed { index, dev ->
                                    if (dev.category == device.category) {
                                        dev.name = "${dev.name.substringBeforeLast(" ")} ${
                                            dev.name.substringAfterLast(" ").toInt() - 1
                                        }"
                                        notifyItemChanged(index)
                                    }
                                }
                        }

                        roomDevices.removeAt(bindingAdapterPosition)
                        notifyItemRemoved(bindingAdapterPosition)
                    }
                }

            }
        }

    }
}
class DeviceAdapter(private val devices: MutableList<ConnectDevice>, private val listener: DeviceConnection) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

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
        fun bind(device: ConnectDevice) {
            deviceName.text = device.name

         deviceImageView.setImageDrawable(ContextCompat.getDrawable(itemView.context, device.icon))

            itemView.setOnClickListener {
                listener.addDevice(device.name, device.category)
            }

            itemView.setOnLongClickListener {

                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                val item1 = ClipData.Item(device.name)
                val item2 = ClipData.Item(device.category)

                val data = ClipData(device.name, mimeTypes, item1)
                data.addItem(item2)

                val dragShadowBuilder = View.DragShadowBuilder(it)
                it.startDragAndDrop(data,dragShadowBuilder,it,0)
                true
            }

        }

    }


}

interface DeviceConnection {
    fun addDevice(name: String,category: String)
    fun addIndent()
}
