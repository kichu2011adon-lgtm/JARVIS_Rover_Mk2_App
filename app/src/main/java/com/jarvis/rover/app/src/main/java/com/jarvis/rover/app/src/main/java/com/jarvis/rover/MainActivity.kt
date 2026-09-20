package com.jarvis.rover

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import android.graphics.Color
import android.view.Gravity
import java.util.UUID

class MainActivity : android.app.Activity() {
    private val service = UUID.fromString("7e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val writeUuid = UUID.fromString("7e400002-b5a3-f393-e0a9-e50e24dcca9e")
    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null
    private lateinit var status: TextView
    private val accent = Color.rgb(41,199,242)

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        buildUi()
        if (android.os.Build.VERSION.SDK_INT >= 31 &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), 10)
    }

    private fun buildUi() {
        val root=LinearLayout(this); root.orientation=LinearLayout.VERTICAL; root.setPadding(24,28,24,20); root.setBackgroundColor(Color.rgb(5,8,12))
        fun tv(s:String,size:Float):TextView { return TextView(this).apply{ text=s; textSize=size; setTextColor(Color.WHITE); setPadding(0,8,0,8)}}
        val title=tv("J.A.R.V.I.S. ROVER MK2",22f); root.addView(title)
        root.addView(tv("PHONE CONTROLLER • ESP32 BLE",11f))
        status=tv("DISCONNECTED",14f); root.addView(status)
        val connect=Button(this); connect.text="CONNECT ESP32"; connect.setOnClickListener{scanAndConnect()}; root.addView(connect)
        val modes=LinearLayout(this); modes.orientation=LinearLayout.HORIZONTAL
        listOf("MANUAL","AUTO","LINE").forEach{ m-> val x=Button(this); x.text=m; x.setOnClickListener{send(m)}; modes.addView(x,LinearLayout.LayoutParams(0,60,1f))}; root.addView(modes)
        val pad=GridLayout(this); pad.columnCount=3
        fun drive(label:String,cmd:String){val x=Button(this);x.text=label;x.textSize=22f;x.setOnClickListener{send(cmd)};pad.addView(x,GridLayout.LayoutParams().apply{width=0;height=130;columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f)})}
        drive("▲","F"); drive("STOP","X"); drive("▲","B"); drive("◀","L"); drive("■","STOP"); drive("▶","R")
        root.addView(pad)
        val horn=Button(this); horn.text="HORN"; horn.setOnClickListener{send("HORN")}; root.addView(horn)
        val emergency=Button(this); emergency.text="EMERGENCY STOP"; emergency.setOnClickListener{send("STOP")}; root.addView(emergency)
        val speed=SeekBar(this); speed.max=100; speed.progress=60; speed.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){if(u)send("SPEED:$p")}
            override fun onStartTrackingTouch(s:SeekBar?){}; override fun onStopTrackingTouch(s:SeekBar?){}
        }); root.addView(tv("SPEED",12f)); root.addView(speed)
        root.addView(tv("BLE service ready for the ESP32 JARVIS Rover firmware.",12f))
        setContentView(root)
    }

    private fun scanAndConnect(){
        if (android.os.Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED){requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT),10);return}
        val adapter=(getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
        if(adapter==null||!adapter.isEnabled){status.text="TURN ON BLUETOOTH";return}
        status.text="SCANNING FOR JARVIS ROVER..."
        adapter.bluetoothLeScanner.startScan(object:ScanCallback(){
            override fun onScanResult(type:Int,r:ScanResult){ if(r.scanRecord?.serviceUuids?.any{it.uuid==service}==true){adapter.bluetoothLeScanner.stopScan(this);connect(r.device)} }
        })
    }
    private fun connect(d:BluetoothDevice){
        status.text="CONNECTING..."
        gatt=d.connectGatt(this,false,object:BluetoothGattCallback(){
            override fun onConnectionStateChange(g:BluetoothGatt,s:Int,n:Int){runOnUiThread{status.text=if(n==BluetoothProfile.STATE_CONNECTED)"CONNECTED" else "DISCONNECTED"};if(n==BluetoothProfile.STATE_CONNECTED)g.discoverServices()}
            override fun onServicesDiscovered(g:BluetoothGatt,s:Int){writeChar=g.getService(service)?.getCharacteristic(writeUuid)}
        })
    }
    private fun send(cmd:String){
        val c=writeChar?:run{status.text="CONNECT ESP32 FIRST";return}
        c.writeType=BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        c.value=(cmd+"\n").toByteArray()
        gatt?.writeCharacteristic(c)
    }
}
