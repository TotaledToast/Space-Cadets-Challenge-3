import java.util.*;
import java.io.*;

public class Main {

    List<String> programInput = new ArrayList<String>();

    public static void main(String[] args) throws FileNotFoundException {
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter the program file path: ");
        String path = reader.nextLine();
        //gets the path to the txt file containing the code
        reader.close();
        Program_Runner program = new Program_Runner(path);
        //starts the interpreter



    }



}

class Program_Runner {
    List<String> programInput = new ArrayList<String>();
    //stores the read program input
    Integer CurrentLine = 0;
    //Stores the currently running line of code

    List<While_Storage> Whiles = new ArrayList<While_Storage>();
    //stores all encountered while, including inactive ones
    Dictionary<String, Integer> Variables = new Hashtable<>();
    //stores every variable and its value

    public Program_Runner(String fileName) throws FileNotFoundException {
        fileReader(fileName); //reads the given code file
        Run_Code(); //runs the code using the programInput arraylist
    }

    void Run_Code(){


        do {
            String command = get_Command(programInput.get(CurrentLine)); //seperated the command word from the line of code
            System.out.print("Line: " + CurrentLine + " Code: " + programInput.get(CurrentLine) + " | ");
            //outputs current line of code to the console to form a record of execution
            Run_Command(command, programInput.get(CurrentLine)); //runs the line of code
            Display_Variable(); //outputs current variables to the console to keep a record of execution
            //this forms 1 line of text in the console with the outputted line of code
            CurrentLine++;
            //increments to the next line of code
        } while (CurrentLine < programInput.size());
        // will keep running until the last line of code has been passed
    }

    void Display_Variable(){
        System.out.println("Variables are: " + Variables);
        //displays all variables currently made
    }

    void Run_Command(String command, String Line){
        //runs a different function depending on what command was given
        switch (command) {
            case "clear":
                clear_Command(Line);
                break;
            case "incr":
                incr_Command(Line);
                break;
            case "decr":
                decr_Command(Line);
                break;
            case "while":
                while_Command(Line);
                break;
            case "end":
                end_Command(Line);
                break;
        }
    }

    void clear_Command(String Line){
        //adds a new variable to the Variables with a value of 0, or sets the value of an existing variable to 0
        Variables.put(Line.replaceAll("^(\\s*)clear (.+);", "$2"), 0);
    }

    void incr_Command(String Line){
        //increases the value of a variable by 1
        String Identifier = Line.replaceAll("^(\\s*)incr (.+);", "$2");
        Variables.put(Identifier, Variables.get(Identifier) + 1);
    }

    void decr_Command(String Line){
        //decreases the value of a variable by 1
        String Identifier = Line.replaceAll("^(\\s*)decr (.+);", "$2");
        Variables.put(Identifier, Variables.get(Identifier) - 1);
    }

    void while_Command(String Line){
        //seperates the code into the given variable and the amount of whitespace before the while
        String Variable = Line.replaceAll("^(\\s*)while (.+) not 0 do;", "$2");
        Integer Whitespace = Line.replaceAll("^(\\s*)while (.+) not 0 do;", "$1").length();
        Whiles.add(new While_Storage(Variable, Whitespace, CurrentLine));
        //creates a new While_Storage object with this information and the current line of code
    }

    void end_Command(String Line){
        //find the amount of whitespace before the end statement
        Integer Whitespace = Line.replaceAll("^(\\s*)end;", "$1").length();
        for (While_Storage element : Whiles){
            if (Objects.equals(element.Whitespace, Whitespace) && element.active){
                //finds the While_Storage object that is active and has the same amount of whitespace (The while corresponding to the end)
                if (Variables.get(element.Variable) != 0){
                    CurrentLine = element.Startspace;
                } else {
                    element.active = false;
                }   //if the while condition is not yet satisfied then it will move the code back to the start of the while loop
                //if the while condition is satisfied the loop is turned inactive and will be moved past in the code
            }
        }
    }

    String get_Command(String Line){
        //seperates the command word from a line of code
        return Line.replaceAll("(\\s*)(clear|incr|decr|while|end)(.*);", "$2");
    }

    void fileReader(String fileName) throws FileNotFoundException {
        File input = new File(fileName); //opens the given file
        Scanner fileReader = new Scanner(input);
        while (fileReader.hasNextLine()){
            String line = fileReader.nextLine();
            programInput.add(line);
            //reads through every line in the file and adds it to an arraylist for easier reading
        }
        fileReader.close();
    }
}

class While_Storage {
    String Variable;
    Integer Whitespace;
    Integer Startspace;
    Boolean active = true;

    While_Storage(String var, Integer White, Integer Start){
        //sets all necessary variables for the while condition
        Variable = var;
        Whitespace = White;
        Startspace = Start;
    }

}