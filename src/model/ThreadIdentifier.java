package model;

import network.IncomingSocket;
import network.OutgoingSocket;

public class ThreadIdentifier {
    private String name;
    private OutgoingSocket outgoingSocket;
    private IncomingSocket incomingSocket;

    public ThreadIdentifier(String name, OutgoingSocket outgoingSocket, IncomingSocket incomingSocket){
        this.name = name;
        this.outgoingSocket = outgoingSocket;
        this.incomingSocket = incomingSocket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OutgoingSocket getOutgoingSocket() {
        return outgoingSocket;
    }

    public void setOutgoingSocket(OutgoingSocket outgoingSocket) {
        this.outgoingSocket = outgoingSocket;
    }

    public IncomingSocket getIncomingSocket() {
        return incomingSocket;
    }

    public void setIncomingSocket(IncomingSocket incomingSocket) {
        this.incomingSocket = incomingSocket;
    }

    @Override
    public String toString() {
        return "ThreadIdentifier{" +
                "name='" + name + '\'' +
                ", outgoingSocket=" + outgoingSocket +
                ", incomingSocket=" + incomingSocket +
                '}';
    }
}
