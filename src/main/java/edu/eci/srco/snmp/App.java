package edu.eci.srco.snmp;

import java.io.IOException;

import org.snmp4j.smi.OID;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws IOException
    {
        App snmpapp = new App();
        //snmpapp.runSNMPManager();
        snmpapp.runSNMPClient();        
    }

    
    public void runSNMPManager() throws IOException {
        System.out.println( "Hello World!" );
        /**
        * Port 161 is used for Read and Other operations
        * Port 162 is used for the trap generation
        */
        SNMPManager client = new SNMPManager("udp:192.168.0.35/161");
        client.start();
        /**
        * OID - .1.3.6.1.2.1.1.1.0 => SysDec
        * OID - .1.3.6.1.2.1.1.5.0 => SysName
        * OID - .1.3.6.1.2.1.25.2.3.1.6.1 => StorageUsed1
        * OID - .1.3.6.1.2.1.25.2.3.1.5.1 => StorageSize1
        * => MIB explorer will be usefull here, as discussed in previous article
        */
        String sysDescr = client.getAsString(new OID(".1.3.6.1.2.1.1.5.0"));
        Integer storageSize = new Integer(client.getAsString(new OID(".1.3.6.1.2.1.25.2.3.1.5.1")));
        Integer storageUsed = new Integer(client.getAsString(new OID(".1.3.6.1.2.1.25.2.3.1.6.1")));
        double storageSizeInt = storageSize.doubleValue();
        double storageUsedInt = storageUsed.doubleValue();
        double storagePercent = storageUsedInt / storageSizeInt;
        System.out.println("System Name: " + sysDescr);
        System.out.println("Percentage of Disk Used: " + storagePercent * 100 + "%");
    }

    private void runSNMPClient() throws IOException {
        OID sysDescr = new OID(".1.3.6.1.2.1.1.1.0");
        SNMPAgent agent = null;
        SNMPManager client = null;
        agent = new SNMPAgent("0.0.0.0/161");
		agent.start();

		// Since BaseAgent registers some MIBs by default we need to unregister
		// one before we register our own sysDescr. Normally you would
		// override that method and register the MIBs that you need
		agent.unregisterManagedObject(agent.getSnmpv2MIB());

		// Register a system description, use one from you product environment
		// to test with
		agent.registerManagedObject(MOCreator.createReadOnly(sysDescr,
				"Descripcion Personalizada SRCO!"));

		// Setup the client to use our newly started agent
		// client = new SNMPManager("udp:127.0.0.1/2001");
		// client.start();
		// // Get back Value which is set
		// System.out.println(client.getAsString(sysDescr));
        System.out.println("Finished");
    }

}
