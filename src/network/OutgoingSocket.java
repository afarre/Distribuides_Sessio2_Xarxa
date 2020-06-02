package network;

import com.google.gson.Gson;
import model.LamportRequest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class OutgoingSocket extends Thread{
    private DataInputStream diStream;
    private DataOutputStream doStream;
    private Socket outSocket;

    private final static String LAMPORT_REQUEST = "LamportRequest";
    private final static String RESPONSE_REQUEST = "ResponseRequest";
    private final static String REMOVE_REQUEST = "RemoveRequest";
    private static final String TOKEN = "TOKEN";
    private static final String WORK = "WORK";

    private int PORT;
    private String name;
    private SocketThread parent;
    private String action;
    private String outgoingRequest;
    private ArrayList<String> forwardingQueue;

    public OutgoingSocket(int port, SocketThread socketThread, String name) {
        this.PORT = port;
        parent = socketThread;
        this.name = name;
        forwardingQueue = new ArrayList<String>();
    }

    @Override
    public void run() {
        createOutcomeConnection();
        while (true){
            try {
                synchronized (this){
                    wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("\tGot notified in " + name + " with the action " + action + " in the thread" + Thread.currentThread().getName());
            switch (action){
                case WORK:
                    work(name);
                    break;
                case LAMPORT_REQUEST:
                    sendLamport(/*outgoingRequest*/);
                    break;
                case RESPONSE_REQUEST:
                    sendResponse(outgoingRequest);
                    break;
            }
        }
    }

    private void createOutcomeConnection() {
        // Averiguem quina direccio IP hem d'utilitzar
        InetAddress iAddress;
        try {
            iAddress = InetAddress.getLocalHost();
            String IP = iAddress.getHostAddress();

            outSocket = new Socket(String.valueOf(IP), PORT);
            doStream = new DataOutputStream(outSocket.getOutputStream());
            diStream = new DataInputStream(outSocket.getInputStream());
        } catch (ConnectException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        try {
            doStream.writeUTF(TOKEN);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void work(String name) {
        try {
            System.out.println("\tWriting work to " + name);
            doStream.writeUTF(WORK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendLamport(/*String lamport*/) {
        for (String lamport : forwardingQueue) {
            try {
                Gson gson = new Gson();
                System.out.println("\tSending this lamport out: " + lamport + " to " + name + " from thread" + Thread.currentThread().getName());
                LamportRequest lamportRequest = gson.fromJson(lamport.replace(LAMPORT_REQUEST, ""), LamportRequest.class);
                doStream.writeUTF(LAMPORT_REQUEST);
                doStream.writeUTF(lamport);
                String responseRequest = diStream.readUTF();
                System.out.println("\t" + name + " answered with this response request: " + responseRequest + " in thread " + Thread.currentThread().getName());
                parent.sendResponseRequest(responseRequest, lamportRequest.getProcess());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendResponse(String outgoingRequest) {
        try {
            System.out.println("\tSending this response: " + outgoingRequest + " to " + name);
            doStream.writeUTF(RESPONSE_REQUEST);
            doStream.writeUTF(outgoingRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRemove(LamportRequest lr) {
        try {
            doStream.writeUTF(REMOVE_REQUEST);
            doStream.writeUTF(lr.toString().replace(LAMPORT_REQUEST, REMOVE_REQUEST));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void myNotify(String action){
        this.action = action;
        synchronized (this){
            this.notify();
        }
    }

    public void setOutgoingRequest(String outgoingRequest) {
        this.outgoingRequest = outgoingRequest;
    }

    public synchronized void addForwardingQueue(String msg){
        forwardingQueue.add(msg);
        if (forwardingQueue.size() == 2){
            myNotify(LAMPORT_REQUEST);
        }
    }
}
