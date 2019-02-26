package com.example.sch.votesystem

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

internal object NetTool {

    /**
     * Get the public address of this machine
     *
     * @return public address
     */
    // TODO Auto-generated catch block
    // System.out.println(e1.getMessage());
    val publicAddress: String?
        get() {
            val e: Enumeration<NetworkInterface>
            try {
                e = NetworkInterface.getNetworkInterfaces()
                while (e.hasMoreElements()) {
                    val addrs = e.nextElement()
                        .inetAddresses
                    while (addrs.hasMoreElements()) {
                        val inetAddress = addrs.nextElement() as InetAddress
                        if (isPublic(inetAddress) && inetAddress is Inet4Address) {
                            return inetAddress.getHostAddress()
                        }
                    }
                }
            } catch (e1: SocketException) {
            }

            return null
        }

    /**
     * If an address can be used for public access
     *
     * @param inetAddress
     * an address
     * @return public or not
     */
    private fun isPublic(inetAddress: InetAddress): Boolean {
        return (!inetAddress.isAnyLocalAddress
                && !inetAddress.isLinkLocalAddress
                && !inetAddress.isLoopbackAddress
                && !inetAddress.isMCGlobal && !inetAddress.isMCLinkLocal
                && !inetAddress.isMCNodeLocal && !inetAddress.isMCOrgLocal
                && !inetAddress.isMCSiteLocal
                && !inetAddress.isMulticastAddress)
    }
}
