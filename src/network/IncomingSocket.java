package network;

import com.google.gson.Gson;
import model.LamportRequest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class IncomingSocket extends Thread{
    private DataInputStream diStream;
    private DataOutputStream doStream;
    private Socket socket;
    private SocketThread parent;
    private String name;

    private final static String LWA1 = "LWA1";
    private final static String LWA2 = "LWA2";
    private final static String LWA3 = "LWA3";

    /** Constants per al algoritme de lamport **/
    private final static String LAMPORT_REQUEST = "LamportRequest";
    private final static String RESPONSE_REQUEST = "ResponseRequest";
    private final static String REMOVE_REQUEST = "RemoveRequest";
    private final static String ONLINE = "ONLINE";
    private final static String PORT = "PORT";
    private final static String LWA_WORK = "LWA_WORK";

    public IncomingSocket(Socket socket, SocketThread parent) {
        this.socket = socket;
        this.parent = parent;
    }

    @Override
    public void run() {
        try {
            diStream = new DataInputStream(socket.getInputStream());
            doStream = new DataOutputStream(socket.getOutputStream());
            while (true){
                String request = diStream.readUTF();
                readRequest(request);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readRequest(String request) throws IOException {
        System.out.println("\t\tRequest: " + request + " in thread: " + Thread.currentThread().getName());
        switch (request){
            case PORT:
                int port = diStream.readInt();
                name = diStream.readUTF();
                System.out.println("\t\tGot ONLINE call from: " + name + " in thread " + Thread.currentThread().getName());
                parent.createOutgoingSocket(port, this, name);
                parent.setOnline(name);
                break;
            case LWA_WORK:
                //parent.lwaWork();
                parent.notifyChildren();
                break;
            case LAMPORT_REQUEST:
                String lamportRequest = diStream.readUTF();
                System.out.println("\t\tGot this lamport request: " + lamportRequest  + " in " + name);
                parent.sendLamportToBrothers(lamportRequest, name);
                break;
            case "CS":
                if (!parent.isCS()){
                    parent.setCS(true);
                    doStream.writeBoolean(true);
                }
                break;
            case "REMOVE":
                parent.setCS(false);
                String msg = diStream.readUTF();
                Gson gson = new Gson();
                LamportRequest lr = gson.fromJson(msg.replace(REMOVE_REQUEST,""), LamportRequest.class);
                parent.notifyRemove(lr);
                break;
        }
    }

    public void sendResponse(String outgoingRequest) {
        try {
            System.out.println("\tSending this response: " + outgoingRequest + " to " + name);
            //doStream.writeUTF(RESPONSE_REQUEST);
            doStream.writeUTF(outgoingRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
