package pfs;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class User implements Runnable {

    public SocketChannel channel;
    private boolean connected = true;
    private Server server;
    private int uid;
    public String name;
    public String room;
    public String lastroom;
    public float x;
    public float y;
    private boolean loggedin;

    ByteBuffer wBuffer = ByteBuffer.allocate(4096);

    public User(Server server, SocketChannel channel, int idd) {
        this.server = server;
        this.channel = channel;
        this.name = "";
        this.room = "";
        this.x = 0;
        this.y = 0;
        this.uid = idd;
        this.loggedin = false;
    }

    @Override
    public void run() {
        while (connected) {
            ByteBuffer buffer;
            try {
                buffer = ByteBuffer.allocate(1024);
                @SuppressWarnings("unused")
                var read = channel.read(buffer);
                if (buffer.hasArray()) {
                    String data = new String(buffer.array(), Charset.defaultCharset());
                    try {
                        JSONObject json = new JSONObject("{type : 99}");
                        if (data.charAt(0) == '{') {
                            json = new JSONObject(data);
                        }
                        switch (getMessageContype(Integer.parseInt(json.get("type").toString()))) {
                            case Join:
                                this.room = json.get("room").toString();
                                this.lastroom = this.room;
                                try {
                                    OutputStream out = channel.socket().getOutputStream();
                                    BufferedOutputStream bout = new BufferedOutputStream(out);
                                    String str = "{\"type\" : 0, \"uid\" : " + getuid() + "};\n";
                                    byte buf[] = str.getBytes();
                                    bout.flush();
                                    bout.write(buf);
                                    bout.flush();
                                } catch (IOException e) { 
                                }
                            case Update:
                                this.room = json.get("room").toString();
                                this.x = Float.parseFloat(json.get("x").toString());
                                this.y = Float.parseFloat(json.get("y").toString());
                                Server.updateUser(this);
                                break;
                            case ChangeRoom:
                                this.lastroom = this.room;
                                this.room = json.get("room").toString();
                                System.out.println(getTimeStamp() + this.name + " joined room: " + this.room);
                                Server.updateUser(this);
                                Server.getRoom(this);
                                break;
                            case Ping:
                                System.out.println("Ping");
                                System.out.println(json.toString());
                                try {
                                    OutputStream out = channel.socket().getOutputStream();
                                    String str = "Pong\n";
                                    byte buf[] = str.getBytes();
                                    out.write(buf);
                                } catch (IOException e) { 
                                }
                                break;
                            case Disconnect:
                                System.out.println(channel.socket().getInetAddress().toString() + " has disconnected.");
                                connected = false;
                                server.removeClient(this);
                                try {
                                    channel.close();
                                } catch (IOException ex1) {
                                    ex1.printStackTrace();
                                }
                                break;
                            case Login:
                                String username = json.get("username").toString();
                                String password = json.get("password").toString();
                                if (ConexaoMySQL.login(username, password)) {
                                    this.loggedin = true;
                                    String characters = ConexaoMySQL.getCharacters(username);
                                    Server.sendData(this, "{\"type\" : 6, \"characters\" : \"" + characters + "\"}");
                                }
                                break;
                            case SelectCharacter:
                                this.name = json.get("name").toString();
                                break;
                            case Null:
                                break;
                            default:
                                break;
                        }
                    }catch (JSONException err){
                            //System.out.println(err.toString());
                    }
                }
            } catch (IOException ex) {
                //ex.printStackTrace();
                System.out.println(channel.socket().getInetAddress().toString() + " has disconnected.");
                connected = false;
                server.removeClient(this);
                try {
                    channel.close();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                }
            }
        }
    }

    public Integer getuid(){
        return this.uid;
    }

    private Server.Contype getMessageContype(int type){
        Server.Contype r = Server.Contype.Null;
        switch (type) {
            case 0: r = Server.Contype.Join; break;
            case 1: r = Server.Contype.Update; break;
            case 2: r = Server.Contype.ChangeRoom; break;
            case 3: r = Server.Contype.Ping; break;
            case 4: r = Server.Contype.Disconnect; break;
            case 5: r = Server.Contype.Login; break;
            case 6: r = Server.Contype.GetCharacters; break;
            case 7: r = Server.Contype.SelectCharacter; break;
        }
        return r;
    }

    private String getTimeStamp(){
        return LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + ":" + LocalDateTime.now().getSecond()  + "> ";
    }
}