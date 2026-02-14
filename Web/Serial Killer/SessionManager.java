import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class SessionManager {
    private ObjectInputStream sessionData;
    
    public void loadSession(String data) {
        try {
            sessionData = new ObjectInputStream(
                new ByteArrayInputStream(data.getBytes())
            );
            
            Object session = sessionData.readObject();
            
            System.out.println("Session loaded: " + session);
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading session: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java SessionManager <base64_serialized_data>");
            System.exit(1);
        }
        
        SessionManager manager = new SessionManager();
        
        manager.loadSession(args[0]);
    }
}
