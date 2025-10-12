package org.politechnika.io;

import org.politechnika.model.Instance;
import org.politechnika.model.Node;
import org.politechnika.util.DistanceCalculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class InstanceReader {

    public static Instance readInstance(String fileName) throws IOException {
        String resourcePath = "/instances/" + fileName;
        InputStream inputStream = InstanceReader.class.getResourceAsStream(resourcePath);
        
        if (inputStream == null) {
            throw new IOException("File not found: " + resourcePath);
        }

        List<Node> nodes = new ArrayList<>();
        int nodeId = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length != 3) {
                    throw new IOException("Invalid line format: " + line);
                }

                double x = Double.parseDouble(parts[0].trim());
                double y = Double.parseDouble(parts[1].trim());
                int cost = Integer.parseInt(parts[2].trim());

                nodes.add(new Node(nodeId++, x, y, cost));
            }
        }

        if (nodes.isEmpty()) {
            throw new IOException("No nodes found in file: " + fileName);
        }

        int[][] distanceMatrix = DistanceCalculator.buildDistanceMatrix(nodes);

        String instanceName = fileName.replaceFirst("[.][^.]+$", "");

        return new Instance(instanceName, nodes, distanceMatrix);
    }
}
