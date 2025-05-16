package se.basile.compax;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import se.basile.compax.nmea.STALK;

public class MsgSender implements Runnable {
    private String server_ip;
    private int server_port;
    private PrintWriter mBufferOut;

    private String cServerMessage;
    private STALK stalk;

    public MsgSender(String server_ip, String server_port, String message) {
        this.server_ip = server_ip;
        this.server_port = Integer.parseInt(server_port);

        this.cServerMessage = message;
    }
    @Override
    public void run() {
        try {
            //insérer son adresse ip ici
            InetAddress serverAddr = InetAddress.getByName(server_ip);

            //créer un socket pour la connexion au serveur
            Socket socket = new Socket(serverAddr, server_port);

            try {
                //envoi d'un message au serveur
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                if (mBufferOut != null && !mBufferOut.checkError()) {
                    mBufferOut.println(cServerMessage);
                    mBufferOut.flush();
                }
                Log.d("COMPAX", cServerMessage+" +1 pressed");


            } catch (Exception e) {

                Log.e("COMPAX", "S: Error", e);

            } finally {

                socket.close();
            }

        } catch (Exception e) {

            Log.e("COMPAX", "C: Error", e);

        }

    }
}
