
/* 
 * Server.java 
 * 
 * Version: 1.1
 *     
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.HashMap;
import java.util.Random;

/**
 * Server class has methods for file insertion, file look up and replication
 * when the file is requested multiple times.
 *
 * @author Shweta Yakkali
 */
public class Server {

    protected static String server_names[] = {"glados", "medusa", "doors", "rhea", "yes", "comet", "queeg"};
    protected static String server_IP;
    protected static HashMap<String, Integer> file_map = new HashMap<String, Integer>();
    protected static int threshold1 = 5;
    protected static int threshold2 = 10;

    /**
     * The main() method accepts the option which the client wants to do i.e.
     * either insertion or lookup and chooses that option and performs the
     * required processing.
     *
     */
    public static void main(String[] args) throws IOException {

        int server_Port = 5554;

        ServerSocket serverSocket = new ServerSocket(server_Port);
        while (true) {
            String path ="";
            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            int option = Integer.parseInt(in.readUTF());

            if (option == 1) {
                String file_name = in.readUTF();
                String server_mapped = in.readUTF();
                String directory = in.readUTF();
                insertFile(directory, server_mapped, file_name);
            } 
            else {

                boolean found = false;
                String file_name = "";
                file_name = in.readUTF();
                int dot_end = file_name.lastIndexOf('.');
                String base_file_name = file_name.substring(0, dot_end);
                String server_mapped = in.readUTF();
                path=server_mapped;
                String directory = in.readUTF();
                int i = Integer.parseInt(in.readUTF());
                int j = Integer.parseInt(in.readUTF());

                found = lookup(new File("/home/stu10/s12/sxy9369/Courses/DS/" + server_mapped), file_name);
                
                
                if (!found && i>0) {
                    i = i - 1;
                    j = j / 2;

                    int server_id = hashCode(base_file_name, i, j);
                    makeConnectionToServer(server_names[server_id]);
                    System.out.println("Request is forwarded  to     :" + server_names[server_id]);
                    Socket socket1 = new Socket(server_IP, server_Port);
                    DataOutputStream out1 = new DataOutputStream(socket1.getOutputStream());
                    DataInputStream in1 = new DataInputStream(socket1.getInputStream());
                    
                    out1.writeUTF(option + "");
                    out1.writeUTF(file_name);
                    
                    out1.writeUTF(server_names[server_id]);
                    out1.writeUTF("/home/stu10/s12/sxy9369/Courses/DS/");
                    out1.writeUTF(i + "");
                    out1.writeUTF(j + "");
                    
                    String status_of_file = in1.readUTF();
                    if (status_of_file.equals("found")) {
                        found = true;
                        path = server_mapped+"-> "+in1.readUTF();
                        
                        System.out.println(" path returned   " + path);
                        out1.writeUTF(path);

                    }

                }
                
                if(!found){
                    System.out.println("File not found  " + server_mapped);
                    out.writeUTF("not found");
                    out.writeUTF(path+" ");
                
                }
             
                
                if (found) {
                    System.out.println("File found  " + server_mapped);
                    out.writeUTF("found");
                    out.writeUTF(path+" ");
                    if (i == 0 && j == 0) {
                        checkForOccurrence(directory + server_mapped, file_name);    // when file found check for count of file 

                    }

                }

            }

        }
    }

    /**
     * The checkForOccurrence() method checks if the count of the file has
     * reached the threshold for replication. If the file is being looked up for
     * the first time it adds the file to the File mapping hashmap.
     *
     * @param directory: String which is the directory where the file is stored
     * @param file_name: String base name of the file.
     *
     */
    public static void checkForOccurrence(String directory, String file_name) {
        //System.out.println("Inside Check occurrence");
        if (file_map.containsKey(file_name)) {
            file_map.put(file_name, file_map.get(file_name) + 1);

            if (file_map.get(file_name) % threshold1 < 1) {
                System.out.println("Replication done to child   ");
                replicateFile(directory, file_name, 1);                                     // replication on level 1

            } else if (file_map.get(file_name) % threshold2 < 1) {
                System.out.println("Replicated done on the leaf server   ");
                replicateFile(directory, file_name, 2);                                     //replication on level 2

            }
        } else {
            file_map.put(file_name, 1);                                                      //adding the file to the hashmap

        }
    }

    /**
     * The replicateFile() method, the server first maps a server in a level
     * lower and copies the file to that server.
     *
     * @param directory: String which is the directory where the file is stored
     * @param file_name: String base name of the file.
     * @param level: The level in the tree
     */
    public static void replicateFile(String directory, String file_name, int level) {
        int random_child_position = 0;
        if (level == 1) {
            Random rand = new Random();
            random_child_position = rand.nextInt(2);
        } else {
            Random rand = new Random();
            random_child_position = rand.nextInt(4);
        }
        int random_child_server = hashCode(file_name, level, random_child_position);
        insertFile(directory, server_names[random_child_server], file_name);

    }

    /**
     * The makeConnection() method, the server first maps a server in a level
     * lower and copies the file to that server.
     *
     * @param server_name: The name of the server to which a connection needs to
     * be made.
     *
     */
    public static void makeConnectionToServer(String server_name) throws IOException {
        try {
            InetAddress iAddress = InetAddress.getByName(server_name + ".cs.rit.edu");
            server_IP = iAddress.getHostAddress();
            
        } catch (UnknownHostException e) {
        }

    }

    /**
     * The hashCode() method, takes the file name, calculates the ASCII value
     * and mods 7 on it and then based on the level and position returns back
     * the hashcode for mapping a server from the list of server.
     *
     * @param file_name: The base file name of the file.
     * @param level: The level for which the hashcode is being calculated.
     * @param position: The position in the level for which the hashcode is
     * being calculated.
     * @return ascii_hashed_value: The index of the server which is the
     * hashcode.
     *
     */
    public static int hashCode(String file_name, int level, int position) {
        
        int ascii_hashed_value = asciiCalc(file_name);
        ascii_hashed_value = (ascii_hashed_value + ((int) Math.pow(2, level)) + position) % 7;
        System.out.println("server mapped    " + ascii_hashed_value);
        return ascii_hashed_value;
    }

    /**
     * The asciiCalc() method takes the filename and sums up the ascii value of
     * all the chracters.
     *
     * @param s: File name for ascii calculation
     */
    public static int asciiCalc(String s) {
        int temp = 0;
        for (int i = 0; i < s.length(); i++) {
            temp += (int) s.charAt(i);
        }
        return temp % 7;

    }

    /**
     * The insertFile() method, the server copies a file from the source path
     * specified to the destination path
     *
     * @param directory: String which is the directory where the file is stored
     * @param server_mapped: Destination Server.
     * @param file_name: The base file name of the file.
     */
    public static void insertFile(String directory, String server_mapped, String file_name) {
        System.out.println("inside insertFile method");
        Path copy_from_1 = Paths.get(directory, file_name);
        Path copy_to_1 = Paths.get("/home/stu10/s12/sxy9369/Courses/DS/" + server_mapped, copy_from_1.getFileName().toString());

        try {
            Files.copy(copy_from_1, copy_to_1, REPLACE_EXISTING, COPY_ATTRIBUTES, NOFOLLOW_LINKS);
            //System.out.println("File copied to "+server_mapped);
        } catch (IOException e) {
            System.err.println(e);
        }

    }

    public static void insertInMap(String file_name) {
        if (!file_map.containsKey(file_name)) {

            file_map.put(file_name, file_map.get(file_name) + 1);
        } else {
            file_map.put(file_name, 1);
        }

    }

    /**
     * The lookup() method, the server looks for a file in the specified
     * directory and return true or false based on the status of the file
     * search.
     *
     * @param directory: String which is the directory where the file is stored
     * @param file_name: The base file name of the file.
     * @return found: Boolean value determining the file status.
     */
    public static boolean lookup(File directory, String fileName) {

        File[] list = directory.listFiles();
        boolean found = false;
        if (list != null) {
            for (File fil : list) {
                if (fil.isDirectory()) {
                    lookup(fil, fileName);
                } else if (fileName.equalsIgnoreCase(fil.getName())) {
                    System.out.println(fil.getParentFile());
                    found = true;
                }
            }
        }

        if (found) {
            System.out.println("Found!");
        } else {
            System.out.println("Not Found!");
        }

        return found;
    }

}
