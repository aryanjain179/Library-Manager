package example.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bson.Document;

import javax.print.Doc;
import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observer;
import java.util.Observable;

class ClientHandler implements Runnable, Observer {

  private example.server.Server server;
  private Socket clientSocket;
  private BufferedReader fromClient;
  private PrintWriter toClient;

  protected ClientHandler(example.server.Server server, Socket clientSocket) {
    this.server = server;
    this.clientSocket = clientSocket;
    try {
      fromClient = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
      toClient = new PrintWriter(this.clientSocket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected void sendToClient(String string) {
    System.out.println("Sending to client: " + string);
    toClient.println(string);
    toClient.flush();
  }

  @Override
  public void run() {
    String input;
    try {
      while ((input = fromClient.readLine()) != null) {
        System.out.println("From client: " + input);
        Gson gson = new Gson();
        ArrayList<Outcome> outcome = server.processRequest(input);
        String string = null;
        if (outcome.get(0) == Outcome.LOGIN_PASSED || outcome.get(0) == Outcome.REGISTRATION_PASSED){

          initialData id = new initialData(server.getLibrary(),server.getBooksChecked(input), null);
          string = gson.toJson(id);
          this.sendToClient("A"+string);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void update(Observable o, Object arg) {
    Gson gson = new Gson();

    Document update = (Document) arg;

    String string = gson.toJson(update);

    System.out.println("Sending to client: " + string);
    toClient.println("B"+string);

    toClient.flush();
  }
}

enum InstructionType {
  LOGIN,
  REGISTER,
  CHECKOUT,
  LOGOUT,
  RETURN
}

class Instruction implements Serializable {
  InstructionType instructionType;

  ArrayList<String> instructionParameters;

  public Instruction(InstructionType instructionType, ArrayList<String> instructionParameters){
    this.instructionType = instructionType;
    this.instructionParameters = instructionParameters;
  }

}

enum Outcome {
  REGISTRATION_PASSED,
  REGISTRATION_FAILED,
  LOGIN_PASSED,
  LOGIN_FAILED,
  CHECKOUT_PASSED,
  CHECKOUT_FAILED,
  UPDATE,
  RETURN_PASSED,
  REFRESH_PASSED
}

class initialData implements Serializable{

  ArrayList<Document> initLib;
  ArrayList<String> initCurrentBooks;

  ArrayList<String> initUserHolds;

  public initialData(ArrayList<Document> initLib, ArrayList<String> initCurrentBooks, ArrayList initUserHolds){
    this.initLib = initLib;
    this.initCurrentBooks = initCurrentBooks;
    this.initUserHolds = initUserHolds;
  }

}