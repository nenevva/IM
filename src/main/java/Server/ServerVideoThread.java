package Server;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
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
            int from=input.readInt();
            int to=input.readInt();
            ServerVideo.videoClientMap.put(from,socket);

            int count=0;
            byte[] my=new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
            while(true){
                byte[] sizeAr = new byte[4];
                input.readFully(sizeAr);
                int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

                byte[] imageAr = new byte[size];
                input.readFully(imageAr);

                if(ServerVideo.videoClientMap.get(to)!=null){
                    DataOutputStream datato =new DataOutputStream(ServerVideo.videoClientMap.get(to).getOutputStream());
                    byte[] data=new byte[sizeAr.length+imageAr.length];
                    System.arraycopy(sizeAr,0,data,0,4);
                    System.arraycopy(imageAr,0,data,4,data.length-4);
//                    datato.write(sizeAr);
//                    datato.write(imageAr);
                    datato.write(data);
                    datato.flush();
                }
            }
        }
        catch (SocketException e){
            System.out.println("video chat finish");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
