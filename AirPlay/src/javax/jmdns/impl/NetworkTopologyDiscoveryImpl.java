/**
 *
 */
package javax.jmdns.impl;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.jmdns.NetworkTopologyDiscovery;

import android.util.Log;

/**
 * This class implements NetworkTopologyDiscovery.
 * 
 * @author Pierre Frisch
 */
public class NetworkTopologyDiscoveryImpl implements NetworkTopologyDiscovery {

    private final Method        _isUp;

    private final Method        _supportsMulticast;

    /**
     *
     */
    public NetworkTopologyDiscoveryImpl() {
        super();
        Method isUp;
        try {
            isUp = NetworkInterface.class.getMethod("isUp", (Class<?>[]) null);
        } catch (Exception exception) {
            // We do not want to throw anything if the method does not exist.
            isUp = null;
        }
        _isUp = isUp;
        Method supportsMulticast;
        try {
            supportsMulticast = NetworkInterface.class.getMethod("supportsMulticast", (Class<?>[]) null);
        } catch (Exception exception) {
            // We do not want to throw anything if the method does not exist.
            supportsMulticast = null;
        }
        _supportsMulticast = supportsMulticast;
    }

    /*
     * (non-Javadoc)
     * @see javax.jmdns.JmmDNS.NetworkTopologyDiscovery#getInetAddresses()
     */
    @Override
    public InetAddress[] getInetAddresses() {
        Set<InetAddress> result = new HashSet<InetAddress>();
        try {

            for (Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces(); nifs.hasMoreElements();) {
                NetworkInterface nif = nifs.nextElement();
                for (Enumeration<InetAddress> iaenum = nif.getInetAddresses(); iaenum.hasMoreElements();) {
                    InetAddress interfaceAddress = iaenum.nextElement();
                    if (Log.isLoggable("jmdns", Log.VERBOSE)) {
                        Log.d("jmdns", "Found NetworkInterface/InetAddress: " + nif + " -- " + interfaceAddress);
                    }
                    if (this.useInetAddress(nif, interfaceAddress)) {
                        result.add(interfaceAddress);
                    }
                }
            }
        } catch (SocketException se) {
            Log.w("jmdns", "Error while fetching network interfaces addresses: " + se);
        }
        return result.toArray(new InetAddress[result.size()]);
    }

    /*
     * (non-Javadoc)
     * @see javax.jmdns.JmmDNS.NetworkTopologyDiscovery#useInetAddress(java.net.NetworkInterface, java.net.InetAddress)
     */
    @Override
    public boolean useInetAddress(NetworkInterface networkInterface, InetAddress interfaceAddress) {
        try {
            if (_isUp != null) {
                try {
                    if (!((Boolean) _isUp.invoke(networkInterface, (Object[]) null)).booleanValue()) {
                        return false;
                    }
                } catch (Exception exception) {
                    // We should hide that exception.
                }
            }
            if (_supportsMulticast != null) {
                try {
                    if (!((Boolean) _supportsMulticast.invoke(networkInterface, (Object[]) null)).booleanValue()) {
                        return false;
                    }
                } catch (Exception exception) {
                    // We should hide that exception.
                }
            }
            if (interfaceAddress.isLoopbackAddress()) {
                return false;
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

}
