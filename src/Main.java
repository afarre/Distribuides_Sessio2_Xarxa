import network.SocketThread;

public class Main {
    public static void main(String[] args) {
        SocketThread socketThread = new SocketThread(Integer.parseInt(args[0]));
        socketThread.start();
    }
}
