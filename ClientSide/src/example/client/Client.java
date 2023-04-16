package example.client;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.net.Socket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Client extends Application {

  private static String host = "127.0.0.1";
  private static BufferedReader fromServer;
  private static PrintWriter toServer;
  private Scanner consoleInput = new Scanner(System.in);

  @FXML
  private static Stage applicationStage;
  @FXML
  private static TextArea libraryDisplay;

  @FXML
  private TextField username;

  @FXML
  private TextField password;

  private static String usernameString;

  private static String passwordString;

  private static InstructionType instructionType;

  public static void main(String[] args) {
    launch(args);
  }

  private void setUpNetworking() throws Exception {
    @SuppressWarnings("resource")
    Socket socket = new Socket(host, 4242);
    System.out.println("Connecting to... " + socket);
    fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    toServer = new PrintWriter(socket.getOutputStream());

    Thread readerThread = new Thread(new Runnable() {
      @Override
      public void run() {
        String input;
        try {
          while ((input = fromServer.readLine()) != null) {
            System.out.println("From server: " + input);
            processRequest(input);
          }
        }
        catch (Exception e) {
        }
      }
    });

    /*Thread writerThread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          if (instructionType == InstructionType.REGISTER) {
            ArrayList<String> instructionParameters = new ArrayList<>(Arrays.asList(usernameString,passwordString));
            Instruction instruction = new Instruction(instructionType, instructionParameters);
            sendToServer(instruction);
            instructionType = null;
          }
        }
      }
    });*/

    readerThread.start();
    //writerThread.start();
  }

  protected void processRequest(String input) {
    Gson gson = new Gson();
    Outcome outcome = gson.fromJson(input,Outcome.class);
    if (outcome == Outcome.LOGIN_PASSED){
      try {
        input = fromServer.readLine();
      }
      catch (IOException e) {
      }
      Library lib = gson.fromJson(input,Library.class);
      libraryLauncher(lib);
    }
  }

  @FXML
  public void libraryLauncher(Library lib){
    Parent root = null;
    try {
      root = FXMLLoader.load(getClass().getResource("Library.fxml"));
    }
    catch (IOException e) {
    }

    Scene scene = new Scene(root);
    libraryDisplay = (TextArea) scene.lookup("#libraryDisplay");

    // Update the UI on the JavaFX Application Thread
    Platform.runLater(() -> {
      libraryDisplay.setText(lib.books.toString());
      applicationStage.setScene(scene);
      applicationStage.show();
    });
  }

  protected void sendToServer(Instruction instruction) {
    Gson gson = new Gson();
    String string = gson.toJson(instruction);
    System.out.println("Sending to server: " + string);
    toServer.println(string);
    toServer.flush();
  }

  @Override
  public void start(Stage applicationStage) {
    this.applicationStage = applicationStage;
    Parent root = null;
    try {
      root = FXMLLoader.load(getClass().getResource("Login.fxml"));
    }
    catch (IOException e) {
    }
    Scene scene = new Scene(root);
    applicationStage.setScene(scene);
    applicationStage.show();
    try {
      setUpNetworking();
    }
    catch (Exception e) {
    }
  }

  @FXML
  public void registerHandler(){
    Parent root = null;
    try {
      root = FXMLLoader.load(getClass().getResource("Library.fxml"));
    }
    catch (IOException e) {
    }

    usernameString = username.getText();
    passwordString = password.getText();
    instructionType = InstructionType.REGISTER;

    /*Scene scene = new Scene(root);
    applicationStage.setScene(scene);
    applicationStage.show();

    libraryDisplay = (TextArea) scene.lookup("#libraryDisplay");*/

    ArrayList<String> instructionParameters = new ArrayList<>(Arrays.asList(usernameString,passwordString));
    Instruction instruction = new Instruction(instructionType, instructionParameters);
    sendToServer(instruction);
    instructionType = null;
  }

  @FXML
  public void loginHandler(){
    Parent root = null;
    try {
      root = FXMLLoader.load(getClass().getResource("Library.fxml"));
    }
    catch (IOException e) {
    }

    usernameString = username.getText();
    passwordString = password.getText();
    instructionType = InstructionType.LOGIN;

    /*Scene scene = new Scene(root);
    applicationStage.setScene(scene);
    applicationStage.show();

    libraryDisplay = (TextArea) scene.lookup("#libraryDisplay");*/

    ArrayList<String> instructionParameters = new ArrayList<>(Arrays.asList(usernameString,passwordString));
    Instruction instruction = new Instruction(instructionType, instructionParameters);
    sendToServer(instruction);
    instructionType = null;
  }


}
class Library implements Serializable {
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