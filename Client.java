
/* 
 * Client.java 
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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.Random;
import java.util.Scanner;

/**
 * Client class has functions where the Client requests for file insertion or
 * file lookup multiple times.
 *
 * @author Shweta Yakkali
 */

public class Client {

    protected static String server_IP;
    private static final int server_Port = 5554;
    protected static String client_IP;
    protected static String server_names[] = {"glados", "medusa", "doors", "rhea", "yes", "comet", "queeg"};

    /**
     * The main() method accepts the option which the client wants to do i.e.
     * either insertion or lookup and chooses that option and performs the
     * required processing.
     *
     */
    public static void main(String[] args) throws IOException {

        try {

            while (true) {
                String path = "";
                Scanner sc = new Scanner(System.in);

                System.out.println("What do you want to do?");
                System.out.println("1. Insert a File");
                System.out.println("2. Look up a file");
                System.out.println("Enter your option");
                int option = Integer.parseInt(sc.nextLine());

                if (option < 2) {

                    System.out.println("Enter the File name");
                    //sc.nextLine();
                    String file_name = sc.nextLine();
                    int dot_end = file_name.lastIndexOf('.');
                    String base_file_name = file_name.substring(0, dot_end);
                    int server_mapped = serverSelection(base_file_name, 0, 0);
                    System.out.println("server  name    :" + server_names[server_mapped]);
                    makeConnectionToServer(server_names[server_mapped]);                    //make connection to the server mapped using hash code.

                    Socket socket = new Socket(server_IP, server_Port);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    out.writeUTF(option + "");
                    out.writeUTF(file_name);
                    out.writeUTF(server_names[server_mapped]);
                    out.writeUTF("/home/stu10/s12/sxy9369/Courses/DS/gorgon");
                    System.out.println("File Insertion Successful");

                } 
                else if (option < 3) {

                    System.out.println("Enter the File name");
                    String file_name = sc.nextLine();
                    int dot_end = file_name.lastIndexOf('.');
                    String base_file_name = file_name.substring(0, dot_end);                  
                    Random rand = new Random();
                    int j = rand.nextInt(4);                                                // mapping a random leaf node server                    
                    int server_mapped = hashCode(base_file_name, 2, j);                       // map the reuired server and make a connetion
                    makeConnectionToServer(server_names[server_mapped]);
                    System.out.println("Forwarding the request..");
                    path = path + server_names[server_mapped];
                    Socket socket = new Socket(server_IP, server_Port);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());

                    out.writeUTF(option + "");
                    out.writeUTF(file_name);
                    out.writeUTF(server_names[server_mapped]);
                    out.writeUTF("/home/stu10/s12/sxy9369/Courses/DS/gorgon");
                    out.writeUTF(2 + "");
                    out.writeUTF(j + "");
                    String status_of_file = in.readUTF();                                     //Reading the status of the file.
                    
                    path = in.readUTF();

                    System.out.println(status_of_file);
                    System.out.println(path);
                    if(status_of_file.equals("not found")){
                        System.out.println("Try Again!");
                    
                    }
                    System.out.println("---------------------------------");

                } else {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Exception occurred");
        }

    }

    /**
     * The serverSelection() method, the correct mapped server is returned by
     * the hashCode() method.
     *
     * @param file_name: The base file name of the file.
     * @param level: The level for which the hashcode is being calculated.
     * @param position: The position in the level for which the hashcode is
     * being calculated.
     * @return: The index of the server which is the hashcode.
     *
     */
    public static int serverSelection(String file_name, int level, int position) {

        return hashCode(file_name, level, position);

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

}
