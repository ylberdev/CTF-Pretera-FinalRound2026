# Rasputin

**Points: 100**  
**Hint:** Don't overcomplicate it. It is easier than you think.

## Solution

### How I Solved It

#### 1. Initial File Analysis
First, I examined the memory dump file to check its type and size:

```bash
cd /home/ylbaa/forensic
file rasputin.mem
ls -lh rasputin.mem
```

This revealed:
- File type: `data`
- Size: **832 bytes** (suspiciously small for a real memory dump!)

This immediately suggested this is not a real memory dump, but rather a simplified forensics challenge file.

#### 2. Hex Dump Examination
I dumped the file contents in hexadecimal to analyze its structure:

```bash
cat rasputin.mem | od -A x -t x1z -v | head -60
```

The output revealed a structured format with four "processes":

**PID 101 (browser.exe):**
```
PID: 101 | Process: browser.exe
Memory: 5a2f26c7057bca74d2261c63620867a3a7f12ba2c7b49457890815cedf78e48c
Data: random data: cookies, sessions, cache...
```

**PID 102 (notepad.exe):**
```
PID: 102 | Process: notepad.exe
Memory: c5d1e5ffe33c7c9f6dc799793294f2f20be7c2b1e81707814912e1650de05f7b
Data: xor_key:key42
```

**Key Finding:** PID 102 contains `xor_key:key42` - this is our decryption key!

**PID 103 (secret_app.exe):**
```
PID: 103 | Process: secret_app.exe
Memory: 6e35ab0c715d641b077ade1102b6dd4c4b259803befe044bddbb2d46bc465f73
Data: encrypted_flag:(binary data follows)
```

**Critical Finding:** PID 103 contains `encrypted_flag:` followed by raw binary bytes!

Hex dump of encrypted flag bytes:
```
000200 04 10 51 1b 0c 57 5e 03 4c 00 57 5c 51 4b 02 54  >..Q..W^.L.W\QK.T<
000210 52 53 1b 05 04 5a 5d 4b 55 00 53 53 4d 06 00 09  >RS...Z]KU.SSM...<
000220 53 4c 0d 50 09 01 4a 50 02 09 55 4f 55 50 5d 56  >SL.P..JP..UOUP]V<
000230 1c 00 03 52 51 41 02 05 09 52 1a 01 04 5c 56 4e  >...RQA...R...\VN<
000240 52 00 09 18 0a                                   >R....<
```

**PID 104 (systemsvc.exe):**
```
PID: 104 | Process: systemsvc.exe
Memory: 0966a8a1348e8be9257c89d60afa909ba01b148297372573e045d66cf498049
Data: logs: error 404, connection timeout
```

#### 3. Connecting the Dots
The challenge becomes clear:
1. **PID 102** gives us the XOR key: `key42`
2. **PID 103** contains the encrypted flag using XOR encryption
3. We need to XOR-decrypt the binary data with the repeating key `key42`

Just as the hint said: "Don't overcomplicate it. It is easier than you think."

#### 4. XOR Decryption
I wrote a Python script to extract and decrypt the flag:

```python
# Read the file
data = open('/home/ylbaa/forensic/rasputin.mem', 'rb').read()

# Find encrypted_flag: marker
marker = b'encrypted_flag:'
idx = data.index(marker) + len(marker)

# Find next newline (end of encrypted data)
end = data.index(b'\n', idx)
encrypted = data[idx:end]

# XOR decrypt with key "key42"
key = b'key42'
decrypted = bytes([encrypted[i] ^ key[i % len(key)] for i in range(len(encrypted))])

print('Encrypted bytes:', encrypted.hex())
print('Decrypted:', decrypted)
print('Decrypted str:', decrypted.decode('utf-8'))
```

Running this script:

```bash
python3 -c "
data = open('/home/ylbaa/forensic/rasputin.mem', 'rb').read()
marker = b'encrypted_flag:'
idx = data.index(marker) + len(marker)
end = data.index(b'\n', idx)
encrypted = data[idx:end]
key = b'key42'
decrypted = bytes([encrypted[i] ^ key[i % len(key)] for i in range(len(encrypted))])
print('Decrypted str:', decrypted.decode('utf-8'))
"
```

**Output:**
```
CSC26{4b8e5f54e7426f96b16182a286422b659bbd3d0b06ab63e4194867b7c56737f2b}
```

### Flag

```
CSC26{4b8e5f54e7426f96b16182a286422b659bbd3d0b06ab63e4194867b7c56737f2b}
```

### Verification

You can verify this solution by following these steps:

```bash
# 1. Check the file size (should be 832 bytes, not a real memory dump)
ls -lh rasputin.mem

# 2. Search for the XOR key in the file
strings rasputin.mem | grep -i "xor_key"
# Output: xor_key:key42

# 3. Search for the encrypted flag marker
strings rasputin.mem | grep -i "encrypted_flag"
# Output: encrypted_flag:(binary data)

# 4. Extract and decrypt the flag with Python
python3 << 'EOF'
data = open('rasputin.mem', 'rb').read()
marker = b'encrypted_flag:'
idx = data.index(marker) + len(marker)
end = data.index(b'\n', idx)
encrypted = data[idx:end]
key = b'key42'
decrypted = bytes([encrypted[i] ^ key[i % len(key)] for i in range(len(encrypted))])
print(decrypted.decode('utf-8'))
EOF
```

Or as a one-liner:

```bash
python3 -c "d=open('rasputin.mem','rb').read();m=b'encrypted_flag:';i=d.index(m)+len(m);e=d.index(b'\n',i);enc=d[i:e];k=b'key42';print(bytes([enc[x]^k[x%len(k)]for x in range(len(enc))]).decode())"
```


### Tools Used
- `file` - identify file type
- `ls` - check file size
- `od` - hexadecimal dump for binary analysis
- `strings` - extract printable text strings
- `Python 3` - XOR decryption script
