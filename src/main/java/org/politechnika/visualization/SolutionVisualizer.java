package org.politechnika.visualization;

import org.politechnika.model.Instance;
import org.politechnika.model.Node;
import org.politechnika.model.Solution;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;


public class SolutionVisualizer extends JPanel {

    private final Instance instance;
    private final Solution solution;
    private static final int PADDING = 50;
    private static final int NODE_SIZE = 8;

    public SolutionVisualizer(Instance instance, Solution solution) {
        this.instance = instance;
        this.solution = solution;
        setPreferredSize(new Dimension(800, 800));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for (Node node : instance.getNodes()) {
            minX = Math.min(minX, node.getX());
            maxX = Math.max(maxX, node.getX());
            minY = Math.min(minY, node.getY());
            maxY = Math.max(maxY, node.getY());
        }

        double rangeX = maxX - minX;
        double rangeY = maxY - minY;
        double scale = Math.min((getWidth() - 2 * PADDING) / rangeX,
                               (getHeight() - 2 * PADDING) / rangeY);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(instance.getName() + " - " + solution.getAlgorithmName(), 10, 20);
        g2d.drawString(String.format("Objective: %.2f", solution.getObjectiveValue()), 10, 40);

        int maxCost = instance.getNodes().stream()
                .mapToInt(Node::getCost)
                .max()
                .orElse(1);

        List<Integer> selectedNodes = solution.getNodeIds();
        for (Node node : instance.getNodes()) {
            double x = PADDING + (node.getX() - minX) * scale;
            double y = getHeight() - PADDING - (node.getY() - minY) * scale;
            float ratio = (float) node.getCost() / maxCost;
            Color nodeColor = new Color(ratio, 1 - ratio, 0);

            g2d.setColor(nodeColor);
            Ellipse2D circle = new Ellipse2D.Double(x - NODE_SIZE / 2.0, y - NODE_SIZE / 2.0,
                    NODE_SIZE, NODE_SIZE);
            g2d.fill(circle);
//            if (!selectedNodes.contains(node.getId())) {
//                g2d.setColor(new Color(220, 220, 220));
//                Ellipse2D circle = new Ellipse2D.Double(x - NODE_SIZE / 2.0, y - NODE_SIZE / 2.0,
//                        NODE_SIZE, NODE_SIZE);
//                g2d.fill(circle);
//            }
        }

        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < selectedNodes.size(); i++) {
            int nodeId1 = selectedNodes.get(i);
            int nodeId2 = selectedNodes.get((i + 1) % selectedNodes.size());

            Node node1 = instance.getNode(nodeId1);
            Node node2 = instance.getNode(nodeId2);

            double x1 = PADDING + (node1.getX() - minX) * scale;
            double y1 = getHeight() - PADDING - (node1.getY() - minY) * scale;
            double x2 = PADDING + (node2.getX() - minX) * scale;
            double y2 = getHeight() - PADDING - (node2.getY() - minY) * scale;

            g2d.setColor(new Color(50, 150, 255, 180));
            g2d.draw(new Line2D.Double(x1, y1, x2, y2));
        }

        for (int nodeId : selectedNodes) {
            Node node = instance.getNode(nodeId);
            double x = PADDING + (node.getX() - minX) * scale;
            double y = getHeight() - PADDING - (node.getY() - minY) * scale;



            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 9));
            g2d.drawString(String.valueOf(nodeId), (int) x + 5, (int) y - 5);
        }

        drawLegend(g2d, maxCost);
    }

    private void drawLegend(Graphics2D g2d, int maxCost) {
        int barWidth = 150;
        int barHeight = 20;
        int legendX = getWidth() - barWidth - 20;
        int legendY = 60;

        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(Color.BLACK);
        g2d.drawString("Node Cost:", legendX, legendY);

        for (int i = 0; i < barWidth; i++) {
            float ratio = (float) i / barWidth;
            Color color = new Color(ratio, 1 - ratio, 0);
            g2d.setColor(color);
            g2d.drawLine(legendX + i, legendY + 5, legendX + i, legendY + 5 + barHeight);
        }

        g2d.setColor(Color.BLACK);
        g2d.drawRect(legendX, legendY + 5, barWidth, barHeight);
        g2d.drawString("0", legendX, legendY + 40);
        g2d.drawString(String.valueOf(maxCost), legendX + barWidth - 20, legendY + 40);
    }

    public static void show(Instance instance, Solution solution) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TSP Solution - " + solution.getAlgorithmName());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new SolutionVisualizer(instance, solution));
            frame.pack();

            int offset = (int) (Math.random() * 100 + 50);
            frame.setLocation(100 + offset, 100 + offset);
            
            frame.setVisible(true);

            frame.toFront();
            frame.repaint();
        });

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void saveToFile(Instance instance, Solution solution, String fileName) {
        SolutionVisualizer visualizer = new SolutionVisualizer(instance, solution);
        visualizer.setSize(800, 800);

        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                800, 800, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        visualizer.paint(g2d);
        g2d.dispose();

        try {
            javax.imageio.ImageIO.write(image, "PNG", new java.io.File(fileName));
            System.out.println("  Visualization saved to: " + fileName);
        } catch (Exception e) {
            System.err.println("  Error saving visualization: " + e.getMessage());
        }
    }
}
