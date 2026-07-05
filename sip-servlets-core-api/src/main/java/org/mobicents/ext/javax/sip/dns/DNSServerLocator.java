package org.mobicents.ext.javax.sip.dns;

import javax.sip.SipStack;

/** Stub - DNS server locator removed for J25 compact edition */
public interface DNSServerLocator {
    void init(SipStack stack);
    String getBestServer(String uri, String transport);
}
