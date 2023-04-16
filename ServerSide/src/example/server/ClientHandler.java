package example.server;

import com.google.gson.Gson;

import java.io.*;
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
        Outcome outcome = server.processRequest(input);
        Gson gson = new Gson();
        Library lib = null;
        String string = null;
        outcome = Outcome.LOGIN_PASSED;
        if (outcome == Outcome.REGISTRATION_FAILED){
          string = gson.toJson(Outcome.REGISTRATION_FAILED);
        }
        else if (outcome == Outcome.LOGIN_FAILED){
          string = gson.toJson(Outcome.LOGIN_FAILED);
        }
        else if (outcome == Outcome.REGISTRATION_PASSED){
          string = gson.toJson(Outcome.REGISTRATION_PASSED);
        }
        else if (outcome == Outcome.LOGIN_PASSED){
          string = gson.toJson(Outcome.LOGIN_PASSED);
          this.sendToClient(string);
          ArrayList<String> books = new ArrayList<>(Arrays.asList("Harry Potter", "Diary of a Wimpy Kid", "Big Nate", "Catcher in the Rye"));
          lib = new Library(books);
          string = gson.toJson(lib);
        }
        this.sendToClient(string);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void update(Observable o, Object arg) {
    this.sendToClient((String) arg);
  }
}

class Library implements Serializable{
  ArrayList<String> books;
  int numBooks;
  public Library(ArrayList<String> books){
    this.books = books;
    numBooks = books.size();
  }
}

enum InstructionType {
  LOGIN,
  REGISTER,
  CHECKOUT,
  LOGOUT
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
}