package network;

import model.LamportRequest;
import model.ThreadIdentifier;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SocketThread extends Thread {
    private int PORT;
    private ArrayList<ThreadIdentifier> threadList;
    private boolean CS;

    private final static String HWA = "HWA";
    private final static String LWA1 = "LWA1";
    private final static String LWA2 = "LWA2";
    private final static String LWA3 = "LWA3";
    private final static String HWB = "HWB";
    private final static String LWB1 = "LWB1";
    private final static String LWB2 = "LWB2";

    private final static String LAMPORT_REQUEST = "LamportRequest";
    private final static String RESPONSE_REQUEST = "ResponseRequest";
    private final static String REMOVE_REQUEST = "RemoveRequest";
    private final static String ONLINE = "ONLINE";
    private final static String LWA_WORK = "LWA_WORK";
    private static final String WORK = "WORK";

    private boolean HWAOnline;
    private boolean LWA1Online;
    private boolean LWA2Online;
    private boolean LWA3Online;
    private boolean HWBOnline;
    private boolean LWB1Online;
    private boolean LWB2Online;

    public SocketThread(int port){
        PORT = port;
        threadList = new ArrayList<ThreadIdentifier>();
        HWAOnline = false;
        LWA1Online = false;
        LWA2Online = false;
        LWA3Online = false;
        HWBOnline = false;
        LWB1Online = false;
        LWB2Online = false;
        CS = false;
    }

    @Override
    public void run(){
        try {
            //creem el nostre socket
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true){
                //esperem a la conexio d'algun usuari dins d'un bucle infinit. A cada usuari li crearem un nou servidor dedicat
                Socket sClient = serverSocket.accept();
                generaNouServidorDedicat(sClient);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Genera un nuevo servidor dedicado por cada cliente que se quiera conectar
     * @param socket Socket del cliente que se quiere conectar
     */
    private void generaNouServidorDedicat(Socket socket){
        IncomingSocket incomingSocket = new IncomingSocket(socket, this);
        incomingSocket.start();
    }

    public void createOutgoingSocket(int port, IncomingSocket incomingSocket, String name) {
        OutgoingSocket outgoingSocket = new OutgoingSocket(port, this, name);
        outgoingSocket.start();
        ThreadIdentifier threadIdentifier = new ThreadIdentifier(name, outgoingSocket, incomingSocket);
        threadList.add(threadIdentifier);
    }

    public void setOnline(String name) {
        switch (name) {
            case HWA:
                HWAOnline = true;
                break;
            case LWA1:
                LWA1Online = true;
                break;
            case LWA2:
                LWA2Online = true;
                break;
            case LWA3:
                LWA3Online = true;
                break;
            case HWB:
                HWBOnline = true;
                break;
            case LWB1:
                LWB1Online = true;
                break;
            case LWB2:
                LWB2Online = true;
                break;
        }
        if (HWAOnline && LWA1Online && LWA2Online && LWA3Online && HWBOnline && LWB1Online && LWB2Online){
            System.out.println("Everyone online\n");
            for (ThreadIdentifier ti: threadList){
                if (ti.getName().equals("HWA")){
                    ti.getOutgoingSocket().init();
                }
            }
        }
    }

    public void lwaWork() {
        for (ThreadIdentifier ti: threadList){
            if (ti.getName().contains("LWA")){
                ti.getOutgoingSocket().work(ti.getName());
            }
        }
    }

    public void sendLamportToBrothers(String lamport, String name) {
        for (ThreadIdentifier ti: threadList){
            if (ti.getName().contains("LWA") && !ti.getName().equals(name)){
                System.out.println("\t\tNotifying " + ti.getName() + "'s thread in order to forward this lamport request" + lamport + " from thread " + Thread.currentThread().getName());
                ti.getOutgoingSocket().addForwardingQueue(lamport);
                //ti.getOutgoingSocket().setOutgoingRequest(lamport);
                //ti.getOutgoingSocket().myNotify(LAMPORT_REQUEST);
                //ti.getOutgoingSocket().sendLamport();
            }
        }
    }

    public void notifyChildren() {
        for (ThreadIdentifier ti: threadList){
            if (ti.getName().contains("LWA")){
                ti.getOutgoingSocket().myNotify(WORK);
            }
        }
    }

    public void sendResponseRequest(String responseRequest, String name) {
        for (ThreadIdentifier ti: threadList){
            if (ti.getName().equals(name)){
                //ti.getOutgoingSocket().setOutgoingRequest(responseRequest);
                //ti.getOutgoingSocket().myNotify(RESPONSE_REQUEST);
                ti.getIncomingSocket().sendResponse(responseRequest);
            }
        }
    }

    public synchronized boolean isCS() {
        return CS;
    }

    public synchronized void setCS(boolean CS) {
        this.CS = CS;
    }

    public void notifyRemove(LamportRequest lr) {
        for (ThreadIdentifier ti: threadList){
            if (ti.getName().contains("LWA") && !ti.getName().equals(lr.getProcess())){
                ti.getOutgoingSocket().sendRemove(lr);
            }
            if (ti.getName().equals("HWA")){
                ti.getOutgoingSocket().notifyChildrenDone(lr);
            }
        }
    }
}
