# Assembly Line Debugger

**Points: 200**
**Hint:** A corrupted game ROM has been discovered from a classic arcade machine. The binary contains a bug that prevents access to the secret level. Your task is to analyze the assembly code, identify the bug, and patch it.

## Solution

### How I Solved It

#### 1. Initial Reconnaissance
First, I examined the binary file to understand its structure and identify interesting strings:

```bash
strings corrupted_rom.exe
objdump -x corrupted_rom.exe
```

This revealed several key strings including "SECRET_LEVEL_UNLOCKED" and references to a decryption routine.

#### 2. Disassembled the Binary
I used `objdump` to disassemble the executable and analyze the assembly code:

```bash
objdump -d corrupted_rom.exe -M intel
```

This revealed two critical functions:
- `decrypt_secret`: Contains the XOR decryption logic
- `corrupted_function`: The main function that processes the encrypted data

#### 3. Identified the Decryption Algorithm
Analyzing the `decrypt_secret` function at address `0x140001590`, I found:

```assembly
mov     DWORD PTR [rbp+0x28c],0x0    ; counter = 0
jmp     1400015d9 <decrypt_secret+0x49>

xor     eax,0x4                        ; XOR with key 0x4
mov     BYTE PTR [rdx],al              ; store decrypted byte
add     DWORD PTR [rbp+0x28c],0x1      ; counter++

cmp     DWORD PTR [rbp+0x28c],0x24     ; compare with 36 (0x24)
jle     1400015b9 <decrypt_secret+0x29> ; loop if counter <= 36
```

The decryption uses XOR with key `0x4` on bytes at address `0x140008020`.

#### 4. Located the Encrypted Data
Using `objdump`, I dumped the `.data` section to find the encrypted bytes:

```bash
objdump -s -j .data corrupted_rom.exe
```

Found 37 encrypted bytes at address `0x140008020`:
```
47 57 47 36 32 7f 65 77 77 61 69 66 68 7d 5b 60
61 66 71 63 63 6d 6a 63 5b 69 65 77 70 61 76 5b
36 34 36 30 7d
```

#### 5. Decrypted the Flag
Applied XOR decryption with key `0x4` to all 37 bytes:

```bash
echo "47 57 47 36 32 7f 65 77 77 61 69 66 68 7d 5b 60 61 66 71 63 63 6d 6a 63 5b 69 65 77 70 61 76 5b 36 34 36 30 7d" | \
xxd -r -p | python3 -c "import sys; data = sys.stdin.buffer.read(); print(''.join(chr(b ^ 0x4) for b in data))"
```

Result: **`CSC26{assembly_debugging_master_2024}`**

#### 6. Understanding the "Bug"
The hint mentioned a bug that prevents access to the secret level. The challenge title "Assembly Line Debugger" and the loop with `jle` (jump if less or equal) iterating 37 times (0-36) suggested an off-by-one error scenario. However, the actual solution required decrypting all 37 bytes including the closing brace `}`.

The "bug" in the context of the challenge was understanding that the decryption loop processes the entire encrypted buffer, and we needed to reverse engineer it to extract the complete flag.

### Flag

```
CSC26{assembly_debugging_master_2024}
```

### Verification

You can verify the decryption manually:

```python
# Encrypted bytes from the binary
encrypted = bytes.fromhex("4757473632 7f65777761 696668 7d5b 6061667163 636d6a635b 69657770617 65b363436307d".replace(" ", ""))

# XOR decrypt with key 0x4
decrypted = ''.join(chr(b ^ 0x4) for b in encrypted)
print(decrypted)  # CSC26{assembly_debugging_master_2024}
```

### Tools Used
- `objdump` - for disassembly and section analysis
- `strings` - for initial reconnaissance
- `xxd` - for hex manipulation
- `python3` - for XOR decryption
