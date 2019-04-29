package edu.eci.srco.snmp;

import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.snmp4j.smi.OID;

public class App {
    public static void main(String[] args) throws IOException
    {
        App snmpapp = new App();
        //snmpapp.runSNMPManager();
        //snmpapp.runSNMPClient();

        Options options = new Options();
        generateOptions(options);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar [jar] [<arguments>]", options);
            System.exit(1);
        }

        String setString = cmd.getOptionValue("set");
        String targetString = cmd.getOptionValue("target");
        String oidString = cmd.getOptionValue("oid");
        String communityString = cmd.getOptionValue("community");
        String protocolString = cmd.getOptionValue("protocol");
        String portString = cmd.getOptionValue("port");

        snmpapp.doOperation(setString, targetString, oidString, communityString, protocolString, portString);

    }

    /**
     * Segun los argumentos organiza los datos necesarios para hacer un SET o GET
     * @param setString puede ser Null, si lo es hará un GET Request
     * @param targetString IP o hostname del target
     * @param oidString OID a usar
     * @param communityString puede ser Null, si lo es utilizara "public"
     * @param protocolString puede ser "udp" o "tcp"
     * @param portString puerto entre 0 y 65535
     */
    private void doOperation(String setString, String targetString, String oidString, String communityString,
            String protocolString, String portString) {
        if (communityString == null) {
            communityString = "public";
        }
        String transportString = getTransportString(protocolString, targetString, portString);
        SNMPManager client = new SNMPManager(transportString, communityString);
        OID oid = new OID(oidString);
        try {
            client.start();
        } catch (IOException e) {
            System.out.println("Error starting SNMP Client, check your arguments...");
            System.out.println(e.getLocalizedMessage());
        }
        if (setString == null) {
            System.out.println("SNMP v2c GET Request \n\tto: " + transportString + "\n\tCommunity String: " + communityString + "\n\tOID: " + oidString);
            try {
                String response = client.getAsString(oid);
                System.out.println("Response: " + response);
            } catch (IOException e) {
                System.out.println("Error making SNMP GET Request...");
                System.out.println(e.getLocalizedMessage());
            }
        }
        else {
            System.out.println("SNMP v2c SET Request \n\tto: " + transportString + "\n\tCommunity String: " + communityString +  "\n\tOID: " + oidString + "\n\tData: " + setString);
            try {
                String response = client.setAsString(oid, setString);
                System.out.println("Response: " + response);
            } catch (IOException e) {
                System.out.println("Error making SNMP SET Request...");
                System.out.println(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Organiza el protocolo, target y puerto de la forma [protocol]:[target]/[puerto]
     * @param protocolString ...
     * @param targetString ...
     * @param portString ...
     * @return ...
     */
    private String getTransportString(String protocolString, String targetString, String portString) {
        if (protocolString == null) {
            protocolString = "udp";
        }
        if (portString == null) {
            portString = "161";
        }
        return protocolString + ":" + targetString + "/" + portString;
    }

    /**
     * Metodo para hacer pruebas como SNMP Manager,por defecto no se corre en ningun momento
     * @throws IOException .. 
     */
    public void runSNMPManager() throws IOException {
        System.out.println( "Hello World!" );
        /**
        * Port 161 is used for Read and Other operations
        * Port 162 is used for the trap generation
        */
        SNMPManager client = new SNMPManager("udp:192.168.0.35/161", "public");
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
        System.out.println("System Description =  " + sysDescr);
        System.out.println("Percentage of Disk Used = " + storagePercent * 100 + "%");
        //String newSysDescr = client.setAsString(new OID(".1.3.6.1.2.1.1.5.0"), "NewNamePC");
        //System.out.println("New System Descreiption = " + newSysDescr);

    }

    /**
     * Metodo para hacer pruebas como Cliente SNMP, por defecto no se corre en ningun momento
     * @throws IOException .. 
     */
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

    /**
     * Genera las opciones y argumentos al correr el programa (main())
     * @param options options object
     */
    private static void generateOptions(Options options) {
        Option set = new Option("s", "set", true, "tells the program if it is a SNMP SET operation and defines its value");
        set.setRequired(false);
        options.addOption(set);

        Option target = new Option("t", "target", true, "target hostname/ip");
        target.setRequired(true);
        options.addOption(target);

        Option oid = new Option("o", "oid", true, "OID used in the SNMP request");
        oid.setRequired(true);
        options.addOption(oid);

        Option community = new Option("c", "community", true, "Community String - default: public");
        community.setRequired(false);
        options.addOption(community);

        Option protocol = new Option("p", "protocol", true, "Protocol use: tcp or udp - default: udp");
        protocol.setRequired(false);
        options.addOption(protocol);

        Option port = new Option("pt", "port", true, "SNMP Port - default: 161");
        port.setRequired(false);
        options.addOption(port);

        Option help = new Option("h", "help", false, "Shows this help");
        help.setRequired(false);
        options.addOption(help);
    }
}
