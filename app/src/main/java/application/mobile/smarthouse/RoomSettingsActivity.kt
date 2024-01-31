package application.mobile.smarthouse

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import application.mobile.smarthouse.databinding.ActivityRoomSettingsBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class Category(val name: String, val icon: Int, val devices: MutableList<Device> = mutableListOf())
data class Device(var name: String, var icon: Int){
    companion object {
        private val countersMap = mutableMapOf<Int, Int>()

        fun getNextId(icon: Int): Int {
            val counter = countersMap.getOrDefault(icon, 0) + 1
            countersMap[icon] = counter
            return counter
        }
        fun getLastId(icon: Int): Int {
            val counter = countersMap.getOrDefault(icon, 0) - 1
            countersMap[icon] = counter
            return counter
        }

        fun countersClear(){
            countersMap.clear()
        }
    }
}

class RoomSettingsActivity : AppCompatActivity(),DeviceConnection {

    private lateinit var binding: ActivityRoomSettingsBinding
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var roomDevicesRV: RecyclerView
    private lateinit var room_id: String
    private lateinit var room_name: String
    private var room_type: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomSettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)


        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        Device.countersClear()
        room_type = intent.getIntExtra("room_type",-1)
        val room_picture = intent.getIntExtra("room_image", -1)
        room_id = intent.getStringExtra("room_id").toString()
        room_name = intent.getStringExtra("room_name").toString()

        Glide.with(this)
            .load(room_picture)
            .into(binding.headerImage)

        with(binding) {

            roomTitle.text = room_name
            intent.getStringExtra("room_name")
            var type_categorie: Category? = null
                when(room_type){
                    R.drawable.room_type_classic_ico ->{roomImage.setImageResource(room_type)}

                    R.drawable.room_type_kitchen_ico ->{
                    type_categorie = Category("Kitchen", room_type, createDevList("kitchen"))
                    roomImage.setImageResource(room_type)
                }
                    R.drawable.room_type_bathroom_ico ->{
                    type_categorie = Category("Bathroom", room_type, createDevList("bathroom"))
                    roomImage.setImageResource(room_type)
                }
            }

            val categories = mutableListOf(
                Category("Lighting", R.drawable.category_sun_ico, createDevList("light")),
                Category("Climate", R.drawable.category_temp_ico,createDevList("climate")),
                Category("Energy", R.drawable.category_lightning_ico,createDevList("energy")),
                Category("Security", R.drawable.category_security_ico,createDevList("security")),
                Category("Entertainment",R.drawable.category_tv_ico,createDevList("entertainment")),
                Category("Other", R.drawable.category_other_ico,createDevList("other"))
            )

            if (type_categorie!=null){
                categories.add(0,type_categorie)
            }


            val categoriesAdapter = CategoryAdapter(categories, this@RoomSettingsActivity)
            categoriesRecyclerView.adapter = categoriesAdapter

            roomContainer.setOnDragListener(dragListener)

            roomDevicesRV = roomDevicesRecyclerView
            val roomDevices = mutableListOf<Device>()
            GlobalObj.db.collection("devices")
                .whereEqualTo("room_id", room_id)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        roomDevices.add(Device(document["device_name"].toString(), document["device_icon"].toString().toInt()))
                    }
                    roomAdapter = RoomAdapter(roomDevices, this@RoomSettingsActivity)
                    roomDevicesRV.layoutManager = GridLayoutManager(this@RoomSettingsActivity, 4)
                    roomDevicesRV.adapter = roomAdapter
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@RoomSettingsActivity, "err: " + exception.message, Toast.LENGTH_SHORT)
                        .show()
                }




            roomCollapseButton.setOnClickListener {



                //val uniqueDeviceNames = HashSet<String>()

                roomAdapter.roomDevices.forEach {
                    val device_name = it.name.substringBeforeLast(" ")
                  //  val count = it.name.substringAfterLast(" ").toInt()

               //     if (uniqueDeviceNames.add(device_name)) {
                        val device_id = UUID.randomUUID().toString()
                        val device = hashMapOf(
                            "device_id" to device_id,
                            "room_id" to room_id,
                            "device_name" to device_name,
                            "device_icon" to it.icon
                           // "count" to count
                        )
                        GlobalObj.db.collection("devices").add(device)
               //     }
                }

                val intent = Intent(this@RoomSettingsActivity, BaseActivity::class.java)
                startActivity(intent)
            }


//            roomHeader.setOnClickListener{
//                if(roomAdapter.itemCount != 0) {
//                    if (roomDevicesRV.visibility == View.VISIBLE) {
//                        roomDevicesRV.visibility = View.GONE
//                        roomCollapseButton.setImageResource(R.drawable.arrow_down)
//                    } else {
//                        roomDevicesRV.visibility = View.VISIBLE
//                        roomCollapseButton.setImageResource(R.drawable.arrow_up)
//                    }
//                }
//                roomCollapseButton.performClick()
//            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }



    private fun createDevList(name: String): MutableList<Device> {
        val devicesList = mutableListOf<Device>()
        when(name){
            "light" -> {
                devicesList.addAll(mutableListOf(
                    Device("Desktop lamp", R.drawable.new_lamp_desk_ico),
                    Device("Chandelier",R.drawable.new_chandelier_ico),
                    Device("Bulb",R.drawable.new_bulb_ico),
                    Device("Spotlight", R.drawable.new_spotlight_ico)
                    )
                )
            }
            "climate" ->{
                devicesList.addAll(mutableListOf(
                    Device("Radiator",R.drawable.new_radiator_ico),
                    Device("Cooling system", R.drawable.new_conditioner_ico),
                    Device("Heating floor", R.drawable.new_heating_floor_ico),
                    Device("Ceiling fan",R.drawable.new_ceiling_fan_ico),
                    Device("Desktop fan",R.drawable.new_desk_fan_ico),
                    Device("Thermo regulator",R.drawable.new_thermostat_ico),
                    Device("Humidifier", R.drawable.new_humidifier_ico)
                    )
                )
            }
            "energy" -> {
                devicesList.addAll(mutableListOf(
                    Device("Switch",R.drawable.new_switch_ico),
                    Device("Plug", R.drawable.new_plug_ico),
                    Device("Socket", R.drawable.new_socket_ico),
                    )
                )
            }
            "entertainment" -> {
                devicesList.addAll(mutableListOf(
                    Device("TV",R.drawable.new_tv_ico),
                    Device("Radio", R.drawable.new_radio_ico),
                    Device("Speaker", R.drawable.new_speaker_ico),
                    Device("Projector",R.drawable.new_projector_ico),
                    )
                )
            }
            "security" -> {
                devicesList.addAll(mutableListOf(
                    Device("Camera",R.drawable.new_videocamera_ico),
                    Device("Lock", R.drawable.new_lock_ico),
                    )
                )
            }
            "kitchen" ->{
                devicesList.addAll(mutableListOf(
                    Device("Fridge",R.drawable.new_fridge_ico),
                    Device("Coffee machine", R.drawable.new_coffee_machine_ico),
                    Device("Stove",R.drawable.new_stove_ico),
                    )
                )
            }
            "bathroom" ->{
                devicesList.addAll(mutableListOf(
                    Device("Washing machine",R.drawable.new_washing_machine_ico),
                    Device("Tap", R.drawable.new_tap_ico),
                    Device("Shower", R.drawable.new_shower_ico)
                    )
                )
            }
            "other" -> {
                devicesList.addAll(mutableListOf(
                    Device("Wi-Fi",R.drawable.new_wi_fi_ico),
                    Device("Moped", R.drawable.new_moped_ico),
                    Device("Scooter", R.drawable.new_scooter_ico),
                    Device("Printer", R.drawable.new_printer_ico)
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
                val item = event.clipData.getItemAt(0)
                deviceWithIcon(item.text.toString().toInt())
                true
            }
            DragEvent.ACTION_DRAG_ENDED ->{

                true
            }
            else -> false
        }
    }

    private fun deviceWithIcon(devIcon: Int){
        when(devIcon){
            R.drawable.new_bulb_ico -> addDevice("Bulb" ,devIcon)
            R.drawable.new_chandelier_ico -> addDevice("Chandelier",devIcon)
            R.drawable.new_lamp_desk_ico -> addDevice("Desktop lamp", devIcon)
            R.drawable.new_spotlight_ico -> addDevice("Spotlight", devIcon)

            R.drawable.new_radiator_ico -> addDevice("Radiator",devIcon)
            R.drawable.new_conditioner_ico -> addDevice("Cooling system",devIcon)
            R.drawable.new_heating_floor_ico -> addDevice("Heating floor",devIcon)
            R.drawable.new_ceiling_fan_ico -> addDevice("Ceiling fan",devIcon)
            R.drawable.new_desk_fan_ico -> addDevice("Desktop fan",devIcon)
            R.drawable.new_thermostat_ico -> addDevice("Thermo regulator", devIcon)
            R.drawable.new_humidifier_ico -> addDevice("Humidifier",devIcon)

            R.drawable.new_switch_ico -> addDevice("Switch",devIcon)
            R.drawable.new_plug_ico -> addDevice("Plug", devIcon)
            R.drawable.new_socket_ico-> addDevice("Socket", devIcon)

            R.drawable.new_tv_ico -> addDevice("TV",devIcon)
            R.drawable.new_radio_ico -> addDevice("Radio", devIcon)
            R.drawable.new_speaker_ico -> addDevice("Speaker", devIcon)
            R.drawable.new_projector_ico -> Device("Projector",devIcon)

            R.drawable.new_videocamera_ico -> addDevice("Camera",devIcon)
            R.drawable.new_lock_ico -> addDevice("Lock",devIcon)

            R.drawable.new_fridge_ico -> addDevice("Fridge",devIcon)
            R.drawable.new_coffee_machine_ico -> addDevice("Coffee machine",devIcon)
            R.drawable.new_stove_ico -> addDevice("Stove",devIcon)

            R.drawable.new_washing_machine_ico -> addDevice("Washing machine",devIcon)
            R.drawable.new_tap_ico -> addDevice("Tap", devIcon)
            R.drawable.new_shower_ico -> addDevice("Shower", devIcon)

            R.drawable.new_wi_fi_ico -> addDevice("Wi-Fi",devIcon)
            R.drawable.new_moped_ico -> addDevice("Moped", devIcon)
            R.drawable.new_scooter_ico -> addDevice("Scooter", devIcon)
            R.drawable.new_printer_ico -> addDevice("Printer", devIcon)
        }
    }

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

    override fun addDevice(name: String,icon: Int) {
       // connect()
        if(roomAdapter.itemCount == 0){
            val textView = findViewById<TextView>(R.id.drag_device_text)
            textView.visibility = View.GONE
            val button = findViewById<ImageButton>(R.id.room_collapse_button)
            button.visibility = View.VISIBLE
        }

        roomAdapter.addNewDeviceToRoom(Device(name + " ${Device.getNextId(icon)}", icon), 0)

    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(this@RoomSettingsActivity, CreateRoomActivity::class.java)
            intent.putExtra("room_id", room_id)
            intent.putExtra("room_name", room_name)
            intent.putExtra("room_type", room_type)
            startActivity(intent)
        }
    }

}


class CategoryAdapter(private val categories: MutableList<Category>, private val listener: DeviceConnection) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
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
            image.setImageResource(category.icon)
            val deviceAdapter = DeviceAdapter(category.devices,listener)
            devicesRecyclerView.layoutManager = GridLayoutManager(itemView.context, 4)
            devicesRecyclerView.adapter = deviceAdapter


            collapse.setOnClickListener{
                if(devicesRecyclerView.visibility == View.GONE) {
                    devicesRecyclerView.visibility = View.VISIBLE
                    collapse.setImageResource(R.drawable.arrow_up)
                }
                else {
                    devicesRecyclerView.visibility = View.GONE
                    collapse.setImageResource(R.drawable.arrow_down)
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
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

            Glide.with(itemView.context)
                .load(device.icon)
                .into(deviceImageView)


            itemView.setOnLongClickListener{

                Device.getLastId(device.icon)
                if(roomDevices.size == 1) {
                    listener.addIndent()
                }
                else
                {
                    roomDevices.subList(0,bindingAdapterPosition).forEachIndexed{index,dev->
                        if(dev.icon == device.icon){
                            dev.name= "${dev.name.substringBeforeLast(" ")} ${dev.name.substringAfterLast(" ").toInt() - 1}"
                            notifyItemChanged(index)
                        }
                    }
                }

                roomDevices.removeAt(bindingAdapterPosition)
                notifyItemRemoved(bindingAdapterPosition)

                true
            }
        }

    }
}
class DeviceAdapter(private val devices: MutableList<Device>, private val listener: DeviceConnection) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
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
            deviceName.text = device.name

            Glide.with(itemView.context)
                .load(device.icon)
                .into(deviceImageView)

            itemView.setOnClickListener {
                listener.addDevice(device.name, device.icon)
            }

            itemView.setOnLongClickListener {
                val cliptext = device.icon.toString()
                val item = ClipData.Item(cliptext)
                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                val data = ClipData(cliptext,mimeTypes, item)

                // addNewDev(getDevice(device.icon))
                val dragShadowBuilder = View.DragShadowBuilder(it)
                it.startDragAndDrop(data,dragShadowBuilder,it,0)
                true
            }

        }

    }


}


interface DeviceConnection {
    fun addDevice(name: String,icon: Int)
    fun addIndent()
}
