package Client;

import Util.AudioUtils;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamLockException;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class VoiceChatThread implements Runnable{

    public Socket socket;
    private DataOutputStream output;
    private int from;
    private int to;
    TargetDataLine audioIn;
    SourceDataLine audioOut;
    public VoiceChatThread(String hostname, int port,int from, int to) {
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
            audioIn=AudioUtils.getTargetDataLine();
            audioOut=AudioUtils.getSourceDataLine();

            System.out.println("from"+from+";to"+to);
            output=new DataOutputStream(socket.getOutputStream());
            DataInputStream input= new DataInputStream(socket.getInputStream());
            output.writeInt(from);
            output.writeInt(to);
            int count=0;
            while(Client.isVideo){
                writeAudio(output);
                readAudio(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void writeAudio(DataOutputStream output) throws IOException {
        byte[] audioBuffer=new byte[4096];
        int writeLen = audioIn.read(audioBuffer,0,audioBuffer.length);
        //发
        if (audioBuffer != null) {
            //向对方发送拾音器获取到的音频
            output.write(audioBuffer,0,writeLen);
        }
    }

    private void readAudio(DataInputStream input) throws IOException {
        //收
        byte[] audioBuffer=new byte[4096];
        int readLen = input.read(audioBuffer);
        if (audioBuffer != null) {
            //播放对方发送来的音频
            audioOut.write(audioBuffer, 0, readLen);
        }
    }
}
