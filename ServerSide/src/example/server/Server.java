package example.server;

import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import com.google.gson.Gson;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import com.mongodb.client.MongoCursor;

class Server extends Observable {

  private HashMap<String,String> loginCredentials = new HashMap<String,String>();

  private Object checkoutLock = new Object();


  private static MongoDb mongoDb;

  private static ArrayList<Document> library;

  private static ArrayList<Document> users;

  public static void main(String[] args) {

    mongoDb = new MongoDb();
    mongoDb.connect();
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

  protected synchronized ArrayList<Outcome> processRequest(String input) {

    Gson gson = new Gson();

    ArrayList<Outcome> outcome = new ArrayList<Outcome>();

    Instruction instruction = gson.fromJson(input, Instruction.class);

    if (instruction.instructionType == InstructionType.REGISTER){
      Document filter = new Document("username",instruction.instructionParameters.get(0));
      Document result = mongoDb.users.find(filter).first();

      if (result != null){
        outcome.add(Outcome.REGISTRATION_FAILED);
      }
      else {

        outcome.add(Outcome.REGISTRATION_PASSED);

        Document doc = new Document();
        doc.put("username", instruction.instructionParameters.get(0));
        doc.put("password", instruction.instructionParameters.get(1));
        ArrayList<String> booksChecked = new ArrayList<String>();
        doc.put("booksChecked",booksChecked);
        mongoDb.users.insertOne(doc);
      }
    }
    else if (instruction.instructionType == InstructionType.LOGIN){

      Document filter = new Document("username",instruction.instructionParameters.get(0));
      Document result = mongoDb.users.find(filter).first();
      if (result.get("password").equals(instruction.instructionParameters.get(1))){
        outcome.add(Outcome.LOGIN_PASSED);
      }
      else {
        outcome.add(Outcome.LOGIN_FAILED);
      }
    }

    else if (instruction.instructionType == InstructionType.CHECKOUT){
      //synchronized(checkoutLock){

        for (int i = 1; i < instruction.instructionParameters.size(); i++){
          Document filter = new Document("title",instruction.instructionParameters.get(i));
          Document result = mongoDb.library.find(filter).first();
          if (!result.get("currentUser").equals("")){

            ArrayList<String> holds = (ArrayList<String>) result.getList("holds", String.class);


            holds.add(instruction.instructionParameters.get(0));

            Document update = new Document("$set", new Document("holds", holds));

            mongoDb.library.updateOne(filter,update);

            //Update updateDoc = new Update(instruction.instructionParameters.get(0),result);

            outcome.add(Outcome.CHECKOUT_FAILED);
          }
          else {

            outcome.add(Outcome.CHECKOUT_PASSED);

            Document update = new Document("$set", new Document("currentUser", instruction.instructionParameters.get(0)));

            mongoDb.library.updateOne(filter, update);

            filter = new Document("username", instruction.instructionParameters.get(0));

            result = MongoDb.users.find(filter).first();

            ArrayList<String> booksChecked = (ArrayList<String>) result.getList("booksChecked", String.class);

            booksChecked.add(instruction.instructionParameters.get(i));

            update = new Document("$set", new Document("booksChecked", booksChecked));

            mongoDb.users.updateOne(filter, update);

            filter = new Document("title",instruction.instructionParameters.get(i));

            result = MongoDb.library.find(filter).first();

            this.setChanged();
            this.notifyObservers(result);
          }
        }
      //}
    }


    else if (instruction.instructionType == InstructionType.RETURN){
      for (int i = 1; i < instruction.instructionParameters.size(); i++){


        outcome.add(Outcome.RETURN_PASSED);


        Document filter = new Document("title",instruction.instructionParameters.get(i));

        Document result = MongoDb.library.find(filter).first();

        ArrayList<String> holds = (ArrayList<String>) result.getList("holds", String.class);

        Document update = null;

        if (holds.size() != 0) {
          update = new Document("$set", new Document("currentUser", holds.get(0)));
          mongoDb.library.updateOne(filter, update);

          filter = new Document("username", holds.get(0));

          Document result2 = MongoDb.users.find(filter).first();

          ArrayList<String> booksChecked = (ArrayList<String>) result2.getList("booksChecked", String.class);

          booksChecked.add(instruction.instructionParameters.get(i));

          update = new Document("$set", new Document("booksChecked", booksChecked));

          mongoDb.users.updateOne(filter, update);

          holds.remove(holds.get(0));

          filter = new Document("title",instruction.instructionParameters.get(i));

          update = new Document("$set", new Document("holds", holds));

          mongoDb.library.updateOne(filter,update);
        }

        else {
          update = new Document("$set", new Document("currentUser", ""));
          mongoDb.library.updateOne(filter, update);

        }



        ArrayList<String> pastUsers = (ArrayList<String>) result.getList("pastUsers", String.class);

        pastUsers.add(instruction.instructionParameters.get(0));

        update = new Document("$set", new Document("pastUsers", pastUsers));

        mongoDb.library.updateOne(filter, update);




        filter = new Document("username", instruction.instructionParameters.get(0));

        result = MongoDb.users.find(filter).first();

        ArrayList<String> booksChecked = (ArrayList<String>) result.getList("booksChecked", String.class);

        booksChecked.remove(instruction.instructionParameters.get(i));
        update = new Document("$set", new Document("booksChecked", booksChecked));

        mongoDb.users.updateOne(filter, update);

        filter = new Document("title",instruction.instructionParameters.get(i));

        result = MongoDb.library.find(filter).first();

        this.setChanged();
        this.notifyObservers(result);

      }
    }

    return outcome;
  }

  public ArrayList<Document> getLibrary(){
    MongoCursor<Document> cursor = mongoDb.library.find().iterator();
    library = new ArrayList<Document>();
    while (cursor.hasNext()){
      library.add(cursor.next());
    }
    return this.library;
  }

  public ArrayList<String> getBooksChecked(String input){

    Gson gson = new Gson();
    Instruction instruction = gson.fromJson(input, Instruction.class);

    Document filter = new Document("username", instruction.instructionParameters.get(0));

    Document result = MongoDb.users.find(filter).first();

    ArrayList<String> booksChecked = (ArrayList<String>) result.getList("booksChecked", String.class);

    return booksChecked;

  }

}