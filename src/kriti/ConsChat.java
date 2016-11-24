import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;


public class ConsChat {
  public static void main(String[] args) throws IOException {
    new ConsChat().chat();
  }

  protected Socket sock;
  protected BufferedReader cons;
  ObjectOutputStream objectOutputStream;         //stream write to the socket
  ObjectInputStream objectInputStream; 
  FileInputStream fis;
  BufferedInputStream bis;
  
  protected ConsChat() throws IOException {
    sock = new Socket("localhost", Chat.PORTNUM);
    cons = new BufferedReader(new InputStreamReader(System.in));
    objectOutputStream = new ObjectOutputStream(sock.getOutputStream());
    objectInputStream = new ObjectInputStream(sock.getInputStream());
    
    // Construct and start the reader: from server to stdout.
    // Make a Thread to avoid lockups.
    new Thread() {
      public void run() {
        setName("socket reader thread");
        System.out.println("Starting " + getName());
        System.out.flush();
        try {
          // blocking-read
          while (true) {
        	  Message receivedMessage=new Message();
        	  receivedMessage=(Message)objectInputStream.readObject();
        	  String text=receivedMessage.getText();
        	  String [] messageSplit=text.split(" ");
        	  if(messageSplit[0].equalsIgnoreCase("Unicast")||messageSplit[0].equalsIgnoreCase("Blockcast"))
        	  { if(messageSplit[1].equalsIgnoreCase("Message"))
        		  {
        		  String senderName=receivedMessage.getSenderUserName();
        		  System.out.println(senderName+":"+messageSplit[3]); 
        		  }
        	  else if(messageSplit[1].equalsIgnoreCase("File"))
        		  saveFile(receivedMessage);
        	  else
        		  continue;
        	  }
        	  else  
        	  {
        		  if(messageSplit[1].equalsIgnoreCase("Message"))
            		  {
        			  String senderName=receivedMessage.getSenderUserName();
              		
        			  System.out.println(senderName+":"+messageSplit[2]); 
            		  }
            	  else if(messageSplit[1].equalsIgnoreCase("File"))
            		  saveFile(receivedMessage);
            	  else
            		  continue;  
        	  }
        	  
          
          }
        } catch (IOException ex) {
          System.err.println("Read error on socket: " + ex);
          return;
        }
        catch(ClassNotFoundException ex)
        {
        	ex.printStackTrace();
        }
      }
      public void saveFile(Message message) {
    	  //System.out.println("in save file");
          try {
        	  if(!Files.isDirectory(Paths.get(message.getUserName())))
        			  new File(message.getUserName()).mkdir();
              FileOutputStream fileOutputStream = new FileOutputStream(message.getUserName()+"/"+message.getFile().getName());
              fileOutputStream.write(message.fileContent);
          } catch (FileNotFoundException e) {
              e.printStackTrace();
          }
          catch (IOException e) {
              e.printStackTrace();
          }
      }
    }.start();
  }

  protected void chat() throws IOException {
    String text;

    System.out.print("Login name: "); System.out.flush();
    text = cons.readLine();
    sendText(Chat.CMD_LOGIN + " " +text);

    // Main thread blocks here
    while ((text = cons.readLine()) != null) {
      if (text.length() == 0 || text.charAt(0) == '#')
        continue;      
      else
      { //text: Unicast Message mal hello
    	  String [] messageToSend=text.split(" ");
    	  if (messageToSend[1].equalsIgnoreCase("Message"))
    	  sendText(text);
    	  else
    		  sendFile(text);
      
    }
    }
  }

  protected void sendText(String textMessage) {
	  
   /* pw.println(textMessage);
    pw.flush();
    */
	  Message message=new Message();
	  message.setText(textMessage);
	  try {
		objectOutputStream.writeObject(message);
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
  protected void sendFile(String textMessage)
  {
	  /*
	   * define a file object with File content and the File name set
	   */
	  Message message= new Message();
	  String [] messageSplit=textMessage.split(" ");
	  if(messageSplit[0].equalsIgnoreCase("Unicast")||messageSplit[0].equalsIgnoreCase("Blockcast"))
	  { String path=messageSplit[3];
	  File file=new File(path);
	  message.setFile(file);
	  byte [] fileinBytes  = new byte [(int)file.length()];
      try {
		fis = new FileInputStream(file);
	    bis = new BufferedInputStream(fis);
	    bis.read(fileinBytes,0,fileinBytes.length);
	    message.setFileContent(fileinBytes);
	    message.setText(textMessage);
        objectOutputStream.writeObject(message);
  }
	  
      catch (IOException e) {
  		e.printStackTrace();
  	}
	  }
	  else
	  {
		  String path=messageSplit[2];
		  File file=new File(path);
		  message.setFile(file);
		  byte [] fileinBytes  = new byte [(int)file.length()];
	      try {
			fis = new FileInputStream(file);
		    bis = new BufferedInputStream(fis);
		    bis.read(fileinBytes,0,fileinBytes.length);
		    message.setFileContent(fileinBytes);
		    message.setText(textMessage);
	        objectOutputStream.writeObject(message);
	  }
		  
	      catch (IOException e) {
	  		e.printStackTrace();
	  	}
		
	  }
	
  }
  }

