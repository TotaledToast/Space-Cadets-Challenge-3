import java.util.*;
import java.io.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter the program file path: ");
        String path = reader.nextLine();
        //gets the path to the txt file containing the code
        reader.close();
        Program_Container program = new Program_Container(path);
        //Program_Runner program = new Program_Runner(path);
        //starts the interpreter



    }



}

class Program_Container {
    List<String> programInput = new ArrayList<>();

    List<Function_Store> functions = new ArrayList<>();

    public Program_Container(String fileName) throws Exception {
        fileReader(fileName); //reads the given code file
        Split_Code_Into_Functions();
        Program_Runner func = new Program_Runner(functions.get(Find_Function("Main")).code_Store, functions);
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

    void Split_Code_Into_Functions(){
        String current_Function = "";
        for (String Line : programInput){


            if (Line.contains("):")){
                current_Function = Line.replaceAll("^(.*)\\((.*)\\):", "$1");
                String all_Variables = Line.replaceAll("^(.*)\\((.*)\\):", "$2");
                List<String> split_Variables = Arrays.stream(all_Variables.split(",")).toList();
                Function_Store temp;
                if (Objects.equals(split_Variables.get(0), "")){
                    temp = new Function_Store(current_Function, new ArrayList<>());
                } else {
                    temp = new Function_Store(current_Function, split_Variables);
                }
                functions.add(temp);
            } else if(!current_Function.isEmpty()){
                functions.get(Find_Function(current_Function)).add_Line_To_Code(Line);
            }
        }
    }

    int Find_Function(String current_Function){
        int index = 0;
        for (Function_Store function : functions){
            if (Objects.equals(function.identifier, current_Function)){
                return index;
            }
            index++;
        }
        return -1;
    }
}

class Program_Runner {
    List<String> programInput = new ArrayList<String>();
    //stores the read program input
    List<Function_Store> functions = new ArrayList<>();

    Program_Runner current_Function;
    int currentLine = 0;
    //Stores the currently running line of code

    List<While_Storage> whiles = new ArrayList<While_Storage>();
    //stores all encountered while, including inactive ones
    List<If_Storage> ifs = new ArrayList<>();
    //stores all encountered if, including inactive ones
    Dictionary<String, Integer> variables = new Hashtable<>();
    //stores every variable and its value
    Integer return_Value;

    public Program_Runner(List<String> code, List<Function_Store> functions) throws Exception {
        this.programInput = code;
        this.functions = functions;
        Run_Code(); //runs the code using the programInput arraylist
    }

    public Program_Runner(List<String> code, List<Function_Store> functions, Dictionary<String,Integer> inputVariables) throws Exception {
        this.programInput = code;
        this.functions = functions;
        this.variables = inputVariables;
        Run_Code(); //runs the code using the programInput arraylist
    }

    int Find_Function(String current_Function){
        int index = 0;
        for (Function_Store function : functions){
            if (Objects.equals(function.identifier, current_Function)){
                return index;
            }
            index++;
        }
        return -1;
    }

    void Run_Code() throws Exception {


        do {
            if (Should_Code_Run()){
                Run_Line();
            } else {
                try {
                    check_Line(programInput.get(currentLine));
                } catch (Exception e) {};
            }
            //checks if any if statements are currently stopping code from being run. If they arent then the code runs as usual
            //if the code is being stopped from running then the line of code is checked to see if any changes to the if statements have occured

            currentLine++;
            //increments to the next line of code
        } while (currentLine < programInput.size());
        // will keep running until the last line of code has been passed
    }

    void Run_Line() throws Exception {
        String command = get_Command(programInput.get(currentLine)); //seperated the command word from the line of code
        Run_Command(command, programInput.get(currentLine)); //runs the line of code
        System.out.print("Line: " + currentLine + " Code: " + programInput.get(currentLine) + " | ");
        Display_Variable(); //outputs current variables to the console to keep a record of execution
    }

    void check_Line(String Line) throws Exception {
        //checks the current line to see if any changes to an if statement should be run
        int whitespace = 0;
        if (Line.matches("(\\s*)else(.*);")) {
            //checks if the current line is an else statement
            else_command(Line);

        }else if (Line.matches("(\\s*)end if;")) {
            //if the line is not an else this checks if its an end if
            whitespace = Line.replaceAll("(\\s*)end if;","$1").length();
            int index = find_corresponding_if(whitespace);
            //attempts to find a corresponding if statement to the end if
            //found by comparing whitepsace values of active if statements
            if (index != -1){
                //if there is a corresponding if statement then the if statement is deactived as the end of it has been reached
                ifs.get(index).active = false;

            } else {
                //if there isnt a correspoding if statement then an error is thrown
                throw new Exception("No corresponding if statement found! | " + Line);
            }
        }else if (!Line.matches("(\\s*)(//)(.*)")){
            //if it isnt an else or end if statment then it checkd if it isnt a comment (a regular line of code)
            whitespace = Line.replaceAll("(\\s*)(.*);","$1").length();
            int index = find_corresponding_if(whitespace);
            //if it is a regular line of code it checks if its on the same indentation as any active if statement
            if (index != -1){
                //if it is on the same indentation as an active if statement then an error is thrown as there should be an end if statement before this line
                System.out.println("No detected \"end if;\" statement");
                throw new Exception("Test");
                
            }
        }
    }
    int find_corresponding_if(int whitespace){
        //finds an active if statement with the same whitespace indentation given to it
        int count = 0;
        for (If_Storage element : ifs){
            //loops throw each elements in the ifs arraylist
            if (element.whitespace == whitespace && element.active){
                return count;
            }
            count ++;
            //keeps track of the current arraylist index corresponding to the current element in the if statement
        }
        return -1;
        //returns -1 if no corresponding if statement is found
        //returns the index of the if statement if one is found
    }

    boolean Should_Code_Run(){
        //checks if any active if statements are currently stopping code from running
        boolean code_Running = true;
        for (If_Storage element : ifs){
            if (element.active && !element.code_Running){
                code_Running = false;
                break;
            }
        }
        return code_Running;
        //returns if the code should run or not
    }

    void Display_Variable(){
        System.out.println("Variables are: " + variables);
        //displays all variables currently made
    }

    void Run_Command(String command, String Line) throws Exception {
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
                else_command(Line);
                break;
            case "function":
                function_command(Line);
                break;
            case "return":
                return_command(Line);
                break;
            case "none":
            case "comment": {
                break;
            }
        }
    }

    void function_command(String Line) throws Exception {
        String functionName = Line.replaceAll("^(\\s*)(.*)\\((.*)\\)", "$2");
        int index = Find_Function(functionName);
        Function_Store func = functions.get(index);
        if (is_function_valid(Line.replaceAll("^(\\s*)(.*)\\((.*)\\)", "$3"), func)){
            Dictionary<String, Integer> inputVars = get_function_variables(Line.replaceAll("^(\\s*)(.*)\\((.*)\\)", "$3"), func);
            System.out.println("Function \"" + functionName + "\" is Starting:");
            Program_Runner new_func;
            if (Line.replaceAll("^(\\s*)(.*)\\((.*)\\)", "$3").isEmpty()){
                new_func = new Program_Runner(func.code_Store, functions);
            } else {
                new_func = new Program_Runner(func.code_Store, functions, inputVars);
            }
            current_Function = new_func;
            System.out.println("Function \"" + functionName + "\" hsa ended:");
        } else {
            throw new Exception("Incorrect Input Parameters!");
        }
    }

    void return_command(String Line){
        return_Value = variables.get(Line.replaceAll("(\\s*)return (.*);", "$2"));
    }

    int get_return_value(){
        return return_Value;
    }

    Dictionary<String, Integer> get_function_variables(String input, Function_Store function){
        List<String> inputSplit = List.of(input.split(","));
        Dictionary<String, Integer> outputVariables = new Hashtable<>();
        int count = 0;
        for (String var: inputSplit){
            outputVariables.put(function.input_Variables.get(count), variables.get(var));
            count++;
        }
        return outputVariables;
    }

    boolean is_function_valid(String input, Function_Store function){
        List<String> inputSplit = List.of(input.split(","));
        return inputSplit.size() == function.input_Variables.size();
    }

    void if_command(String Line){
        //(\s*)if (.*) (Is|Is Not) (.*);

        ifs.add(new If_Storage(Line.replaceAll("(\\s*)if (.*) (Is Not|Is) (.*);", "$1").length(), check_if_statement_valid(Line)));

    }

    void else_command(String Line){
        //runs when an else command is found
        int whitespace = Line.replaceAll("(\\s*)else(.*);", "$1").length();
        int index = find_corresponding_if(whitespace);
        //finds the if statement corresponding to the whitespace indentation of the else statement
        if (ifs.get(index).code_Running) {
            //if the corresponsing if statement is allowing code to run then the code will stop being allowed to run
            // and the if statement will have been run
            ifs.get(index).code_Running = false;
            ifs.get(index).has_Run = true;
        } else {
            //if the code isnt being allowed to run then this code is run
            System.out.print("Line: " + currentLine + " Code: " + programInput.get(currentLine) + " | ");
            Display_Variable();
            //prints the current line are variable to console as the else statement is being run (checked)
            if (!Line.replaceAll("(\\s*)else(.*);", "$2").isEmpty() && !ifs.get(index).has_Run) {
                //has an if AND is hasnt been run yet
                if (check_if_statement_valid(Line.replaceAll("(\\s*)else (.*);", "$2") + ";")) {
                    //checks if the if statement is valid, and if it is then the code is set to being allowed to run
                    ifs.get(index).code_Running = true;

                }
            } else if (!ifs.get(index).has_Run) {
                //no if
                //if its just an else statement then no check needs to be made and the code can be allowed to run
                ifs.get(index).code_Running = true;
            }
        }


    }

    boolean check_if_statement_valid(String Line){
        //checks if the condition of an if statement
        boolean is = false;
        int comparing_Value = 0;
        int variable_Value = variables.get(Line.replaceAll("(\\s*)if (.*) (Is Not|Is) (.*);","$2"));
        //gets the value of the variable that will be used
        if (variables.get(Line.replaceAll("(\\s*)if (.*) (Is Not|Is) (.*);", "$4")) != null){
            //if the value that the main variable is being comapred to is a variable
            comparing_Value = variables.get(Line.replaceAll("(\\s*)if (.*) (Is Not|Is) (.*);","$4"));
        } else {
            //if the value that the main variable is being comapred to is a number
            comparing_Value = Integer.parseInt(Line.replaceAll("(\\s*)if (.*) (Is Not|Is) (.*);","$4"));
        }
        if (Line.replaceAll("(\\s*)if (.*) (Is Not|Is) (.*);", "$3").equals("Is")) {
            //if the statement is an 'Is' statement then is is set to true
            is = true;
        }
        return Objects.equals(comparing_Value, variable_Value) == is;
        //returns if the condition is met or not
    }

    void operator_Commands(String Line, String operator) throws Exception {
        String changing_Value= "";
        int first_Value = 0;
        int second_Value = 0;
        changing_Value = Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$2");
        //gets the name of the variable thats being affected
        if (variables.get(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$3")) != null) {
            //if the first value is a variable
            first_Value = variables.get(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$3"));
        } else if(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$3").matches("(.*)\\((.*)\\)")) {
            function_command(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$3"));
            first_Value = current_Function.get_return_value();
        }else {
            //if the first value is a number
            first_Value = Integer.parseInt(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$3"));
        }
        if (variables.get(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$5")) != null) {
            //if the second value is a variable
            second_Value = variables.get(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$5"));
        } else if(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$5").matches("(.*)\\((.*)\\)")) {
            function_command(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$5"));
            second_Value = current_Function.get_return_value();
        }else {
            //is the second value is a number
            second_Value = Integer.parseInt(Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);","$5"));
        }

        switch  (operator){
            //depending on the operator given it affects the value of the variable ina different way
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

    String get_Command(String Line) throws Exception {
        //seperates the command word from a line of code
        String command = "";
        if (Line.isEmpty()) {
            command = "none";
        }else if (Line.matches("(\\s*)(//)(.*)")){
            command = "comment";
        } else if (Line.matches("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);")) {
            command = Line.replaceAll("(\\s*)(.*) = (.*) (\\+|-|\\*|/|%) (.*);", "$4");
        } else if (Line.matches("(\\s*)(clear|incr|decr|while|end|if|else|return)(.*);")) {
            command = Line.replaceAll("(\\s*)(clear|incr|decr|while|end|if|else|return)(.*);", "$2");
        }else if(Line.matches("^(.*)\\((.*)\\);")) {
            command = "function";
        } else {
            throw new Exception("Incorrect command syntax: \"" + Line + "\"");
        }
        return command;
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

class Function_Store{
    String identifier;
    List<String> code_Store = new ArrayList<>();
    List<String> input_Variables = new ArrayList<>();

    public Function_Store(String identifier, List<String> input_Variables){
        this.identifier = identifier;
        this.input_Variables = input_Variables;
    }

    public void add_Line_To_Code(String Line){
        this.code_Store.add(Line);
    }

}