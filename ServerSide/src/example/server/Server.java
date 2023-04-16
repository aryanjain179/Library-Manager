package example.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import com.google.gson.Gson;

class Server extends Observable {

  private HashMap<String,String> loginCredentials = new HashMap<String,String>();

  public static void main(String[] args) {
    new Server().runServer();
  }

  private void runServer() {
    try {
      setUpNetworking();
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }

  private void setUpNetworking() throws Exception {
    @SuppressWarnings("resource")
    ServerSocket serverSock = new ServerSocket(4242);
    while (true) {
      Socket clientSocket = serverSock.accept();
      System.out.println("Connecting to... " + clientSocket);
      ClientHandler handler = new ClientHandler(this, clientSocket);
      this.addObserver(handler);
      Thread t = new Thread(handler);
      t.start();
    }
  }

  protected Outcome processRequest(String input) {
    /*String output = "Error";
    Gson gson = new Gson();
    Message message = gson.fromJson(input, Message.class);
    try {
      String temp = "";
      switch (message.type) {
        case "upper":
          temp = message.input.toUpperCase();
          break;
        case "lower":
          temp = message.input.toLowerCase();
          break;
        case "strip":
          temp = message.input.replace(" ", "");
          break;
      }
      output = "";
      for (int i = 0; i < message.number; i++) {
        output += temp;
        output += " ";
      }
      this.setChanged();
      this.notifyObservers(output);
    } catch (Exception e) {
      e.printStackTrace();
    }*/

    Gson gson = new Gson();
    Instruction instruction = gson.fromJson(input, Instruction.class);

    Outcome outcome = null;
    if (instruction.instructionType == InstructionType.REGISTER){
      loginCredentials.put(instruction.instructionParameters.get(0),instruction.instructionParameters.get(1));
      outcome = Outcome.REGISTRATION_PASSED;
    }
    else if (instruction.instructionType == InstructionType.LOGIN){
      for ( String username : loginCredentials.keySet()){
        if (username == instruction.instructionParameters.get(0)){
          if (loginCredentials.get(username) == instruction.instructionParameters.get(0)){
            outcome = Outcome.LOGIN_PASSED;
            return outcome;
          }
        }
      }
      outcome = Outcome.LOGIN_FAILED;
    }

    return outcome;

  }

}