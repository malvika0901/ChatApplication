import java.io.File;
import java.io.Serializable;


public class Message implements Serializable{

    

   
    private String text;
    private File file;
    private String userName;
    private String senderUserName;
    byte[] fileContent;

    Message(){}

   

    public void setText(String text){
        this.text = text;
    }

    public String getText(){
        return text;
    }

    public void setFile(File file){
        this.file = file;
    }

    public File getFile(){
        return file;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public String getUserName(){
        return userName;
    }

    public void setSenderUserName(String userName){
        this.senderUserName = userName;
    }
    public String getSenderUserName(){
        return this.senderUserName;
    }

    public void setFileContent(byte[] bytes){
        this.fileContent = bytes;
    }

    public byte[] getFileContent(){
        return this.fileContent;
    }
}