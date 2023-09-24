//This is vartalap server 2.0
//Much enhanced then previous code it uses some new concepts like queue(which I stored in L34), threadsafe queue etc.
//It is actually concurrent server framework - which can be used to make different applications like a chat server, dropbox like application etc.


package L32;
import java.io.*;
import java.net.*;
import java.util.*;

//import L33.Server1;

public class Server1 {
   public static MessageQueue<String> q = new MessageQueue<>();
   public static ArrayList<PrintWriter> noslist = new ArrayList<>();
   public static void main(String[] args) throws Exception {
       System.out.println("Server Signing ON");
       ServerSocket ss = new ServerSocket(8096);
       MessageDispatcher md = new MessageDispatcher();
       md.setDaemon(true);
       md.start();
       for (int i = 0; i < 10; i++)
       {
           Socket soc = ss.accept();
           System.out.println("Connection established");
           Conversation c = new Conversation(soc);
           c.start();
       }
       System.out.println("Server Signing OFF");
       ss.close();
   }
}

class MessageQueue<T> {
   ArrayList<T> al = new ArrayList<>();
   synchronized public void enqueue(T i) {
       al.add(i);
       notify();
   }
   synchronized public T dequeue() {
       if (al.isEmpty()) {
           try {
               wait();
           } catch (Exception ex) {
           }
       }
       return al.remove(0);
   }
   synchronized public void print() {
       for (T i : al) {
           System.out.println("-->" + i);
       }
   }
   @Override
   synchronized public String toString() {
       String str = null;
       for (T s : al) {
           str += "::" + s;
       }
       return str;
   }
}



//Conversation file/class/thread
class Conversation extends Thread {
  Socket soc;
  Conversation(Socket soc) {
      this.soc = soc;
  }
  @Override
  public void run() {
      try {
          BufferedReader nis = new BufferedReader(
                      new   InputStreamReader(
                               soc.getInputStream()
                      )
          );
          PrintWriter nos = new PrintWriter(
                  new BufferedWriter(
                          new OutputStreamWriter(
                                  soc.getOutputStream()
                                                          )
                  ), true
          );
          Server1.noslist.add(nos);
          String str = nis.readLine();
          while (!str.equals("End")) {
              Server1.q.enqueue(str);
              System.out.println("Server Received "+str);
              str = nis.readLine();
          }
          nos.println("End");
          Server1.noslist.remove(nos);
          System.out.println(
                  "Connection with "+
                   soc.getInetAddress().getHostAddress()+
                  " Terminated");
      } catch (Exception e) {
      }
  }
}

//Dispatcher file/class/thread
class MessageDispatcher extends Thread {
  @Override
  public void run() {
      while (true) {
          try {
              String str = Server1.q.dequeue();
              for (PrintWriter o : Server1.noslist) {
                  o.println(str);
              }
          } catch (Exception e) {
          }
      }
  }
}
