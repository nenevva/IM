package Server;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class ServerVoiceThread implements Runnable{
    public Socket socket;
    private DataOutputStream output;
    int from;
    public ServerVoiceThread(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try {
            output=new DataOutputStream(socket.getOutputStream());
            DataInputStream input= new DataInputStream(socket.getInputStream());
            from=input.readInt();
            int to=input.readInt();
            ServerVoice.voiceClientMap.put(from,socket);
            while(true){
                byte[] buffer=new byte[4096];
                input.readFully(buffer);

                if(ServerVoice.voiceClientMap.get(to)!=null){
                    DataOutputStream datato =new DataOutputStream(ServerVoice.voiceClientMap.get(to).getOutputStream());
                    datato.write(buffer);
                    datato.flush();
                }
            }
        }
        catch (SocketException e){
            System.out.println("voice chat finish");
            ServerVoice.voiceClientMap.remove(from);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
