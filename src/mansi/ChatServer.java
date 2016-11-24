
import java.net.*;
import java.io.*;
import java.util.*;
import java.net.ServerSocket;

class ChatServer {
	protected final static String CHATMASTER_ID = "ChatMaster";
	protected final static String SEP = ": ";
	protected ServerSocket servSock;
	protected ArrayList clientList;
	ObjectOutputStream objectOutputStream; // stream write to the socket
	ObjectInputStream objectInputStream;
	String user = null;

	public static void main(String[] argv) {
		System.out.println("Chat Server starting...");
		ChatServer w = new ChatServer();
		w.runServer();
		System.out.println("Chat Server quitting");
	}

	ChatServer() {
		clientList = new ArrayList();
		try {
			servSock = new ServerSocket(Chat.PORTNUM);
		} catch (IOException e) {
			log("IO Exception in ChatServer.<init>" + e);
			System.exit(0);
		}
	}

	public void runServer() {
		try {
			while (true) {
				Socket us = servSock.accept();
				String hostName = us.getInetAddress().getHostName();
				System.out.println("Accepted from " + hostName);
				ChatHandler cl = new ChatHandler(us, hostName);
				synchronized (clientList) {
					clientList.add(cl);
					cl.start();
                    Message message=new Message();
                    message.setText("Please Enter Your User Name");
					cl.sendObject(CHATMASTER_ID, message);

				}
			}
		} catch (IOException e) {
			log("IO Exception in runServer: " + e);
			System.exit(0);
		}
	}

	protected void log(String s) {
		System.out.println(s);
	}

	protected class ChatHandler extends Thread {
		protected Socket clientListock;
		protected BufferedReader is;
		protected PrintWriter pw;
		protected String clientIP;
		public String getLogin() {
			return login;
		}

		public void setLogin(String login) {
			this.login = login;
		}

		protected String login;
		

		ObjectOutputStream objectOutputStream; // stream write to the socket
		ObjectInputStream objectInputStream;

		/* Construct a Chat Handler */
		public ChatHandler(Socket sock, String clnt) throws IOException {
			clientListock = sock;
			clientIP = clnt;
			is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			pw = new PrintWriter(sock.getOutputStream(), true);
			objectOutputStream = new ObjectOutputStream(sock.getOutputStream());
			objectInputStream = new ObjectInputStream(sock.getInputStream());

		}

		public void run() {
			String line;
			try {
				System.out.println("In chatserver run");
				while (true) {
					Message messageReceived=(Message)objectInputStream.readObject();
					String messageText=messageReceived.getText();
					String[] textRecieved = messageText.split(" ");
					String messageType = "Message";
					String command = textRecieved[0];
					if (command.equals("Login") || command == ("Login")) {
						user = textRecieved[1];
					}
					if (textRecieved.length > 1) {
						messageType = textRecieved[1];
					}
					
					switch (command) {
					case Chat.CMD_LOGIN:
						if (!Chat.isValidLoginName(user)) {
							log("LOGIN INVALID from " + clientIP);
							continue;
						}
						login = user;
						break;
					
					case Chat.CMD_UCAST:
						if (login == null) {
							Message message= new Message();
							message.setText("Please login first");
							sendObject(CHATMASTER_ID,message);
							continue;
						}
							String recip = textRecieved[2];
							String message = textRecieved[3];
							log("MESG: " + login + "-->" + recip + ": " + message);
							ChatHandler cl = lookup(recip);
							messageReceived.setUserName(recip);
								cl.sendObject(login, messageReceived);
						
					
						break;

					case Chat.CMD_BCAST:
							if (login != null)
							{
								String messageRec = textRecieved[2];
								log("Broadcasting " + login +  ": " + messageRec);

								broadcast(login,messageReceived);}
							else
								log("B<L FROM " + clientIP);
						
						break;

			case Chat.CMD_BLCAST:
						if (login == null) {
							Message errormessage=new Message();
							errormessage.setText("please login first");
							sendObject(CHATMASTER_ID, errormessage );
							continue;
						}
							String block = textRecieved[2];
							 

							ChatHandler cblock = lookup(block);

							blockcast(login, messageReceived, cblock);
						
						break;

					
					default:
						log("Unknown cmd " + command + " from " + login + "@" + clientIP);
					}
				}
			} catch (IOException e) {
				log("IO Exception: " + e);
			}
			catch(ClassNotFoundException e)
			{
				log("Exception");
			}
	
			finally {
				System.out.println(login + SEP + "Logging out");
				synchronized (clientList) {
					clientList.remove(this);
					if (clientList.size() == 0) {
						System.out.println(CHATMASTER_ID + SEP + "No clientList logged in");
					} else if (clientList.size() == 1) {
						ChatHandler last = (ChatHandler) clientList.get(0);
					} else {
						System.out.println(CHATMASTER_ID + SEP + "There are now " + clientList.size() + " users");
					}
				}
			}
		}

		protected void close() {
			if (clientListock == null) {
				log("close when not open");
				return;
			}
			try {
				clientListock.close();
				clientListock = null;
			} catch (IOException e) {
				log("Failure during close to " + clientIP);
			}
		}

		/** Send one message to this user */
		public void sendObject(String sender, Message mesg) {
			mesg.setSenderUserName(sender);
			try{
			objectOutputStream.writeObject(mesg);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}



		public void blockcast(String sender, Message mesg, ChatHandler blocked) {
			for (int i = 0; i < clientList.size(); i++) {
				ChatHandler sib = (ChatHandler) clientList.get(i);
				if (!sib.equals(blocked)) {
					String reciever=sib.getLogin();
					mesg.setUserName(reciever);

					sib.sendObject(sender, mesg);
				}
			}

		}

		public void broadcast(String sender, Message mesg) {
			for (int i = 0; i < clientList.size(); i++) {
				ChatHandler sib = (ChatHandler) clientList.get(i);
				String reciever=sib.getLogin();
				mesg.setUserName(reciever);
				sib.sendObject(sender, mesg);
			}

		}



		protected ChatHandler lookup(String nick) {
			synchronized (clientList) {
				for (int i = 0; i < clientList.size(); i++) {
					ChatHandler cl = (ChatHandler) clientList.get(i);
					if (cl.login.equals(nick))
						return cl;
				}
			}
			return null;
		}

		public String toString() {
			return "ChatHandler[" + login + "]";
		}

		
	}
}
