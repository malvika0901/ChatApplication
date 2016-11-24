


class Chat {


  public static final int PORTNUM = 9999;
  public static final int MAX_LOGIN_LENGTH = 20;
  public static final char SEPARATOR = '\\';
  public static final char COMMAND = '\\';
  public static final String CMD_LOGIN = "Login";
  public static final String CMD_QUIT  = "Quit";
  public static final String CMD_UCAST  = "Unicast";
  public static final String CMD_BCAST = "Broadcast";
  public static final String CMD_BLCAST = "Blockcast";


  

  public static boolean isValidLoginName(String login) {
	    if (login.length() > MAX_LOGIN_LENGTH)
	      return false;

	       return true;
	  }

}
  

