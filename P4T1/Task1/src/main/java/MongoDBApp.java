import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.util.Scanner;

public class MongoDBApp {
    // MongoDB connection details
    private static final String CONNECTION_STRING = "mongodb+srv://vchatter:<epicor>@cluster0.effzl.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    private static final String DATABASE_NAME = "football_db";
    private static final String COLLECTION_NAME = "football_collection";

    public static void main(String[] args) {
        System.out.println("Connecting to MongoDB...");

        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            // Get the database and collection
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            System.out.println("Connected to MongoDB successfully!");

            // Prompt the user for a string
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a string to store in MongoDB: ");
            String userInput = scanner.nextLine();
            System.out.println("You entered: " + userInput);

            // Create a document and insert it into the collection
            Document document = new Document("text", userInput)
                    .append("timestamp", System.currentTimeMillis());

            System.out.println("Inserting document into MongoDB...");
            collection.insertOne(document);
            System.out.println("String saved to database successfully!");

            // Read all documents from the collection
            System.out.println("\nRetrieving all strings from the database:");
            System.out.println("-----------------------------");

            FindIterable<Document> documents = collection.find();
            int count = 0;

            for (Document doc : documents) {
                count++;
                String text = doc.getString("text");
                System.out.println(count + ". " + text);
            }

            if (count == 0) {
                System.out.println("No strings found in the database.");
            } else {
                System.out.println("-----------------------------");
                System.out.println("Total documents retrieved: " + count);
            }

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Program completed.");
    }
}