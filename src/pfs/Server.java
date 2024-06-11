package pfs;
import java.io.IOException;
import java.io.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Server {

    enum Contype {
        Join,
        Update,
        ChangeRoom,
        Ping,
        Disconnect,
        Login,
        GetCharacters,
	    SelectCharacter,
        Null
    }

    public static List<User> clients;
    private ServerSocketChannel socket;
    private boolean running;
    private int lastid;

    public Server(int port) {
        Server.clients = new ArrayList<User>();
        this.running = false;

        System.out.print(getTimeStamp() + "[Server] Trying to Listen on Port : " + port + "...");
        try {
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.socket().bind(new java.net.InetSocketAddress(port));
            System.out.println("Success!");
            channel.configureBlocking(false);
            socket = channel;
            running = true;
        } catch (IOException e) {
            System.err.println("Failed!");
            socket = null;
            running = false;
        }

        // Server loop
        while (running) {
            try {
                // Sleep the thread
                Thread.sleep(1);
                // Check for new connections
                SocketChannel newChannel = socket.accept();
                // If a connection is found, create a User and add it to
                // the client list
                if (newChannel != null) {
                    System.out.println("New Connection " + newChannel.socket().getInetAddress().toString());
                    ++lastid;
                    User c = new User(this, newChannel, lastid);
                    Thread t = new Thread(c);
                    t.start();
                    clients.add(c);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeClient(User user) {
        clients.remove(user);
    }

    public static void updateUser(User joined){
        try {
            for (User user : clients) {
                if (user.getuid() != joined.getuid() && (user.room.compareToIgnoreCase(joined.room) == 0 || user.room.compareToIgnoreCase(joined.lastroom) == 0)) {
                    OutputStream out = user.channel.socket().getOutputStream();
                    BufferedOutputStream bout = new BufferedOutputStream(out);
                    String str = "{\"type\" : \"1\", \"name\" : \"" + joined.name + "\", \"x\" : \"" + joined.x + "\", \"y\" : \"" + joined.y + "\", \"uid\" : \"" + joined.getuid() + "\", \"room\" : \"" + joined.room + "\"};\n";
                    byte buf[] = str.getBytes();
                    bout.flush();
                    bout.write(buf);
                    bout.flush();
                }
            }
        } catch (IOException e) {
        }
    }

    public static void getRoom(User usr){
        try {
            OutputStream out = usr.channel.socket().getOutputStream();
            BufferedOutputStream bout = new BufferedOutputStream(out);
            for (User user : clients) {
                if (user.room.compareToIgnoreCase(usr.room) != 0) {
                    continue;
                }
                if (user.getuid() == usr.getuid()) {
                    continue;
                }
                String str = "{\"type\" : \"1\", \"name\" :\"" + user.name + "\", \"x\" : \"" + user.x + "\", \"y\" : \"" + user.y + "\", \"uid\" : \"" + user.getuid() + "\", \"room\" : \"" + user.room + "\"};\n";
                byte buf[] = str.getBytes();
                bout.flush();
                bout.write(buf);
                bout.flush();
            }
            //System.out.println("-------------------------------------------------------------------------------");
        } catch (IOException e) {
        }
    }

    public static void sendData(User user, String data){
        try {
            data = data + ";";
            OutputStream out = user.channel.socket().getOutputStream();
            BufferedOutputStream bout = new BufferedOutputStream(out);
            byte buf[] = data.getBytes();
            bout.flush();
            bout.write(buf);
            bout.flush();
        } catch (IOException e) {
        }
    }

    public static String getTimeStamp(){
        int hour = LocalDateTime.now().getHour();
        int minute = LocalDateTime.now().getMinute();
        int second = LocalDateTime.now().getSecond();
        String hourstring = String.valueOf(hour);
        if (hour < 9) {
            hourstring = "0" + String.valueOf(hour);
        }
        String minutestring = String.valueOf(minute);
        if (minute < 9) {
            minutestring = "0" + String.valueOf(minute);
        }
        String secondstring = String.valueOf(second);
        if (second < 9) {
            secondstring = "0" + String.valueOf(second);
        }
        return "[" + hourstring + ":" + minutestring + ":" + secondstring + "] ";
    }

    public static void main(String... args) {
        for (int i = 0; i < 30; i++) {
            System.out.println();
        }
    	ConexaoMySQL.getConexaoMySQL();
    	System.out.println(ConexaoMySQL.statusConection());        
    	new Server(21337); 
    }

}