// Name: Vishal Chatterjee
// AndrewID: vchatter

package ds.demoservlet;

import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.bson.Document;

import java.io.IOException;
import java.util.*;

@WebServlet("/dashboard") // Servlet mapped to /dashboard endpoint
public class Dashboard extends HttpServlet {

    // MongoDB connection URI — update this securely in production
    private static final String URI = "mongodb+srv://vchatter:vchatter12345@cluster-task4.tqhcj1k.mongodb.net/?retryWrites=true&w=majority&appName=Cluster-task4";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Try-with-resources to ensure MongoClient closes automatically
        try (MongoClient client = MongoClients.create(URI)) {

            // Access the football database and logs collection
            MongoDatabase db = client.getDatabase("football");
            MongoCollection<Document> logs = db.getCollection("logs");

            // =========================
            // 1. Most Frequently Queried Competition
            // =========================
            Document topCompetition = logs.aggregate(Arrays.asList(
                    Aggregates.group("$competition", Accumulators.sum("count", 1)), // Group by competition
                    Aggregates.sort(Sorts.descending("count")), // Sort by count descending
                    Aggregates.limit(1) // Limit to top 1
            )).first();

            // Set as request attribute for use in dashboard.jsp
            req.setAttribute("topCompetition", topCompetition != null ? topCompetition.getString("_id") : "N/A");

            // =========================
            // 2. Most Common Query Type (matches, teams, topscorers)
            // =========================
            Document topType = logs.aggregate(Arrays.asList(
                    Aggregates.group("$type", Accumulators.sum("count", 1)), // Group by query type
                    Aggregates.sort(Sorts.descending("count")),
                    Aggregates.limit(1)
            )).first();

            req.setAttribute("topType", topType != null ? topType.getString("_id") : "N/A");

            // =========================
            // 3. Last 5 Query Logs (for recent activity section)
            // =========================
            List<Document> recentLogs = logs.find()
                    .sort(Sorts.descending("timestamp")) // Sort logs by latest timestamp
                    .limit(5) // Limit to last 5 logs
                    .into(new ArrayList<>());

            req.setAttribute("recentLogs", recentLogs);

            // =========================
            // 4. Average Result Count per Query Type
            // =========================
            List<Document> avgResultsByType = logs.aggregate(Arrays.asList(
                    Aggregates.group("$type", Accumulators.avg("averageResults", "$resultCount")) // Calculate average resultCount by type
            )).into(new ArrayList<>());

            req.setAttribute("avgResultsByType", avgResultsByType);

            // =========================
            // 5. Pie Chart Data by Competition Frequency
            // =========================
            Map<String, Integer> pieChart = new LinkedHashMap<>();

            List<Document> pieData = logs.aggregate(Arrays.asList(
                    Aggregates.group("$competition", Accumulators.sum("count", 1)), // Count per competition
                    Aggregates.sort(Sorts.descending("count"))
            )).into(new ArrayList<>());

            for (Document d : pieData) {
                pieChart.put(d.getString("_id"), d.getInteger("count"));
            }

            req.setAttribute("pieChart", pieChart);

            // =========================
            // Forward to JSP for rendering the dashboard
            // =========================
            req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            // Handle any connection or aggregation errors
            resp.getWriter().write("Dashboard error: " + e.getMessage());
        }
    }
}
