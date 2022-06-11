package Client;

import GUI.Model.Content;
import javafx.application.Platform;

import javafx.scene.image.Image;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamLockException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;

import javafx.embed.swing.SwingFXUtils;

public class VideoChatThread implements Runnable{

    public Socket socket;
    private DataOutputStream output;
    private final int from;
    private final int to;
    private Webcam webcam;
    private Boolean webcamLock=false;
    BufferedImage debug_image;

    public VideoChatThread(String hostname, int port, int from, int to) {
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
                Random random=new Random();
                try {
                    Thread.sleep(random.nextInt(500));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                webcam.open();
            }
            catch (WebcamLockException e){
                System.out.println("摄像头锁定");
                debug_image=ImageIO.read(new File("video_chat_debug_help.jpg"));
                webcamLock=true;//在同一台主机上，不能同时打开两个webcam
            }

            output=new DataOutputStream(socket.getOutputStream());
            DataInputStream input= new DataInputStream(socket.getInputStream());
            output.writeInt(from);
            output.writeInt(to);
            int count=0;

            new Thread(new VideoWriteThread(output,webcam,webcamLock)).start();
            while(Content.isVideo){
                BufferedImage img=readImage(input);
                if(img==null)
                    continue;
                Image image = SwingFXUtils.toFXImage(img, null);
                Platform.runLater(() -> {
                    Content.videoController.updateImage(image);
                });
                
            }
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

class VideoWriteThread implements Runnable{
    private DataOutputStream output;
    private Webcam webcam;
    private Boolean webcamLock=false;
    BufferedImage debug_image;

    public VideoWriteThread(DataOutputStream output, Webcam webcam, Boolean webcamLock) {
        this.output = output;
        this.webcam = webcam;
        this.webcamLock = webcamLock;
        if (webcamLock){
            try {
                debug_image=ImageIO.read(new File("video_chat_debug_help.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while(Content.isVideo){
            writeImage(output);
        }
    }

    private void writeImage(DataOutputStream output){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        BufferedImage image;
        try {
            if(webcamLock){
                image=debug_image;
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
}
