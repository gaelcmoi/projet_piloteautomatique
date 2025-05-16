package se.basile.compax;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MsgReceiver implements Runnable {
    private String server_ip;
    private int server_port;
    // message a envoyer au serveur
    private String mServerMessage;
    private OnMessageReceived mMessageListener = null;

    // tant que c'est vrai le serverur tour
    private boolean mRun = false;
    // envoie de messages
    private PrintWriter mBufferOut;
    // lit les messages
    private BufferedReader mBufferIn;

    public MsgReceiver(String server_ip, String server_port, MsgReceiver.OnMessageReceived listener) {
        this.server_ip = server_ip;
        this.server_port = Integer.parseInt(server_port);

        mMessageListener = listener;
    }

    @Override
    public void run() {
        mRun = true;
        Log.d("COMPAX", "Thread1.run()");

        try {
            //mettre adresse ip ici
            InetAddress serverAddr = InetAddress.getByName(server_ip);

            //creation d'un socket serveur
            Socket socket = new Socket(serverAddr, server_port);

            try {

                //recoie un message a renvoyer
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //pendant un temps défini le client écoute les messages du serveur
                while (mRun) {
                    mServerMessage = mBufferIn.readLine();
                    if (mServerMessage != null && mMessageListener != null) {
                        //appel de methode
                        mMessageListener.messageReceived(mServerMessage);
                    }
                }
                Log.e("COMPAX", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {

                Log.e("COMPAX", "S: Error", e);

            } finally {

                socket.close();
            }

        } catch (Exception e) {

            Log.e("COMPAX", "C: Error", e);

        }

    }

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
