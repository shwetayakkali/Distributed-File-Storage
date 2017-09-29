
import java.util.ArrayList;
import java.util.Random;

public class HashClass {

    //static int prime_nos[]={3, 5, 7, 11, 13, 17, 19,23,29,31,37,41,43,47};
    protected static String server_names[] = {"glados", "medusa", "doors", "rhea", "reddwarf", "comet", "queeg"};

    public static void main(String args[]) {
        String str[] = {"test", "abc", "shweta", "7", "output", "yakkali", "ds"};
        int i = 1;
        int j = 0;
        //for(int k=0;k<str.length;k++)
        System.out.println("Hash code     :" + server_names[hashCode(str[0], i, j)]);
    }

    public static int hashCode(String file_name, int level, int position) {
        int ascii_hashed_value = asciiCalc(file_name);
        ascii_hashed_value = (ascii_hashed_value + ((int) Math.pow(2, level)) + position) % 7;
        return ascii_hashed_value;
    }

    public static int asciiCalc(String s) {
        int temp = 0;
        for (int i = 0; i < s.length(); i++) {
            temp += (int) s.charAt(i);
        }
        return temp % 7;

    }
}
