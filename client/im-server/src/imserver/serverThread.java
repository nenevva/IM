package imserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Iterator;



public class serverThread implements Runnable{
    private Socket socket  = null;

    serverThread(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader bf = null;
        OutputStream os = null;
        PrintWriter pw = null;

        try{
            is = socket.getInputStream();
            isr = new InputStreamReader(is, "UTF-8");
            bf = new BufferedReader(isr);

            String msg;
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while((line = bf.readLine()) != null){
                buffer.append(line);
            }
            msg = buffer.toString();

            String instruction = msg.substring(0,2);
            String data = msg.substring(2);
            socket.shutdownInput();

            // paser msg. If it's a private chating, server will send ip address. else server will send boradcast
            if(instruction.equals("00")){ // 00 broadcast
                os = socket.getOutputStream();
                pw = new PrintWriter(os);
                pw.write("00"+"230.0.0.1");
                pw.flush();
                socket.close();
                //广播有人加入
                Thread.sleep(1000);
                synchronized(App.userbook){
                    App.userbook.add(new user(data, socket.getInetAddress().getHostAddress()));
                    MulticastSocket multicastSocket = new MulticastSocket(8089);
                    InetAddress bcAddress = InetAddress.getByName("230.0.0.1");
                    multicastSocket.joinGroup(bcAddress);
                    multicastSocket.setLoopbackMode(false); // 必须是false才能开启广播功能！！
                    DatagramPacket output = new DatagramPacket(new byte[0], 0, bcAddress, 8088);
                    StringBuffer buff = new StringBuffer();
                    buff.append("10");
                    for(Iterator<user> it = App.userbook.iterator(); it.hasNext(); ){
                        buff.append(it.next().getName() + "/");
                    }
                    byte[] userinfo = buff.toString().getBytes();
                    output.setData(userinfo);
                    multicastSocket.send(output);
                    multicastSocket.leaveGroup(bcAddress);
                    multicastSocket.close();
                }
            }
            if(instruction.equals("01")){ // 01 private
                System.out.println(data);
                synchronized(App.userbook){
                    int symbol = 0;
                    for (Iterator<user> it = App.userbook.iterator(); it.hasNext(); ) {
                        user element = it.next();
                        if(element.getName().equals(data)){
                            os = socket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.write("01"+element.getIP());
                            pw.flush();
                            System.out.println("01" + element.getIP());
                        }    
                    }
                    if(symbol == 0){
                        os = socket.getOutputStream();
                        pw = new PrintWriter(os);
                        pw.write("00");//找不到要私聊的对象
                        pw.flush();
                    }
                    socket.close();
                }
            }
            if(instruction.equals("10")){ 
                // 10 exit
                synchronized(App.userbook){
                    StringBuffer buff = new StringBuffer();
                    buff.append("10");
                    for(Iterator<user> it = App.userbook.iterator(); it.hasNext();){
                        if(it.next().getIP().equals(socket.getInetAddress().getHostAddress())){
                            it.remove();
                        }else{
                            buff.append(it.next().getName() + "/");
                        }
                    }
                    socket.close();
                    //广播有人离开
                    MulticastSocket multicastSocket = new MulticastSocket(8089);
                    InetAddress bcAddress = InetAddress.getByName("230.0.0.1");
                    multicastSocket.joinGroup(bcAddress);
                    multicastSocket.setLoopbackMode(false); // 必须是false才能开启广播功能！！
                    byte[] userinfo = buff.toString().getBytes();
                    DatagramPacket output = new DatagramPacket(userinfo, userinfo.length, bcAddress, 8088);
                    output.setData(userinfo);
                    multicastSocket.send(output);
                    multicastSocket.leaveGroup(bcAddress);
                    multicastSocket.close();

                }
            }
        }catch(IOException | InterruptedException  e){
            e.printStackTrace();
        }finally{
            try {
                if(pw!=null)
                    pw.close();
                if(os!=null)
                    os.close();
                if(bf!=null)
                    bf.close();
                if(isr!=null)
                    isr.close();
                if(is!=null)
                    is.close();
                if(socket!=null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        
    }
    
}
