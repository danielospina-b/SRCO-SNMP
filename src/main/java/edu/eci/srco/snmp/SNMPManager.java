package edu.eci.srco.snmp;

import java.io.IOException;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMPManager {

Snmp snmp = null;
String address = null;
String community = "public";

    /**
    * Constructor
    * @param add protocol/ip/port of the target.. form: [protocol]:[ip|host]/[port]
    * @param community community used by snmp
    */
    public SNMPManager(String address, String community) {
        this.address = address;
        this.community = community;
    }

    /**
    * Start the Snmp session. If you forget the listen() method you will not
    * get any answers because the communication is asynchronous
    * and the listen() method listens for answers.
    * @throws IOException
    */
    public void start() throws IOException {
        TransportMapping transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        // Do not forget this line!
        transport.listen();
    }

    /**
    * Method which takes a single OID and returns the response from the agent as a String.
    * @param oid
    * @return
    * @throws IOException
    */
    public String getAsString(OID oid) throws IOException {
        ResponseEvent event = get(new OID[] { oid });
        return event.getResponse().get(0).getVariable().toString();
    }

    /**
    * This method is capable of handling multiple OIDs
    * @param oids
    * @return
    * @throws IOException
    */
    public ResponseEvent get(OID oids[]) throws IOException {
        PDU pdu = new PDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(PDU.GET);
        ResponseEvent event = snmp.send(pdu, getTarget(), null);
        if(event != null) {
            return event;
        }
        throw new RuntimeException("GET timed out");
    }

    /**
     * Makes a SET Request and return the result as a String
     * @param oid oid to be used
     * @param value value to SET
     * @return makes a GET Request of the same OID returned as a String
     * @throws IOException ...
     */
    public String setAsString(OID oid, String value) throws IOException {
        ResponseEvent event = set(oid, new OctetString(value));
        return event.getResponse().get(0).getVariable().toString();
    }

    /**
     * Makes a SET Request
     * @param oid OID to be used
     * @param value Value to SET
     * @return ResponseEvent
     * @throws IOException ...
     */
    public ResponseEvent set(OID oid, Variable value) throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid, value));
        pdu.setType(PDU.SET);
        ResponseEvent event = snmp.send(pdu, getTarget());
        if(event != null) {
            return event;
        }
        throw new RuntimeException("GET timed out");
    }

    /**
    * This method returns a Target, which contains information about
    * where the data should be fetched and how.
    * @return
    */
    private Target getTarget() {
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }
}