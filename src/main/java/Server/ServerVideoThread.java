package Server;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ServerVideoThread implements Runnable{
    public Socket socket;
    private DataOutputStream output;
    public ServerVideoThread(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try {
            output=new DataOutputStream(socket.getOutputStream());
            DataInputStream input= new DataInputStream(socket.getInputStream());
            byte[] buffer=new byte[4096];
            int from=input.readInt();
            int to=input.readInt();
            ServerVideo.videoClientMap.put(from,socket);
            int count=0;
            while(true){
                byte[] sizeAr = new byte[4];
                input.read(sizeAr);
                int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

                byte[] imageAr = new byte[size];
                input.read(imageAr);

                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));
                count++;
                if(ServerVideo.videoClientMap.get(to)!=null){
                    DataOutputStream datato =new DataOutputStream(ServerVideo.videoClientMap.get(to).getOutputStream());
                    datato.write(sizeAr);
                    datato.write(imageAr);
                    datato.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
