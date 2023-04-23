package example.client;

import java.io.Serializable;
import java.net.Socket;

import com.google.gson.Gson;

import org.bson.Document;

import java.io.*;
import java.lang.reflect.Type;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Client extends Application {

  private String host = "127.0.0.1";
  private static BufferedReader fromServer;
  private static PrintWriter toServer;
  private Scanner consoleInput = new Scanner(System.in);

  @FXML
  private static Stage applicationStage;

  @FXML
  private TextField username;

  @FXML
  private TextField password;

  @FXML
  private MenuButton booksAvailable;

  @FXML
  private TextArea selected;

  @FXML
  private Scene scene;

  @FXML
  private VBox vBox;

  @FXML
  private ScrollPane scrollPane;

  @FXML
  private MenuButton currentBooksMenu;

  @FXML
  private TextArea current;


  @FXML
  private MenuButton booksAvailableR;

  @FXML
  private TextArea selectedR;

  @FXML
  private TextField search;

  @FXML
  private TextArea holdsDisplay;

  private static String usernameString;

  private static String passwordString;

  private InstructionType instructionType;

  private static ArrayList<String> booksSelectedCheckouts;

  private static ArrayList<Library> lib;

  private static ArrayList<String> currentBooks;

  private static ArrayList<TextArea> textAreas;

  private static ArrayList<String> booksSelectedReturns;

  private static ArrayList<String> userHolds;

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

    readerThread.start();
  }

  @FXML
  protected void processRequest(String input) {
    Gson gson = new Gson();

    if (input.charAt(0) == 'A') {
      initialData id = gson.fromJson(input.substring(1), initialData.class);

      ArrayList<Document> libDocs = id.lib;

      String string = gson.toJson(libDocs);
      Type libraryType = new TypeToken<ArrayList<Library>>() {}.getType();

      lib = gson.fromJson(string, libraryType);
      currentBooks = id.temp;
      userHolds = null;
      libraryLauncher();

    }

    else if (input.charAt(0) == 'B'){
      Library book = gson.fromJson(input.substring(1),Library.class);

      for (int i = 0; i < lib.size(); i++){
        if (lib.get(i).title.equals(book.title)){
          lib.set(i,book);
          TextArea textArea = textAreas.get(i);
          int finalI = i;
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              textArea.clear();
              textArea.appendText("TITLE: " + lib.get(finalI).title + "\n" + "AUTHOR: " + lib.get(finalI).author + "\n" + "PAGES: " + lib.get(finalI).pages + "\n" + "SUMMARY: " + lib.get(finalI).summary + "\n" + "CURRENT USER: " + lib.get(finalI).currentUser + "\n" + "PAST USERS: " + lib.get(finalI).pastUsers.toString() + "\n" + "REVIEWS: " + lib.get(finalI).reviews.toString() + "\n" + "HOLDS: " + lib.get(finalI).holds.toString() + "\n\n");

              booksAvailable.getItems().removeAll();
              booksAvailableR.getItems().removeAll();
              currentBooksMenu.getItems().removeAll();

              selected.clear();
              selectedR.clear();
              current.clear();
              holdsDisplay.clear();

              for (int i = 0; i < lib.size();i++){
                if(lib.get(i).currentUser.equals(usernameString)) {
                  MenuItem returnBook = new MenuItem(lib.get(i).title);
                  booksAvailableR.getItems().add(returnBook);
                  MenuItem reviewBook = new MenuItem(lib.get(i).title);
                  currentBooksMenu.getItems().add(reviewBook);

                  current.appendText(lib.get(i).title);

                  int finalI = i;
                  EventHandler<ActionEvent> returnEvent = new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                      bookSelectedR(lib.get(finalI).title);
                    }
                  };
                  returnBook.setOnAction(returnEvent);

                  EventHandler<ActionEvent> reviewEvent = new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                      //
                    }
                  };
                  reviewBook.setOnAction(reviewEvent);
                }
                else if (!lib.get(i).currentUser.equals(usernameString) && !lib.get(i).holds.contains(usernameString)){
                  MenuItem checkoutBook = new MenuItem(lib.get(i).title);

                  int finalI = i;
                  EventHandler<ActionEvent> checkoutEvent = new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                      bookSelected(lib.get(finalI).title);
                    }
                  };
                  checkoutBook.setOnAction(checkoutEvent);
                }
                if (lib.get(i).holds.contains(usernameString)){
                  holdsDisplay.appendText(lib.get(i).title);
                }


              }
            }
          });
        }
      }
    }

  }
  @FXML
  public void libraryLauncher(){
    Parent root = null;
    try {
      root = FXMLLoader.load(getClass().getResource("test.fxml"));
    }
    catch (IOException e) {
    }



    scene = new Scene(root);

    scrollPane = (ScrollPane) scene.lookup("#scrollPane");
    vBox = (VBox) scrollPane.getContent();
    search = (TextField) scene.lookup("#search");

    selected = (TextArea) scene.lookup("#selected");
    booksAvailable = (MenuButton) scene.lookup("#booksAvailable");
    current = (TextArea) scene.lookup("#current");
    currentBooksMenu = (MenuButton) scene.lookup("#currentBooksMenu");

    booksAvailableR = (MenuButton) scene.lookup("#booksAvailableR");
    selectedR = (TextArea) scene.lookup("#selectedR");

    Platform.runLater(new Runnable() {
      @Override
      public void run() {

        booksAvailable.getItems().clear();

        applicationStage.setScene(scene);
        applicationStage.show();

        booksSelectedCheckouts = new ArrayList<String>();

        booksSelectedReturns = new ArrayList<String>();

        ArrayList<TextArea> textAreasTemp = new ArrayList<TextArea>();

        booksAvailableR.getItems().clear();
        for (int i = 0; i < currentBooks.size(); i++){
          MenuItem m = new MenuItem(currentBooks.get(i));
          booksAvailableR.getItems().add(m);
          int finalI = i;
          EventHandler<ActionEvent> event3 = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
              bookSelectedR(currentBooks.get(finalI));
            }
          };
          m.setOnAction(event3);

        }


        currentBooksMenu.getItems().clear();
        for (int i = 0; i < currentBooks.size(); i++){
          current.appendText(currentBooks.get(i)+"\n");

          MenuItem m = new MenuItem(currentBooks.get(i));
          currentBooksMenu.getItems().add(m);
          EventHandler<ActionEvent> event2 = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
              //bookSelected(lib.get(finalI).title);
            }
          };
          m.setOnAction(event2);
        }

        for (int i = 0; i < lib.size(); i++){


          HBox bookBox = new HBox();

          bookBox.setMinSize(300,300);

          TextArea ta = new TextArea();

          textAreasTemp.add(ta);

          ta.appendText("TITLE: " + lib.get(i).title + "\n" + "AUTHOR: " + lib.get(i).author + "\n" + "PAGES: " + lib.get(i).pages + "\n" + "SUMMARY: " + lib.get(i).summary + "\n" + "CURRENT USER: " + lib.get(i).currentUser + "\n" + "PAST USERS: " + lib.get(i).pastUsers.toString() + "\n" + "REVIEWS: " + lib.get(i).reviews.toString() + "\n" + "HOLDS: " + lib.get(i).holds.toString() + "\n\n");

          // Create an ImageView for the book image and add it to the HBox
          if (lib.get(i).title.equals("Harry Potter")) {
            Image image = new Image(lib.get(i).image);
            ImageView imageView = new ImageView(image);
            bookBox.getChildren().addAll(imageView, ta);
          }
          else {
            bookBox.getChildren().add(ta);
          }

          // Add the book section to the VBox
          vBox.getChildren().add(bookBox);

          if (!currentBooks.contains(lib.get(i).title) && !userHolds.contains(lib.get(i).title)){

            MenuItem m = new MenuItem(lib.get(i).title);
            booksAvailable.getItems().add(m);
            int finalI = i;
            EventHandler<ActionEvent> event1 = new EventHandler<ActionEvent>() {
              public void handle(ActionEvent e) {
                bookSelected(lib.get(finalI).title);
              }
            };
            m.setOnAction(event1);
          }

        }
        textAreas = textAreasTemp;
      }
    });
  }

  @FXML
  public void searchClicked(){

    //ArrayList<TextArea> textAreasTemp = new ArrayList<TextArea>();
    vBox.getChildren().clear();
    for (int i = 0; i < textAreas.size(); i++) {
      if (textAreas.get(i).getText().contains(search.getText())){
        HBox bookBox = new HBox();
        bookBox.setMinSize(300,300);
        bookBox.getChildren().add(textAreas.get(i));
        vBox.getChildren().add(bookBox);
      }
    }
  }
  @FXML
  public void bookSelectedR(String book){
    if (booksSelectedReturns.size() == 0){
      selectedR.clear();
    }
    if (!booksSelectedReturns.contains(book)){
      booksSelectedReturns.add(book);
      selectedR.appendText(book+"\n");
    }
  }

  @FXML
  public void returnBooks(){
    ArrayList<String> temp = new ArrayList<>(booksSelectedReturns);
    temp.add(0,usernameString);
    Instruction instruction = new Instruction(InstructionType.RETURN,temp);
    sendToServer(instruction);
  }

  @FXML
  public void bookSelected(String book){
    if (booksSelectedCheckouts.size() == 0){
      selected.clear();
      //checkouts.clear();
    }
    if (!booksSelectedCheckouts.contains(book)){
      booksSelectedCheckouts.add(book);
      selected.appendText(book+"\n");
    }
  }

  @FXML
  public void checkoutBooks(){
    ArrayList<String> temp = new ArrayList<>(booksSelectedCheckouts);
    temp.add(0,usernameString);
    Instruction instruction = new Instruction(InstructionType.CHECKOUT,temp);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        sendToServer(instruction);
      }
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

    usernameString = username.getText();
    passwordString = password.getText();

    try {
      Signature sign = Signature.getInstance("SHA256withRSA");
    }
    catch (NoSuchAlgorithmException e) {

    }

    //Creating KeyPair generator object
    KeyPairGenerator keyPairGen = null;
    try {
      keyPairGen = KeyPairGenerator.getInstance("RSA");
    }
    catch (NoSuchAlgorithmException e) {
    }

    //Initializing the key pair generator
    keyPairGen.initialize(2048);

    //Generate the pair of keys
    KeyPair pair = keyPairGen.generateKeyPair();

    //Getting the public key from the key pair
    PublicKey publicKey = pair.getPublic();

    //Creating a Cipher object
    Cipher cipher = null;
    try {
      cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    }
    catch (NoSuchAlgorithmException e) {
    }
    catch (NoSuchPaddingException e) {
    }

    //Initializing a Cipher object
    try {
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    }
    catch (InvalidKeyException e) {
    }

    //Add data to the cipher
    byte[] input = passwordString.getBytes();
    cipher.update(input);

    //encrypting the data
    byte[] cipherText = new byte[0];
    try {
      cipherText = cipher.doFinal();
    }
    catch (IllegalBlockSizeException e) {
    }
    catch (BadPaddingException e) {
    }

    try {
      System.out.println( new String(cipherText, "UTF8"));
    }
    catch (UnsupportedEncodingException e) {
    }

    //Initializing the same cipher for decryption
    try {
      cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
    }
    catch (InvalidKeyException e) {
    }


    //Decrypting the text
    byte[] decipheredText = null;
    try {
      decipheredText = cipher.doFinal(cipherText);
    }
    catch (IllegalBlockSizeException e) {
    }
    catch (BadPaddingException e) {
    }

    instructionType = InstructionType.REGISTER;

    ArrayList<String> instructionParameters = new ArrayList<>(Arrays.asList(usernameString,passwordString));
    Instruction instruction = new Instruction(instructionType, instructionParameters);
    sendToServer(instruction);
    instructionType = null;
  }

  @FXML
  public void refreshLibrary(){
    System.out.println("Sending to server: " + "REFRESH");
    toServer.println("REFRESH");
    toServer.flush();
  }

  @FXML
  public void loginHandler(){
    usernameString = username.getText();
    passwordString = password.getText();

    instructionType = InstructionType.LOGIN;

    ArrayList<String> instructionParameters = new ArrayList<>(Arrays.asList(usernameString,passwordString));
    Instruction instruction = new Instruction(instructionType, instructionParameters);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        sendToServer(instruction);
        instructionType = null;
      }
    });
  }


}
class Library implements Serializable {

  String title;

  String author;

  String pages;

  String summary;

  String image;

  String currentUser;

  ArrayList<String> pastUsers;

  ArrayList<String> reviews;

  ArrayList<String> holds;
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

class initialData implements Serializable{

  ArrayList<Document> lib;
  ArrayList<String> temp;

  public initialData(ArrayList<Document> lib, ArrayList<String> temp){
    this.lib = lib;
    this.temp = temp;
  }

}

