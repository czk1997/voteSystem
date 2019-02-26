package com.example.sch.votesystem

import android.Manifest.permission.READ_SMS
import android.Manifest.permission.RECEIVE_SMS
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Telephony
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val i = 0;
        // Permission Request
        if (checkSelfPermission(READ_SMS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(READ_SMS, RECEIVE_SMS), 1)
        }

        //Init the Activity
        val editIP = findViewById<EditText>(R.id.editIP)
        val editPort = findViewById<EditText>(R.id.editPort)
        val editPass = findViewById<EditText>(R.id.editPass)
        val logText = findViewById<TextView>(R.id.logText)
        val connectButton = findViewById<Button>(R.id.connctButton)
        val resultButton = findViewById<Button>(R.id.resultButton)
        var handler: Handler = Handler()
        var connector: Connector
        var thread: Thread = Thread()
        var connected = false
        //Set default IP and Port
        editIP.setText("10.215.196.121")
        editPort.setText("53217")

        val connectListener = View.OnClickListener {
            val severAddress = editIP.text.toString()
            val Port = Integer.parseInt(editPort.text.toString())
            if (!connected) {
                val l = Runnable {
                    try {
                        connector = Connector(severAddress, Port, handler)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                            intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
                            registerReceiver(connector, intentFilter)
                        }
                        val m = Message.obtain()
                        m.what = 1
                        m.obj = "Connecting"
                        handler.sendMessage(m)
                    } catch (e: IOException) {
                        val m = Message.obtain()
                        m.what = 1
                        m.obj = e.cause
                        handler.sendMessage(m)
                    }
                }
                thread = Thread(l)
                thread.start()
            }
        }

        connectButton.setOnClickListener(connectListener)
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == 1) {
                    //Normal Append Message
                    super.handleMessage(msg)
                    logText.append(msg.obj.toString() + "\n")
                } else if (msg.what == 2) {

                    connectButton.text = "Disconnect"
                }
                connectButton.setOnClickListener {
                    thread.interrupt()
                    connectButton.setOnClickListener(connectListener)
                    connectButton.text = "Connect"
                    connector = Connector()
                    connected = false
                }
                super.handleMessage(msg)
                logText.append(msg.obj.toString())
            }
        }
    }
}
