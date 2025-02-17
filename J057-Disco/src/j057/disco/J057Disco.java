/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package j057.disco;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.util.ArrayList;
import java.util.List;

public class J057Disco {

    public static void main(String[] args) {
        File rootFile = new File("C:\\");
        FileNode rootNode = new FileNode(rootFile.getName(), true);
        traverseDirectory(rootFile.toPath(), rootNode);

        String json = toJson(rootNode);
        try {
            Files.write(Paths.get("filesystem.json"), json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void traverseDirectory(Path path, FileNode node) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!path.equals(dir)) {
                        FileNode childNode = new FileNode(dir.getFileName().toString(), true);
                        node.addChild(childNode);
                        traverseDirectory(dir, childNode);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    FileNode childNode = new FileNode(file.getFileName().toString(), false);
                    childNode.setSize(attrs.size());
                    node.addChild(childNode);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    // Handle the case where access is denied or another I/O error occurs
                    System.err.println("Access denied or I/O error: " + file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String toJson(FileNode node) {
        StringBuilder json = new StringBuilder();
        json.append("{")
            .append("\"name\":\"").append(node.getName()).append("\",")
            .append("\"isDirectory\":").append(node.isDirectory()).append(",")
            .append("\"size\":").append(node.getSize()).append(",")
            .append("\"children\":[");

        for (int i = 0; i < node.getChildren().size(); i++) {
            json.append(toJson(node.getChildren().get(i)));
            if (i < node.getChildren().size() - 1) {
                json.append(",");
            }
        }

        json.append("]")
            .append("}");

        return json.toString();
    }
}

class FileNode {
    private String name;
    private boolean isDirectory;
    private long size;
    private List<FileNode> children;

    public FileNode(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.size = 0;
        this.children = new ArrayList<>();
    }

    public void addChild(FileNode child) {
        children.add(child);
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public long getSize() {
        return size;
    }

    public List<FileNode> getChildren() {
        return children;
    }
}
