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
    int currentLine = 0;
    //Stores the currently running line of code

    List<While_Storage> whiles = new ArrayList<While_Storage>();
    //stores all encountered while, including inactive ones
    List<If_Storage> ifs = new ArrayList<>();
    Dictionary<String, Integer> variables = new Hashtable<>();
    //stores every variable and its value

    public Program_Runner(String fileName) throws FileNotFoundException {
        fileReader(fileName); //reads the given code file
        Run_Code(); //runs the code using the programInput arraylist
    }

    void Run_Code(){


        do {
            if (Check_ifs()){
                Run_Line();
            } else {
                try {
                    check_Line(programInput.get(currentLine));
                } catch (Exception e) {};
            }

            currentLine++;
            //increments to the next line of code
        } while (currentLine < programInput.size());
        // will keep running until the last line of code has been passed
    }

    void Run_Line(){
        String command = get_Command(programInput.get(currentLine)); //seperated the command word from the line of code
        System.out.print("Line: " + currentLine + " Code: " + programInput.get(currentLine) + " | ");
        //outputs current line of code to the console to form a record of execution
        Run_Command(command, programInput.get(currentLine)); //runs the line of code
        Display_Variable(); //outputs current variables to the console to keep a record of execution
        //this forms 1 line of text in the console with the outputted line of code
    }

    void check_Line(String Line){
        int whitespace = 0;
        if (Line.matches("(\\s*)else(.*);")){
            whitespace = Line.replaceAll("(\\s*)else(.*);","$1").length();
            int index = find_corresponding_if(whitespace);
            if (ifs.get(index).code_Running) {
                ifs.get(index).code_Running = false;
                ifs.get(index).has_Run = true;
            } else {
                System.out.print("Line: " + currentLine + " Code: " + programInput.get(currentLine) + " | ");
                Display_Variable();
                if (!Line.replaceAll("(\\s*)else(.*);", "$2").isEmpty() && !ifs.get(index).has_Run){
                    //has an if
                    if (check_if_statement_valid(Line.replaceAll("(\\s*)else (.*);","$2") + ";")){
                        ifs.get(index).code_Running = true;

                    }
                } else if (!ifs.get(index).has_Run){
                    //no if
                    ifs.get(index).code_Running = true;
                }
            }

        } else if (!Line.matches("(\\s*)(//)(.*)")){
            whitespace = Line.replaceAll("(\\s*)(.*);","$1").length();
            int index = find_corresponding_if(whitespace);
            if (index != -1){
                ifs.get(index).active = false;
                Run_Line();

            }
        }
    }

    int find_corresponding_if(int whitespace){
        int count = 0;
        for (If_Storage element : ifs){
            if (element.whitespace == whitespace && element.active){
                return count;
            }
            count ++;
        }
        return -1;
    }

    boolean Check_ifs(){
        boolean code_Running = true;
        for (If_Storage element : ifs){
            if (element.active && !element.code_Running){
                code_Running = false;
                break;
            }
        }
        return code_Running;
    }

    void Display_Variable(){
        System.out.println("Variables are: " + variables);
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
            case "+":
            case "-":
            case "/":
            case "*":
            case "%":{
                operator_Commands(Line, command);
                break;
            }
            case "if":
                if_command(Line);
                break;
            case "else":
                check_Line(Line);
            case "comment":
                break;
        }
    }

    void if_command(String Line){
        //(\s*)if (.*) (Is|Is Not) (.*);

        ifs.add(new If_Storage(Line.replaceAll("(\\s*)if (.*) (Is Not|Is) (.*);", "$1").length(), check_if_statement_valid(Line)));

    }

    void else_command(String Line){
        //            whitespace = Line.replaceAll("(\\s*)else(.*);","$1").length();
        //            int index = find_corresponding_if(whitespace);
        //            if (ifs.get(index).code_Running) {
        //                ifs.get(index).code_Running = false;
        //                ifs.get(index).has_Run = true;
        //            } else {
        //                if (!Line.replaceAll("(\\s*)else(.*);", "$2").isEmpty() && !ifs.get(index).has_Run){
        //                    //has an if
        //                    if (check_if_statement_valid(Line.replaceAll("(\\s*)else (.*);","$2") + ";")){
        //                        ifs.get(index).code_Running = true;
        //
        //                    }
        //                } else if (!ifs.get(index).has_Run){
        //                    //no if
        //                    ifs.get(index).code_Running = true;
        //                }
        //            }


    }

    boolean check_if_statement_valid(String Line){
        boolean is = false;
        int comparing_Value = 0;
        int variable_Value = variables.get(Line.replaceAll("(\\s*)if (.*) (Is Not|Is) (.*);","$2"));
        if (variables.get(Line.replaceAll("(\\s*)if (.*) (Is Not|Is) (.*);", "$4")) != null){
            comparing_Value = variables.get(Line.replaceAll("(\\s*)if (.*) (Is Not|Is) (.*);","$4"));
        } else {
            comparing_Value = Integer.parseInt(Line.replaceAll("(\\s*)if (.*) (Is Not|Is) (.*);","$4"));
        }
        if (Line.replaceAll("(\\s*)if (.*) (Is Not|Is) (.*);", "$3").equals("Is")) {
            is = true;
        }
        return Objects.equals(comparing_Value, variable_Value) == is;
    }

    void operator_Commands(String Line, String operator){
        String changing_Value= "";
        int first_Value = 0;
        int second_Value = 0;
        changing_Value = Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$2");
        if (variables.get(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$3")) != null) {
            first_Value = variables.get(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$3"));
        } else {
            first_Value = Integer.parseInt(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$3"));
        }
        if (variables.get(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$5")) != null) {
            second_Value = variables.get(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$5"));
        } else {
            second_Value = Integer.parseInt(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$5"));
        }

        switch  (operator){
            case "+":
                variables.put(changing_Value, first_Value + second_Value);
                break;
            case "-":
                variables.put(changing_Value, first_Value - second_Value);
                break;
            case "*":
                variables.put(changing_Value, first_Value * second_Value);
                break;
            case "/":
                variables.put(changing_Value, first_Value / second_Value);
                break;
            case "%":
                variables.put(changing_Value, first_Value % second_Value);
                break;
        }
    }



    void clear_Command(String Line){
        //adds a new variable to the Variables with a value of 0, or sets the value of an existing variable to 0
        variables.put(Line.replaceAll("^(\\s*)clear (.+);", "$2"), 0);
    }

    void incr_Command(String Line){
        //increases the value of a variable by 1
        String Identifier = Line.replaceAll("^(\\s*)incr (.+);", "$2");
        variables.put(Identifier, variables.get(Identifier) + 1);
    }

    void decr_Command(String Line){
        //decreases the value of a variable by 1
        String Identifier = Line.replaceAll("^(\\s*)decr (.+);", "$2");
        variables.put(Identifier, variables.get(Identifier) - 1);
    }

    void while_Command(String Line){
        //seperates the code into the given variable and the amount of whitespace before the while
        String Variable = Line.replaceAll("^(\\s*)while (.+) not 0 do;", "$2");
        int Whitespace = Line.replaceAll("^(\\s*)while (.+) not 0 do;", "$1").length();
        whiles.add(new While_Storage(Variable, Whitespace, currentLine));
        //creates a new While_Storage object with this information and the current line of code
    }

    void end_Command(String Line){
        //find the amount of whitespace before the end statement
        int Whitespace = Line.replaceAll("^(\\s*)end;", "$1").length();
        for (While_Storage element : whiles){
            if (Objects.equals(element.whitespace, Whitespace) && element.active){
                //finds the While_Storage object that is active and has the same amount of whitespace (The while corresponding to the end)
                if (variables.get(element.variable) != 0){
                    currentLine = element.startspace;
                } else {
                    element.active = false;
                }   //if the while condition is not yet satisfied then it will move the code back to the start of the while loop
                //if the while condition is satisfied the loop is turned inactive and will be moved past in the code
            }
        }
    }

    String get_Command(String Line){
        //seperates the command word from a line of code
        // (//).*
        String command = "";
        if (Line.matches("(\\s*)(//)(.*)")){
            command = "comment";
        } else if (Line.matches("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);")) {
            command = Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);", "$4");
        } else {
            command = Line.replaceAll("(\\s*)(clear|incr|decr|while|end|if|else)(.*);", "$2");
        }
        return command;
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
    String variable;
    int whitespace;
    int startspace;
    boolean active = true;

    While_Storage(String var, int White, int Start){
        //sets all necessary variables for the while condition
        variable = var;
        whitespace = White;
        startspace = Start;
    }

}

class If_Storage{
    int whitespace;
    boolean code_Running;
    boolean active = true;

    boolean has_Run;

    public If_Storage(int white, boolean running){
        whitespace= white;
        code_Running = running;
        has_Run = running;
    }

}