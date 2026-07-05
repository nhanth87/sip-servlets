package org.mobicents.ha.javax.sip;

import java.net.InetAddress;

/** Stub - load balancer removed for J25 compact edition */
public interface SipLoadBalancer {
    boolean isLocal(String callId);
    InetAddress getAddress();
    int getSipPort();
}
