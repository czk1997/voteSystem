package com.example.sch.votesystem

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

internal class MsgDecoder {
    // used for reading Strings
    private var reader: BufferedReader? = null

    /*
     * read and decode the message into KeyValueList
     */
    val msg: KeyValueList
        @Throws(Exception::class)
        get() {
            var kvList = KeyValueList()
            val builder = StringBuilder()

            var message: String? = reader!!.readLine()
            if (message != null && message.length > 2) {
                builder.append(message)
                Log.e("test", message)
                while (message != null && !message.endsWith(")")) {
                    message = reader!!.readLine()
                    builder.append("\n" + message!!)
                }

                kvList = KeyValueList
                    .decodedKV(builder.substring(1, builder.length - 1))
            }
            return kvList
        }

    constructor() {}
    /*
     * Constructor
     */
    @Throws(IOException::class)
    constructor(`in`: InputStream) {
        reader = BufferedReader(InputStreamReader(`in`))
    }
}
