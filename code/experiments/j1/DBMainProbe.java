import com.dbmain.jidbm.DBMLibrary;
public class DBMainProbe {
  public static void main(String[] args) {
    System.out.println("version=" + DBMLibrary.getVersion());
    System.out.println("platform=" + DBMLibrary.getPlatform());
    System.out.println("No project loaded or modified.");
  }
}
