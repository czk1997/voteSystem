package com.example.sch.votesystem

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import java.lang.Exception
import java.net.Socket
import java.security.Key
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

class Connector : BroadcastReceiver {
    private lateinit var serverAddress: String
    private val SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED"
    private var port = 0
    lateinit var socket: Socket
    private var msgDecoder: MsgDecoder = MsgDecoder()
    private var msgEncoder: MsgEncoder = MsgEncoder()
    var phoneList = arrayListOf<Long>()
    var pollResult = hashMapOf<String, Int>()
    private var readyQueue: Queue<KeyValueList> = LinkedList<KeyValueList>()
    lateinit var uiHandler: Handler
    var passcode = "123456"
    var securityLevel = 1;

    constructor() {}

    constructor(serverAddress: String, port: Int, handler: Handler) {
        this.serverAddress = serverAddress
        this.port = port
        try {
            socket = Socket(serverAddress, port)
            msgDecoder = MsgDecoder(socket.getInputStream())
            msgEncoder = MsgEncoder(socket.getOutputStream())
            this.uiHandler = handler
            val msg: Message = Message.obtain()
            msg.what = 2
            msg.obj = "Connect to server ON $serverAddress : $port"
            handler.sendMessage(msg)
            sendInitMessage()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun sendInitMessage() {
        var k = KeyValueList()
        // Send Register Message
        k.putPair("Scope", "VotingSystem")
        k.putPair("MessageType", "Register")
        k.putPair("Role", "Basic")
        k.putPair("Name", "VotingSystem")
        msgEncoder.sendMsg(k)
        k = KeyValueList()
        k.putPair("Scope", "VotingSystem")
        k.putPair("MessageType", "Connect")
        k.putPair("Role", "Basic")
        k.putPair("Name", "VotingSystem")
        msgEncoder.sendMsg(k)
        k = KeyValueList()
        k.putPair("Scope", "VotingSystem")
        k.putPair("MsgID", "21")
        k.putPair("MessageType", "Setting")
        k.putPair("Passcode", "123456")
        k.putPair("SecurityLevel", "3")
        k.putPair("Name", "VotingSystem")
        k.putPair("Receiver", "VotingSystem")
        k.putPair("InputMsgID 1", "701")
        k.putPair("InputMsgID 2", "702")
        k.putPair("InputMsgID 3", "703")
        k.putPair("OutputMsgID 1", "711")
        k.putPair("OutputMsgID 2", "712")
        k.putPair("OutputMsgID 3", "726")
        msgEncoder.sendMsg(k)
        //Get Msg From SIS
        val runnable: Runnable = Runnable {
            while (true) {
                try {
                    analyzeMsg(msgDecoder.msg)
                } catch (e: Exception) {
                    Thread.sleep(1000)
                }
            }
        }
        Thread(runnable).start()
        readyQueue = LinkedList<KeyValueList>()
        while (true) {
            msgEncoder.sendMsg(readyQueue.poll())
        }
    }


    private fun analyzeMsg(msg: KeyValueList) {
        if (!msg.getValue("Scope").contains("VotingSystem")) {
            return
        }
        val msgID = Integer.parseInt(msg.getValue("MsgID"))
        when (msgID) {
            701 -> candiateType(msg)
            24 -> passcodeModify(msg)
            else -> {
                val handlermsg = Message.obtain()
                handlermsg.what = 1
                handlermsg.obj = "Received Message from:" + msg.getValue("Sender") + "\n"
                this.uiHandler.sendMessage(handlermsg)
            }
        }
    }

    private fun candiateType(msg: KeyValueList) {
        val oritinalAddress = java.lang.Long.parseLong(msg.getValue("VoterPhoneNo"))
        val msgContent = msg.getValue("CandidateID")
        if (oritinalAddress != 0L) {
            println(oritinalAddress)
            if (!phoneList.contains(oritinalAddress)) {
                if (!pollResult.containsKey(msgContent)) {
                    phoneList.add(oritinalAddress)
                    pollResult.put(msgContent, 1)
                    val re = "Received message with candidate ID: $msgContent by Phone number: $oritinalAddress\n"
                    val handlerMsg = Message.obtain()
                    handlerMsg.what = 1
                    handlerMsg.obj = re
                    uiHandler.sendMessage(handlerMsg)
                } else {
                    pollResult.put(msgContent, pollResult.get(msgContent)!! + 1)
                }
            }
        }
    }

    private fun passcodeModify(msg: KeyValueList) {
        if (!msg.getValue("Passcode").isEmpty() && !msg.getValue("SecurityLevel").isEmpty()) {
            passcode = msg.getValue("Passcode")
            securityLevel = Integer.parseInt(msg.getValue("SecurityLevel"))
        }

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val handler = Handler()
        println("Received Message")
        if (intent != null) {
            if (intent.action == SMS_RECEIVED_ACTION) {
                val bundle = intent.extras
                if (bundle != null) {
                    // get sms objects
                    val pdus = bundle.get("pdus") as Array<Any>
                    if (pdus.isEmpty()) {
                        return
                    }
                    // large message might be broken into many
                    val messages = arrayOfNulls<SmsMessage>(pdus.size)
                    val sb = StringBuilder()
                    for (i in pdus.indices) {
                        messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                        sb.append(messages[i]!!.messageBody)
                        val temp = KeyValueList()
                        temp.putPair("Scope", "VotingSystem")
                        temp.putPair("VoterPhoneNo", messages[i]?.originatingAddress)
                        temp.putPair("MessageType", "Alert")
                        temp.putPair("Sender", "VotingSystem")
                        temp.putPair("Receiver", "VotingSystem")
                        temp.putPair("Name", "VotingSystem")
                        temp.putPair("MsgID", "701")
                        temp.putPair("CandidateID", messages[i]!!.messageBody)
                        readyQueue.add(temp)
                        val t =
                            "ID: " + messages[i]!!.messageBody + ", Num: " + messages[i]!!.originatingAddress
                        val msg = Message()
                        msg.what = 1
                        msg.obj = t
                        handler.sendMessage(msg)
                        Toast.makeText(context, t, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

}