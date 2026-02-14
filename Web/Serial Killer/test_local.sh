#!/bin/bash

# Local testing script for the vulnerable Java code
# This demonstrates the vulnerability without hitting the actual challenge server

set -e

echo "=== Local Deserialization Vulnerability Test ==="
echo ""

# Check if Java compiler is available
if ! command -v javac &> /dev/null; then
    echo "Error: javac not found. Please install Java JDK."
    exit 1
fi

# Compile the Java files
echo "[*] Compiling Java files..."
javac SessionManager.java 2>/dev/null || echo "Warning: SessionManager compilation had warnings"
javac VulnerableApp.java 2>/dev/null || echo "Warning: VulnerableApp compilation had warnings"
echo "[+] Compilation complete"
echo ""

# Create a simple serialized HashMap for testing
echo "[*] Creating test serialized object..."
cat > TestSerializer.java << 'EOF'
import java.io.*;
import java.util.*;

public class TestSerializer {
    public static void main(String[] args) throws Exception {
        // Create a simple HashMap
        HashMap<String, String> map = new HashMap<>();
        map.put("test", "data");
        map.put("user", "admin");
        
        // Serialize it
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();
        
        // Print as base64
        System.out.println(java.util.Base64.getEncoder().encodeToString(baos.toByteArray()));
    }
}
EOF

javac TestSerializer.java 2>/dev/null
TEST_PAYLOAD=$(java TestSerializer)
echo "[+] Test payload generated"
echo ""

# Test VulnerableApp
echo "[*] Testing VulnerableApp with benign payload..."
java VulnerableApp session "$TEST_PAYLOAD"
echo ""

echo "[+] Test complete"
echo ""
echo "NOTE: To test with malicious payloads (CommonsCollections gadgets):"
echo "  1. Ensure Apache Commons Collections library is in the classpath"
echo "  2. Use ysoserial to generate the payload"
echo "  3. Run: java VulnerableApp session \$(cat payload.b64)"
echo ""
echo "WARNING: This demonstrates a critical vulnerability (CWE-502)"
echo "         Never deserialize untrusted data in production code!"

# Cleanup
rm -f TestSerializer.java TestSerializer.class
