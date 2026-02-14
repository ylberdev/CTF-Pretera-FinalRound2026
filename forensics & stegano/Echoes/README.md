# Echoes

**350 Points**  
**Hint:** We seized a few files from a criminal. Can you extract the hidden flag from the file?

## Solution

### How I Solved It

#### 1. Analyzed the Seized Files

The challenge provided a zip archive `phantom_images.zip` containing three JPEG files:
- `image1.jpg` (4913 bytes)
- `image2.jpg` (4923 bytes) 
- `image3_corrupted.jpg` (2363 bytes, truncated)

Initial analysis revealed:
- All three images appeared to be solid colors when decoded
- `image2.jpg` contained a JPEG comment field with the text: **`P1x3l5h4d0wK3y!`** (leet-speak for "PixelShadowKey!")
- `image3_corrupted.jpg` was missing its JPEG end-of-image marker

#### 2. Discovered Steganographic Data

Using `steghide` with an **empty passphrase**, I extracted hidden data from the images:

```bash
steghide extract -sf image1.jpg -p ''
# Output: chunk1.txt
steghide extract -sf image2.jpg -p ''
# Output: chunk2.txt
```

**Content of extracted files:**
- `chunk1.txt`: `U2FsdGVkX18vOVMg1+njSFwRpVgv6J0+aqQ2SeMPRt7ScsZInG4yZI`
- `chunk2.txt`: `QIJfuzgx7TnoCmpWjnfubVR+oLddbW3YhCJu9LuAh5YgU+b2K728I=`

#### 3. Identified OpenSSL Encryption

Combining the two Base64-encoded chunks revealed an OpenSSL "Salted__" encrypted blob:

```python
import base64

chunk1 = "U2FsdGVkX18vOVMg1+njSFwRpVgv6J0+aqQ2SeMPRt7ScsZInG4yZI"
chunk2 = "QIJfuzgx7TnoCmpWjnfubVR+oLddbW3YhCJu9LuAh5YgU+b2K728I="
combined = chunk1 + chunk2

decoded = base64.b64decode(combined)
print(decoded[:8])  # b'Salted__'
```

The data structure indicated OpenSSL's `enc` command format with a salt.

#### 4. Brute-Forced Decryption Parameters

Using the discovered key `P1x3l5h4d0wK3y!`, I tested various cipher and digest combinations:

```bash
for cipher in aes-256-cbc aes-128-cbc; do
  for md in md5 sha256 sha1; do
    openssl enc -${cipher} -d -in combined.bin \
      -pass pass:'P1x3l5h4d0wK3y!' \
      -md $md -pbkdf2 2>/dev/null
  done
done
```

**Success:** `aes-128-cbc` with `sha256` digest and `pbkdf2` key derivation!

#### 5. Decrypted the Flag

```bash
echo 'U2FsdGVkX18vOVMg1+njSFwRpVgv6J0+aqQ2SeMPRt7ScsZInG4yZIQIJfuzgx7TnoCmpWjnfubVR+oLddbW3YhCJu9LuAh5YgU+b2K728I=' | \
  openssl enc -aes-128-cbc -d -a -pass pass:'P1x3l5h4d0wK3y!' -md sha256 -pbkdf2

# Output: CSC26{9b2f4e7a1d3c6b8f2e9a7d4b1f3e6c8a2d9b7f4e1c3a6d8f2b9e5}
```

### Flag

```
CSC26{9b2f4e7a1d3c6b8f2e9a7d4b1f3e6c8a2d9b7f4e1c3a6d8f2b9e5}
```

### Complete Decryption Script

```python
import base64
import subprocess

# Extract chunks from images using steghide (requires manual extraction)
# steghide extract -sf image1.jpg -p '' -> chunk1.txt
# steghide extract -sf image2.jpg -p '' -> chunk2.txt

# Read and combine chunks
chunk1 = "U2FsdGVkX18vOVMg1+njSFwRpVgv6J0+aqQ2SeMPRt7ScsZInG4yZI"
chunk2 = "QIJfuzgx7TnoCmpWjnfubVR+oLddbW3YhCJu9LuAh5YgU+b2K728I="
combined_b64 = chunk1 + chunk2

# Decode base64
encrypted_data = base64.b64decode(combined_b64)

# Save to file for OpenSSL
with open('encrypted.bin', 'wb') as f:
    f.write(encrypted_data)

# Decrypt using OpenSSL
password = "P1x3l5h4d0wK3y!"
result = subprocess.run(
    ['openssl', 'enc', '-aes-128-cbc', '-d', '-in', 'encrypted.bin',
     '-pass', f'pass:{password}', '-md', 'sha256', '-pbkdf2'],
    capture_output=True, text=True
)

print(f"Flag: {result.stdout.strip()}")
```


**Flag found:** `CSC26{9b2f4e7a1d3c6b8f2e9a7d4b1f3e6c8a2d9b7f4e1c3a6d8f2b9e5}`
