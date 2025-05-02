<%@ page import="java.util.Map" %>
<%@ page import="org.bson.Document" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Football Data Dashboard</title>
    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            padding: 30px;
        }
        h2 {
            color: #2c3e50;
        }
        table {
            border-collapse: collapse;
            width: 80%;
            margin-bottom: 30px;
        }
        th, td {
            border: 1px solid #ccc;
            padding: 12px;
            text-align: left;
        }
        th {
            background-color: #f4f4f4;
        }
    </style>

    <script type="text/javascript">
        google.charts.load("current", {packages:["corechart"]});
        google.charts.setOnLoadCallback(drawChart);

        function drawChart() {
            const data = new google.visualization.DataTable();
            data.addColumn('string', 'Competition');
            data.addColumn('number', 'Count');

            <% Map<String, Integer> pieChart = (Map<String, Integer>) request.getAttribute("pieChart");
               for (Map.Entry<String, Integer> entry : pieChart.entrySet()) {
            %>
            data.addRow(['<%= entry.getKey() %>', <%= entry.getValue() %>]);
            <% } %>

            const options = {
                title: 'Query Distribution by Competition',
                pieHole: 0.4,
                width: 600,
                height: 400
            };

            const chart = new google.visualization.PieChart(document.getElementById('piechart'));
            chart.draw(data, options);
        }
    </script>
</head>
<body>

<h1>Football Data Dashboard</h1>

<h2>Top Queried Competition: <%= request.getAttribute("topCompetition") %></h2>
<h2>Most Common Query Type: <%= request.getAttribute("topType") %></h2>

<h2>Recent Queries</h2>
<table>
    <tr>
        <th>Competition</th>
        <th>Type</th>
        <th>Competition ID</th>
        <th>Results</th>
        <th>Timestamp</th>
    </tr>
    <%
        List<Document> recentLogs = (List<org.bson.Document>) request.getAttribute("recentLogs");
        for (org.bson.Document doc : recentLogs) {
    %>
    <tr>
        <td><%= doc.getString("competition") %></td>
        <td><%= doc.getString("type") %></td>
        <td><%= doc.getInteger("competitionId", -1) %></td>
        <td><%= doc.getInteger("resultCount", 0) %></td>
        <td><%= new java.util.Date(doc.getLong("timestamp")) %></td>
    </tr>
    <% } %>
</table>

<h2>Average Results Per Query Type</h2>
<table>
    <tr>
        <th>Type</th>
        <th>Average Result Count</th>
    </tr>
    <%
        List<org.bson.Document> avgResultsByType = (List<org.bson.Document>) request.getAttribute("avgResultsByType");
        for (org.bson.Document row : avgResultsByType) {
    %>
    <tr>
        <td><%= row.getString("_id") %></td>
        <td><%= String.format("%.2f", row.getDouble("averageResults")) %></td>
    </tr>
    <% } %>
</table>

<h2>Query Distribution</h2>
<div id="piechart"></div>

</body>
</html>
