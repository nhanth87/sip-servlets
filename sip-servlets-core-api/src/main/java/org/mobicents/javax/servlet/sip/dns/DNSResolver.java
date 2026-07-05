package org.mobicents.javax.servlet.sip.dns;

import java.net.InetAddress;
import java.util.List;
import javax.sip.address.SipURI;

/** Stub - DNS resolver for J25 compact edition */
public interface DNSResolver {
    List<InetAddress> lookupARecords(String host);
    List<String> lookupSrvRecords(SipURI uri);
    List<String> lookupNaptrRecords(SipURI uri);
}
