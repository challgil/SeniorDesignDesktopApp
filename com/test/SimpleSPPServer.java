/**
 * Created by Chris Gill on 3/28/2016.
 */
import com.intel.bluetooth.BlueCoveImpl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.bluetooth.*;
import javax.microedition.io.*;

/**
 * Class that implements an SPP Server which accepts single line of
 * message from an SPP client and sends a single line of response to the client.
 */
public class SimpleSPPServer {

    //start server
    private String startServer() throws IOException{

        //Create a UUID for SPP
        UUID uuid = new UUID("1101", true);
        //Create the servicve url
        String connectionString = "btspp://localhost:" + uuid +";name=Sample SPP Server";

        //open server url
        StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier)Connector.open( connectionString );

        //Wait for client connection
        System.out.println("\nServer Started. Waiting for clients to connect...");
        StreamConnection connection=streamConnNotifier.acceptAndOpen();

        RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
        System.out.println("Remote device address: "+dev.getBluetoothAddress());
        System.out.println("Remote device name: "+dev.getFriendlyName(true));

        //read string from spp client
        InputStream inStream=connection.openInputStream();
        BufferedReader bReader=new BufferedReader(new InputStreamReader(inStream));
        String lineRead=bReader.readLine();
        System.out.println(lineRead);

        //send response to spp client
        OutputStream outStream=connection.openOutputStream();
        PrintWriter pWriter=new PrintWriter(new OutputStreamWriter(outStream));
        pWriter.write("Response String from SPP Server\r\n");
        pWriter.flush();

        pWriter.close();
        streamConnNotifier.close();

        BlueCoveImpl.shutdown();
        return lineRead;

    }


    public static void main(String[] args) throws IOException {

        //display local device address and name
        while(true) {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            System.out.println("Address: " + localDevice.getBluetoothAddress());
            System.out.println("Name: " + localDevice.getFriendlyName());

            SimpleSPPServer sampleSPPServer = new SimpleSPPServer();
            String loc = sampleSPPServer.startServer();

            if(loc.equals("STOP")) {
                try{
                    Process p = Runtime.getRuntime().exec("./closeNavit.sh"); //NEEDS TO KILL NAVIT OPEN OBD
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
            else {
                write(loc);

                navit();
            }
        }

    }

    static void navit(){ //NEED TO KILL OBD SOFTWARE!
        try {
            String cmd[] = {"./cmd.sh"}; //Reads commands from cmd.sh.
            Process p;
            p = Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void write(String loc){
        try {
            File file = new File("/home/pi/navit-build/navit/destination.txt");
            PrintWriter writer = new PrintWriter(file);
            writer.close();
            //if file doesn't exist, create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(loc);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
