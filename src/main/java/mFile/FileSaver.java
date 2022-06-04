package mFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileSaver {
    private int from;
    private int to;
    private long fileLength;
    private String location;
    private int parts;
    private int current;
    private String fileName;
    private FileOutputStream output;
    private boolean isGroup;
    final int PART_BYTE=4096-2-4;

    public FileSaver(int from, int to, long fileLength, String fileName, boolean isGroup) {
        this.from = from;
        this.to = to;
        this.fileLength = fileLength;
        this.fileName = fileName;
        this.isGroup = isGroup;
        current=0;
        parts= (int) (fileLength/PART_BYTE+1);
    }

    public void startSave(){
        if(isGroup){
            location="file/group/"+to+"/"+from+"/";
        }
        else{
            location="file/private/"+from+"/"+to+"/";
        }
        new File(location).mkdirs();
        location+=fileName;
        try {
            output=new FileOutputStream(location);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void write(int part,byte[] data) throws IOException {

        current++;
        if(current<parts) {
            output.write(data, 6, PART_BYTE);
            output.flush();
        }
        else{
            output.write(data,6, (int) (fileLength-PART_BYTE*(current-1)));
            output.flush();
            output.close();
        }
    }

    public boolean isFinish(){
        return current==parts;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileLength() {
        return fileLength;
    }
}
