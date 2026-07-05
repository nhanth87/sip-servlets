package org.mobicents.servlet.sip.dns;

import java.net.InetAddress;
import java.util.*;
import javax.sip.address.Hop;
import javax.sip.address.SipURI;
import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.dns.DNSResolver;

/** Noop DNS resolver for J25 compact edition */
public class MobicentsDNSResolver implements DNSResolver {
    private static final Logger logger = Logger.getLogger(MobicentsDNSResolver.class);
    
    @Override public List<InetAddress> lookupARecords(String host) { return Collections.emptyList(); }
    @Override public List<String> lookupSrvRecords(SipURI uri) { return Collections.emptyList(); }
    @Override public List<String> lookupNaptrRecords(SipURI uri) { return Collections.emptyList(); }
}
