package Client;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamLockException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class VideoChatThread implements Runnable{

    public Socket socket;
    private DataOutputStream output;
    private int from;
    private int to;
    private Webcam webcam;
    private Boolean webcamLock=false;

    public VideoChatThread(String hostname, int port,int from, int to) {
        try {
            this.socket = new Socket(hostname,port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.from = from;
        this.to = to;
    }

    @Override
    public void run() {

        try {

            webcam = Webcam.getDefault();
            try{
                webcam.open();
            }
            catch (WebcamLockException e){
                webcamLock=true;//在同一台主机上，不能同时打开两个webcam
            }
            System.out.println("from"+from+";to"+to);
            output=new DataOutputStream(socket.getOutputStream());
            DataInputStream input= new DataInputStream(socket.getInputStream());
            output.writeInt(from);
            output.writeInt(to);
            int count=0;
            while(Client.isVideo){
                writeImage(output);
                BufferedImage img=readImage(input);
                if(img==null)
                    continue;
                File file=new File("img/"+from+"/"+to+"/");
                file.mkdirs();
                file=new File("img/"+from+"/"+to+"/"+count+++".jpg");
                ImageIO.write(img,"jpg",file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeImage(DataOutputStream output){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        BufferedImage image;
        try {
        if(webcamLock){
            image=ImageIO.read(new File("video_chat_debug_help.jpg"));
        }
        else{
            image = webcam.getImage();
        }

            ImageIO.write(image,"jpg",byteArrayOutputStream);
            byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
            output.write(size);
            output.write(byteArrayOutputStream.toByteArray());
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage readImage(DataInputStream input){
        byte[] sizeAr = new byte[4];
        try {
            input.read(sizeAr);
            int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

            byte[] imageAr = new byte[size];
            input.read(imageAr);

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
