package com.example.sch.votesystem

import java.util.HashMap

internal class KeyValueList {
    // interal map for the message <property name, property value>, key and
    // value are both in String format
    private val map: MutableMap<String, String>

    /*
     * Constructor
     */
    init {
        map = HashMap()
    }

    /*
     * Add one property to the map
     */
    fun putPair(key: String?, value: String?): Boolean {
        var key = key
        var value = value
        key = key!!.trim { it <= ' ' }
        value = value!!.trim { it <= ' ' }
        if (key == null || key.length == 0 || value == null
            || value.length == 0
        ) {
            return false
        }
        map[key] = value
        return true
    }

    fun removePair(key: String): String {
        return map.remove(key)!!
    }

    // /*
    // * extract a List containing all the input message IDs in Integer format
    // * (specifically designed for message 20)
    // */
    // public List<Integer> InputMessages() {
    // int i = 1;
    // List<Integer> list = new ArrayList<>();
    // String m = map.get("InputMsgID" + i);
    // while (m != null) {
    // list.add(Integer.parseInt(m));
    // ++i;
    // m = map.get("InputMsgID" + i);
    // }
    // return list;
    // }
    //
    // /*
    // * extract a List containing all the output message IDs in Integer format
    // * (specifically designed for message 20)
    // */
    // public List<Integer> OutputMessages() {
    // int i = 1;
    // List<Integer> list = new ArrayList<>();
    // String m = map.get("OutputMsgID" + i);
    // while (m != null) {
    // list.add(Integer.parseInt(m));
    // ++i;
    // m = map.get("OutputMsgID" + i);
    // }
    // return list;
    // }

    /*
     * encode the KeyValueList into a String
     */
    /*
     * encode the KeyValueList into a String
     */
    fun encodedString(): String {

        val builder = StringBuilder()
        builder.append("(")
        for ((key, value) in map) {
            builder.append(key + delim + value + delim)
        }
        // X$$$Y$$$, minimum
        builder.append(")")
        return builder.toString()
    }

    /*
     * get the property value based on property name
     */
    fun getValue(key: String): String {
        val value = map[key]
        return value ?: ""
    }

    /*
     * get the number of properties
     */
    fun size(): Int {
        return map.size
    }

    /*
     * toString for printing
     */
    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("\n")
        for ((key, value) in map) {
            builder.append("$key : $value\n")
        }
        return builder.toString()
    }

    companion object {

        // delimiter for encoding the message
        val delim = "$$$"

        // regex pattern for decoding the message
        val pattern = "\\$+"

        /*
     * decode a message in String format into a corresponding KeyValueList
     */
        fun decodedKV(message: String): KeyValueList {
            val kvList = KeyValueList()

            val parts = message.split(pattern.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var validLen = parts.size
            if (validLen % 2 != 0) {
                --validLen
            }
            if (validLen < 1) {
                return kvList
            }

            var i = 0
            while (i < validLen) {
                kvList.putPair(parts[i], parts[i + 1])
                i += 2
            }
            return kvList
        }
    }
}