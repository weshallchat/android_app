
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.Scanner;

public class MongoApp {
    public static void main(String[] args) {
        // Replace with your MongoDB Atlas connection string
        String uri = "mongodb+srv://vchatter:a4w7myTFNHY3SW5k@cluster0.gf5r4.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

        // Connect to MongoDB Atlas using the MongoDB Java driver
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            // Create (or get) the database
            MongoDatabase database = mongoClient.getDatabase("LaptopPurchaseAssistantDB");

            // Create (or get) the collection
            MongoCollection<Document> collection = database.getCollection("UserStrings");

            // Prompt the user for a string
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a string to store in MongoDB: ");
            String userInput = scanner.nextLine();

            // Create a document containing the user input
            Document doc = new Document("userString", userInput);
            collection.insertOne(doc);
            System.out.println("Inserted document: " + doc.toJson());

            // Read all documents currently stored in the collection
            System.out.println("Reading all documents from the collection:");
            for (Document document : collection.find()) {
                // Extract and print the "userString" field from each document
                System.out.println(document.getString("userString"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}