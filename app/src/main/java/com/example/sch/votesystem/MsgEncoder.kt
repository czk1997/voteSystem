package com.example.sch.votesystem

import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream

internal class MsgEncoder {
    // used for writing Strings
    private var writer: PrintStream? = null

    constructor() {}

    /*
     * Constructor
     */
    @Throws(IOException::class)
    constructor(out: OutputStream) {
        writer = PrintStream(out)
    }

    /*
     * encode the KeyValueList that represents a message into a String and send
     */
    @Throws(IOException::class)
    fun sendMsg(kvList: KeyValueList?) {
        if (kvList == null || kvList.size() < 1) {
            return
        }

        writer!!.println(kvList.encodedString())
        writer!!.flush()
    }
}