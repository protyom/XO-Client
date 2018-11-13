package nolife;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.TreeMap;

public class ClientMain {
    public static char field[][];
    public static int moves;
    public static boolean isPlaying;
    public static boolean isMoving;
    public static String user2;
    public static String newMove;
    public static char symb;

    public static void main(String[] args)  {
        isPlaying=false;
        isMoving=false;
        moves = 0;
        field = new char[3][3];
        for (int i=0;i<3;i++) {
            for (int j=0;j<3;j++) {
                field[i][j]=' ';
            }
        }
        if (args.length < 2 || args.length > 3) {
            System.err.println(	"Invalid number of arguments\n" + "Use: nic name [host]" );
            waitKeyToStop();
            return;
        }
        try ( Socket sock = ( args.length == 2 ?
                new Socket( InetAddress.getLocalHost(), Protocol.PORT ):
                new Socket( args[2], Protocol.PORT ) )) {
            System.err.println("initialized");
            session(sock, args[0], args[1] );
        } catch ( Exception e) {
            System.err.println(e);
        } finally {
            System.err.println("bye...");
        }
    }

    public static void newGame(){
        moves=0;
        field = new char[3][3];
        for (int i=0;i<3;i++) {
            for (int j=0;j<3;j++) {
                field[i][j]=' ';
            }
        }
    }

    public static char checkField(){
        char res = checkHorizontal();
        if(res!=' '){
            return res;
        }
        res = checkVertical();
        if(res!=' '){
            return res;
        }
        res = checkDiagonal();
        if(res!=' '){
            return res;
        }
        return ' ';
    }
    public static char checkHorizontal(){
        for(int i=0;i<3;i++){
            char first=field[i][0];
            if(first==' '){
                continue;
            }
            boolean hasWon=true;
            for(int j=1;j<3;j++){
                if(field[i][j]!=first){
                    hasWon=false;
                    break;
                }
            }
            if(hasWon){
                return first;
            }
        }
        return ' ';
    }
    public static char checkVertical(){
        for(int i=0;i<3;i++){
            char first=field[0][i];
            if(first==' '){
                continue;
            }
            boolean hasWon=true;
            for(int j=1;j<3;j++){
                if(field[j][i]!=first){
                    hasWon=false;
                    break;
                }
            }
            if(hasWon){
                return first;
            }
        }
        return ' ';
    }
    public static char checkDiagonal(){
        char first=field[0][0];
        if(first!=' '){
            boolean hasWon=true;
            for(int i=1;i<3;i++){
                if(field[i][i]!=first){
                    hasWon=false;
                    break;
                }
            }
            if(hasWon){
                return first;
            }
        }

        first=field[0][2];
        if(first!=' '){
            boolean hasWon=true;
            for(int i=1;i<3;i++){
                if(field[i][2-i]!=first){
                    hasWon=false;
                    break;
                }
            }
            if(hasWon){
                return first;
            }
        }
        return ' ';
    }



    public static void drawField(){
        System.out.println("  123");
        System.out.println("  ___");
        System.out.print("a|");
        for (char a:field[0]) {
            System.out.print(a);
        }
        System.out.print("|\nb|");
        for (char a:field[1]) {
            System.out.print(a);
        }
        System.out.print("|\nc|");
        for (char a:field[2]) {
            System.out.print(a);
        }
        System.out.print("|\n");
        System.out.println("  ___");
    }

    public static char opponentSymb(){
        if(symb=='X'){
            return '0';
        }
        return 'X';
    }

    public static String makeMove(Scanner in)throws IOException {

        String str= in.nextLine();
        int y = str.charAt(0)-'1';
        int x = Character.toLowerCase(str.charAt(1))-'a';
        if(x<0 || x>2 || y<0 || y>2){
            throw new IOException("Wrong input");
        }
        field[x][y] =symb;
        moves++;
        return str;
    }

    public static void makeOpponentMove(String str)throws IOException {
        int y = str.charAt(0)-'1';
        int x = Character.toLowerCase(str.charAt(1))-'a';
        if(x<0 || x>2 || y<0 || y>2){
            throw new IOException("Wrong input");
        }
        field[x][y] =opponentSymb();
        moves++;
    }


    static void waitKeyToStop() {
        System.err.println("Press a key to stop...");
        try {
            System.in.read();
        } catch (IOException e) {
        }
    }

    static class Session {
        boolean connected = false;
        String userNic = null;
        String userName = null;
        Session( String nic, String name ) {
            userNic = nic;
            userName = name;
        }
    }
    static void session(Socket s, String nic, String name) {
        try ( Scanner in = new Scanner(System.in);
              ObjectInputStream is = new ObjectInputStream(s.getInputStream());
              ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream())) {
            Session ses = new Session(nic, name);
            if ( openSession( ses, is, os, in )) {
                try {
                    while (true) {
                        if(!isPlaying || (isPlaying && isMoving)) {
                            Message msg = getCommand(ses, in);
                            if (!processCommand(ses, msg, is, os, in)) {
                                break;
                            }
                        }else{
                            System.out.println(user2+" makes the move");
                            Message res = (Message) is.readObject();
                            while(res.getID()!=Protocol.CMD_MOVE ) {
                                res = (Message) is.readObject();

                            }
                            MessageMove msm = (MessageMove) res;
                            makeOpponentMove(msm.move);
                            isMoving = true;
                            if(checkField()==symb){
                                System.out.println("You won");
                                isPlaying = false;
                                isMoving = false;
                            }
                            if(isPlaying){
                                char checkSymb=checkField();
                                if(checkSymb!=' '){
                                    if(checkSymb=='X'){
                                        System.out.println("You won");
                                        isPlaying=false;
                                        isMoving=false;
                                    }else{
                                        System.out.println("You lost");
                                        isPlaying=false;
                                        isMoving=false;
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    closeSession(ses, os);
                }
            }
        } catch ( Exception e) {
            System.err.println(e);
        }
    }

    static boolean openSession(Session ses, ObjectInputStream is, ObjectOutputStream os, Scanner in)
            throws IOException, ClassNotFoundException {
        os.writeObject( new MessageConnect(ses.userNic, ses.userName));
        MessageConnectResult msg = (MessageConnectResult) is.readObject();
        if (msg.Error()== false ) {
            System.err.println("connected");
            ses.connected = true;
            return true;
        }
        System.err.println("Unable to connect: "+ msg.getErrorMessage());
        System.err.println("Press <Enter> to continue...");
        if( in.hasNextLine())
            in.nextLine();
        return false;
    }

    static void closeSession(Session ses, ObjectOutputStream os) throws IOException {
        if ( ses.connected ) {
            ses.connected = false;
            os.writeObject(new MessageDisconnect());
        }
    }

    static Message getCommand(Session ses, Scanner in){
        while (true) {
            printPrompt();

            if(!isPlaying){

                if (in.hasNextLine()== false)
                    break;
                String str = in.nextLine();
                byte cmd = translateCmd(str);
                switch ( cmd ) {
                    case -1:
                        return null;
                    case Protocol.CMD_OFFER:
                        user2 = in.nextLine();
                        return new MessageOffer(ses.userNic,user2);
                    case Protocol.CMD_USER:
                        return new MessageUser();
                    case 0:
                        continue;
                    default:
                        System.err.println("Unknow command!");
                        continue;
                }
            }else{
                drawField();
                System.out.println("Enter your coords");
                try {
                    String newMove = makeMove(in);
                }catch (IOException e){
                    System.out.println(e.getMessage());
                    continue;
                }
                isMoving = false;
                return new MessageMove(user2,newMove);
            }

        }
        return null;
    }



    static TreeMap<String,Byte> commands = new TreeMap<String,Byte>();
    static {
        commands.put("q", new Byte((byte) -1));
        commands.put("quit", new Byte((byte) -1));
        commands.put("o", new Byte(Protocol.CMD_OFFER));
        commands.put("offer", new Byte(Protocol.CMD_OFFER));
        commands.put("u", new Byte(Protocol.CMD_USER));
        commands.put("users", new Byte(Protocol.CMD_USER));
    }

    static byte translateCmd(String str) {
        // returns -1-quit, 0-invalid cmd, Protocol.CMD_XXX
        str = str.trim();
        Byte r = commands.get(str);
        return (r == null ? 0 : r.byteValue());
    }

    static void printPrompt() {
        System.out.println();
        System.out.print("(q)uit/(o)offer/(u)sers >");
        System.out.flush();
    }

    static boolean processCommand(Session ses, Message msg,
                                  ObjectInputStream is, ObjectOutputStream os, Scanner in)
            throws IOException, ClassNotFoundException {
        if ( msg != null )
        {
            os.writeObject(msg);
            Message res = (Message) is.readObject();

            switch (res.getID()) {
                case Protocol.CMD_USER:
                    printUsers(( MessageUserResult ) res);
                    break;
                case Protocol.CMD_OFFER:
                    MessageOffer msg1 = (MessageOffer)res;
                    if(offer(msg1,in)) {
                        user2 = msg1.usrNic;
                        os.writeObject(new MessageOfferResult(msg1.usrNic, msg1.usrToOffer,true));
                        symb='0';
                        isMoving = false;
                        isPlaying = true;
                        newGame();
                    }else{
                        os.writeObject(new MessageOfferResult(msg1.usrNic, msg1.usrToOffer,false));
                        isPlaying=false;
                    }
                    break;
                case Protocol.CMD_OFFER_RESULT:
                    MessageOfferResult msg2 = (MessageOfferResult)res;
                    if(msg2.accepted){
                        isPlaying=true;
                        isMoving=true;
                        symb = 'X';
                        newGame();
                    }
                default:
                    assert(false);
                    break;
                }

            return true;
        }
        return false;
    }


    static boolean offer( MessageOffer msg, Scanner in){
        System.out.println("Would you like to play with " +((MessageOffer) msg).usrNic+"?(y/n)");
        String ans = in.nextLine();
        if(Character.toLowerCase(ans.charAt(0))=='y'){
            return true;
        }else{
            return false;
        }
    }

    static void printUsers(MessageUserResult m) {
        if ( m.userNics != null ) {
            System.out.println("Users {");
            for (String str: m.userNics) {
                System.out.println("\t" + str);
            }
            System.out.println("}");
        }
    }
}

