import java.util.*;
import java.util.EnumSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException; 

public class Parser {

	List<String> rules = new ArrayList<>();
    String FILE_NAME = "rules.txt";

	public Parser() {
	loadFile();
	}

	public void loadFile() {
        System.out.println("load file module ");
        String relativelyPath = System.getProperty("user.dir");
        File ruleFile = new File(relativelyPath + "/" + this.FILE_NAME);
		System.out.println(ruleFile);
        BufferedReader br = null;
        try {
            FileReader in = new FileReader(ruleFile);
            br = new BufferedReader(in);
            int i = 0;
            String icmd = "";
            while ((icmd = br.readLine()) != null) {
                this.rules.add(icmd);
            }
        } catch (IOException e) {
            System.out.println("file does not exist.");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("nothing");
                }
            }
        }
        System.out.println("rules = " + rules);
    }


public static void main(String []args) {
	Parser parser = new Parser();
}
}
