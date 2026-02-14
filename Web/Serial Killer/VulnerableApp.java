import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Base64;

public class VulnerableApp {
    
    public static String processSession(String base64Data) {
        return deserializeData(base64Data, "session");
    }
    
    public static String processConfig(String base64Data) {
        return deserializeData(base64Data, "config");
    }
    
    public static String processCache(String base64Data) {
        return deserializeData(base64Data, "cache");
    }
    
    private static String deserializeData(String base64Data, String type) {
        try {
            byte[] data = Base64.getDecoder().decode(base64Data);
            
            ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data)
            );
            
            Object obj = ois.readObject();
            ois.close();
            
            return String.format(
                "{\"status\":\"success\",\"message\":\"%s processed\",\"type\":\"%s\"}",
                type, obj.getClass().getName()
            );
            
        } catch (IOException e) {
            return "{\"status\":\"error\",\"message\":\"Invalid Java serialization stream\"}";
        } catch (ClassNotFoundException e) {
            return "{\"status\":\"error\",\"message\":\"Unknown class in serialization stream\"}";
        } catch (Exception e) {
            return String.format(
                "{\"status\":\"error\",\"message\":\"Deserialization error: %s\"}",
                e.getMessage()
            );
        }
    }
    
    public static void main(String[] args) {
        System.out.println("TechCorp Solutions - Vulnerable API Simulator");
        System.out.println("==============================================\n");
        
        if (args.length < 2) {
            System.out.println("Usage: java VulnerableApp <endpoint> <base64_payload>");
            System.out.println("\nEndpoints:");
            System.out.println("  session  - Simulates /api/session");
            System.out.println("  config   - Simulates /api/config");
            System.out.println("  cache    - Simulates /api/cache");
            System.out.println("\nExample:");
            System.out.println("  java VulnerableApp session rO0ABXNyAB...");
            System.exit(1);
        }
        
        String endpoint = args[0];
        String payload = args[1];
        String result;
        
        switch (endpoint.toLowerCase()) {
            case "session":
                result = processSession(payload);
                break;
            case "config":
                result = processConfig(payload);
                break;
            case "cache":
                result = processCache(payload);
                break;
            default:
                System.err.println("Unknown endpoint: " + endpoint);
                System.exit(1);
                return;
        }
        
        System.out.println("Response: " + result);
    }
}
