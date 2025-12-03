# GitHub SSH Setup - All Commands & Prompts

**Setup Date:** December 3, 2024
**User:** harish-pathak
**Email:** harish.pathak@infobeans.com
**Machine:** macOS with Terminal (zsh)

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [SSH Key Generation](#ssh-key-generation)
3. [SSH Agent Setup](#ssh-agent-setup)
4. [SSH Configuration](#ssh-configuration)
5. [Git Configuration](#git-configuration)
6. [GitHub Integration](#github-integration)
7. [Project Setup for Pushing](#project-setup-for-pushing)
8. [Verification Commands](#verification-commands)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

**System Information:**
```
OS: macOS Sonoma (23.2.0)
Shell: zsh
Machine: /Users/harish.pathak
Working Directory: /Users/harish.pathak/consumer-finance-service
```

**Required Tools:**
- Git installed
- Terminal/zsh access
- GitHub account created

---

## SSH Key Generation

### Command 1: Check Existing SSH Keys

```bash
ls -la ~/.ssh
```

**Purpose:** Verify if SSH keys already exist.

**Output (Before Setup):**
```
total 16
drwx------   4 harish.pathak  staff   128 Mar  4  2024 .
drwxr-x---+ 88 harish.pathak  staff  2816 Dec  3 19:39 ..
-rw-------   1 harish.pathak  staff  1295 Jul  3  2024 known_hosts
-rw-------   1 harish.pathak  staff   458 Mar  4  2024 known_hosts.old
```

---

### Command 2: Generate Ed25519 SSH Key Pair

```bash
ssh-keygen -t ed25519 -C "harish.pathak@infobeans.com" -N "" -f ~/.ssh/id_ed25519
```

**Parameters:**
| Param | Purpose | Value |
|-------|---------|-------|
| `-t ed25519` | Key type (modern, secure) | Ed25519 algorithm |
| `-C` | Comment for identification | Email: harish.pathak@infobeans.com |
| `-N ""` | No passphrase | Empty string |
| `-f` | Output file path | ~/.ssh/id_ed25519 |

**Output:**
```
Generating public/private ed25519 key pair.
Your identification has been saved in /Users/harish.pathak/.ssh/id_ed25519
Your public key has been saved in /Users/harish.pathak/.ssh/id_ed25519.pub
The key fingerprint is:
SHA256:LOtUHUl2caLo2IYdXO4YP59gLO6H1XXTYN0yRZBNkbw harish.pathak@infobeans.com
The key's randomart image is:
+--[ED25519 256]--+
|          + +.+XB|
|       . * + o*o+|
|        = =  . +o|
|       B O .  .Eo|
|      + S O. . ..|
|       * o.+..   |
|      o .o  o    |
|     o .. .      |
|      . ..       |
+----[SHA256]-----+
```

**Files Created:**
- `~/.ssh/id_ed25519` - Private key (KEEP SECRET)
- `~/.ssh/id_ed25519.pub` - Public key (safe to share)

---

### Command 3: Display Your Public Key

```bash
cat ~/.ssh/id_ed25519.pub
```

**Output:**
```
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIPBlvzAGUePUIcyjRD2T6Os/QSvFCyuAPVc1qRf5MyIu harish.pathak@infobeans.com
```

**⚠️ IMPORTANT:** Copy this entire line. You'll need it to add to GitHub Settings.

---

## SSH Agent Setup

### Command 4a: Start SSH Agent

```bash
eval "$(ssh-agent -s)"
```

**Purpose:** Start the SSH agent daemon to manage your SSH keys.

**Output:**
```
Agent pid 64553
```

The `pid` number will vary, but confirms the agent is running.

---

### Command 4b: Add Your Private Key to SSH Agent

```bash
ssh-add ~/.ssh/id_ed25519
```

**Purpose:** Load your private key into the SSH agent so it can be used for authentication.

**Output:**
```
Identity added: /Users/harish.pathak/.ssh/id_ed25519 (harish.pathak@infobeans.com)
```

---

### Command 4c: Verify Key is Loaded (Optional)

```bash
ssh-add -l
```

**Purpose:** List all keys currently loaded in the SSH agent.

**Output:**
```
256 SHA256:LOtUHUl2caLo2IYdXO4YP59gLO6H1XXTYN0yRZBNkbw harish.pathak@infobeans.com (ED25519)
```

---

## SSH Configuration

### Command 5: Create SSH Config File

```bash
cat > ~/.ssh/config << 'EOF'
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/id_ed25519
    AddKeysToAgent yes
    IgnoreUnknown UseKeychain
    UseKeychain yes
EOF
```

**Purpose:** Create SSH configuration for automatic GitHub authentication.

**Config Details:**

| Setting | Value | Purpose |
|---------|-------|---------|
| `Host github.com` | github.com | Hostname this config applies to |
| `HostName` | github.com | Actual server to connect to |
| `User` | git | SSH username for GitHub |
| `IdentityFile` | ~/.ssh/id_ed25519 | Path to your private key |
| `AddKeysToAgent` | yes | Auto-add key to agent |
| `IgnoreUnknown` | UseKeychain | Ignore if UseKeychain not supported |
| `UseKeychain` | yes | Use macOS Keychain for storing passphrases |

**File Saved To:** `~/.ssh/config`

---

### Command 6: Set Correct SSH File Permissions

```bash
chmod 600 ~/.ssh/config
chmod 600 ~/.ssh/id_ed25519
chmod 644 ~/.ssh/id_ed25519.pub
```

**Purpose:** Ensure SSH files have correct security permissions.

**Permission Breakdown:**

| File | Permission | Meaning | Why |
|------|-----------|---------|-----|
| `config` | 600 | Owner read/write only | Contains private configuration |
| `id_ed25519` | 600 | Owner read/write only | PRIVATE KEY - must be protected |
| `id_ed25519.pub` | 644 | Owner RW, others read-only | PUBLIC KEY - safe to share |

**Verification:**
```bash
ls -la ~/.ssh/id*
```

**Output:**
```
-rw-------  1 harish.pathak  staff  419 Dec  3 19:42 /Users/harish.pathak/.ssh/id_ed25519
-rw-r--r--  1 harish.pathak  staff  109 Dec  3 19:42 /Users/harish.pathak/.ssh/id_ed25519.pub
```

---

### Command 7: Accept GitHub's Host Key

```bash
ssh-keyscan -H github.com >> ~/.ssh/known_hosts 2>/dev/null
```

**Purpose:** Add GitHub's public host key to prevent "Host key verification failed" errors.

**What Happens:**
1. `ssh-keyscan` retrieves GitHub's SSH host keys
2. `-H` hashes the hostname for security
3. `>>` appends the key to known_hosts file
4. `2>/dev/null` suppresses diagnostic output

**Verification:**
```bash
grep github.com ~/.ssh/known_hosts
```

---

## Git Configuration

### Command 8a: Set Git User Name

```bash
git config --global user.name "harish-pathak"
```

**Purpose:** Set your name for all Git commits globally.

**Verification:**
```bash
git config --global user.name
```

**Output:**
```
harish-pathak
```

---

### Command 8b: Set Git User Email

```bash
git config --global user.email "harish.pathak@infobeans.com"
```

**Purpose:** Set your email for all Git commits globally.

**Verification:**
```bash
git config --global user.email
```

**Output:**
```
harish.pathak@infobeans.com
```

---

### Command 8c: Verify Git User Configuration

```bash
git config --global --list | grep -E "user\."
```

**Output:**
```
user.name=harish-pathak
user.email=harish.pathak@infobeans.com
```

---

### Command 9: Configure Git SSH URL Alias

```bash
git config --global url."git@github.com:".insteadOf "https://github.com/"
```

**Purpose:** Automatically use SSH instead of HTTPS for all GitHub repositories.

**How It Works:**
```
Input:  git clone https://github.com/harish-pathak/repo.git
Actual: git clone git@github.com:harish-pathak/repo.git
```

**Verification:**
```bash
git config --global --list | grep url.
```

**Output:**
```
url.git@github.com:.insteadof=https://github.com/
```

---

### Command 10: View All Git Global Configuration

```bash
git config --global --list
```

**Output (Relevant Lines):**
```
user.name=harish-pathak
user.email=harish.pathak@infobeans.com
url.git@github.com:.insteadof=https://github.com/
```

**Configuration File Location:** `~/.gitconfig`

---

## GitHub Integration

### Command 11: Display Your SSH Public Key (For GitHub)

```bash
cat ~/.ssh/id_ed25519.pub
```

**Output:**
```
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIPBlvzAGUePUIcyjRD2T6Os/QSvFCyuAPVc1qRf5MyIu harish.pathak@infobeans.com
```

**⚠️ CRITICAL NEXT STEP:** Add this key to GitHub manually.

---

### Manual Step: Add SSH Key to GitHub

This must be done in your web browser:

**Steps:**

1. **Open GitHub Settings**
   ```
   URL: https://github.com/settings/keys
   Or: Profile Icon → Settings → SSH and GPG keys
   ```

2. **Click "New SSH key" button**

3. **Fill in the form:**
   - **Title:** `MacBook SSH Key` (or descriptive name)
   - **Key type:** Select `Authentication Key`
   - **Key:** Paste your complete public key from Command 11:
     ```
     ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIPBlvzAGUePUIcyjRD2T6Os/QSvFCyuAPVc1qRf5MyIu harish.pathak@infobeans.com
     ```

4. **Click "Add SSH key"**

5. **Confirm with GitHub password if prompted**

---

### Command 12: Test SSH Connection to GitHub

```bash
ssh -T git@github.com
```

**Expected Output (After Adding Key to GitHub):**
```
Hi harish-pathak! You've successfully authenticated, but GitHub does not provide shell access.
```

**Current Output (Before Adding Key):**
```
git@github.com: Permission denied (publickey).
```

**This is expected** until you complete the manual GitHub key addition step above.

---

## Project Setup for Pushing

### Command 13a: Navigate to Project Directory

```bash
cd /Users/harish.pathak/consumer-finance-service
```

Or simply:
```bash
cd ~/consumer-finance-service
```

---

### Command 13b: Initialize Git Repository (If New)

```bash
git init
```

**Purpose:** Initialize an empty Git repository.

**Output:**
```
Initialized empty Git repository in /Users/harish.pathak/consumer-finance-service/.git/
```

---

### Command 13c: Add All Files to Staging Area

```bash
git add .
```

**Purpose:** Stage all files for commit.

---

### Command 13d: Create Initial Commit

```bash
git commit -m "Initial commit"
```

**Purpose:** Create your first commit with all project files.

**Output Example:**
```
[main (root-commit) abc1234] Initial commit
 15 files changed, 2500 insertions(+)
 create mode 100644 src/...
```

---

### Command 13e: Add GitHub Repository as Remote

```bash
git remote add origin git@github.com:harish-pathak/consumer-finance-service.git
```

**Purpose:** Link your local repository to your GitHub repository.

**Note:** Replace `consumer-finance-service` with your actual GitHub repo name.

---

### Command 13f: Verify Remote Configuration

```bash
git remote -v
```

**Expected Output:**
```
origin	git@github.com:harish-pathak/consumer-finance-service.git (fetch)
origin	git@github.com:harish-pathak/consumer-finance-service.git (push)
```

---

### Command 13g: Ensure Main Branch (if needed)

```bash
git branch -M main
```

**Purpose:** Rename your primary branch to "main" (GitHub default).

---

### Command 13h: Push to GitHub

```bash
git push -u origin main
```

**Purpose:** Push your local commits to GitHub and set up branch tracking.

**Parameter Meanings:**
- `-u` = Set upstream (link local to remote branch)
- `origin` = Remote name
- `main` = Branch name

**Output Example:**
```
Enumerating objects: 15, done.
Counting objects: 100% (15/15), done.
Delta compression using up to 8 threads
Compressing objects: 100% (12/12), done.
Writing objects: 100% (15/15), 2.5 KiB | 2.5 MiB/s, done.
Total 15 (delta 0), reused 0 (delta 0), pack-reused 0
To github.com:harish-pathak/consumer-finance-service.git
 * [new branch]      main -> main
Branch 'main' set up to track remote branch 'main' from 'origin'.
```

---

## Verification Commands

### Verify SSH Key Exists

```bash
ls -la ~/.ssh/id_ed25519*
```

**Output:**
```
-rw-------  1 harish.pathak  staff  419 Dec  3 19:42 /Users/harish.pathak/.ssh/id_ed25519
-rw-r--r--  1 harish.pathak  staff  109 Dec  3 19:42 /Users/harish.pathak/.ssh/id_ed25519.pub
```

---

### Verify SSH Config Exists

```bash
cat ~/.ssh/config
```

**Output:**
```
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/id_ed25519
    AddKeysToAgent yes
    IgnoreUnknown UseKeychain
    UseKeychain yes
```

---

### Verify SSH Key is in Agent

```bash
ssh-add -l
```

**Output:**
```
256 SHA256:LOtUHUl2caLo2IYdXO4YP59gLO6H1XXTYN0yRZBNkbw harish.pathak@infobeans.com (ED25519)
```

---

### Verify Git Configuration

```bash
git config --global --list | grep -E "user\.|url\."
```

**Output:**
```
user.name=harish-pathak
user.email=harish.pathak@infobeans.com
url.git@github.com:.insteadof=https://github.com/
```

---

### Verify GitHub Connection

```bash
ssh -T git@github.com
```

**Expected Output (After Key Added):**
```
Hi harish-pathak! You've successfully authenticated, but GitHub does not provide shell access.
```

---

### Verify Git Remote

```bash
git remote -v
```

**Output:**
```
origin	git@github.com:harish-pathak/consumer-finance-service.git (fetch)
origin	git@github.com:harish-pathak/consumer-finance-service.git (push)
```

---

## Complete Setup Script

Run these commands in order:

```bash
# 1. Start SSH agent
eval "$(ssh-agent -s)"

# 2. Add key to agent
ssh-add ~/.ssh/id_ed25519

# 3. Set SSH permissions
chmod 600 ~/.ssh/config ~/.ssh/id_ed25519
chmod 644 ~/.ssh/id_ed25519.pub

# 4. Accept GitHub host key
ssh-keyscan -H github.com >> ~/.ssh/known_hosts 2>/dev/null

# 5. Configure Git (if not already done)
git config --global user.name "harish-pathak"
git config --global user.email "harish.pathak@infobeans.com"
git config --global url."git@github.com:".insteadOf "https://github.com/"

# 6. Display public key (copy this to GitHub)
cat ~/.ssh/id_ed25519.pub

# 7. Test connection (will work after adding key to GitHub)
ssh -T git@github.com

# 8. Setup project for pushing
cd ~/consumer-finance-service
git init
git add .
git commit -m "Initial commit"
git remote add origin git@github.com:harish-pathak/consumer-finance-service.git
git branch -M main

# 9. Push to GitHub (after adding key to GitHub)
git push -u origin main
```

---

## Troubleshooting

### Issue: "Permission denied (publickey)"

**Cause:** SSH key not added to GitHub account.

**Solutions:**

```bash
# 1. Verify key is loaded
ssh-add -l

# 2. Re-add key if needed
ssh-add ~/.ssh/id_ed25519

# 3. Display your public key to add to GitHub
cat ~/.ssh/id_ed25519.pub

# 4. Check SSH config
cat ~/.ssh/config

# 5. Test with verbose output
ssh -vT git@github.com
```

---

### Issue: "Could not open a connection to your authentication agent"

**Cause:** SSH agent is not running.

**Solution:**
```bash
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519
```

---

### Issue: "Host key verification failed"

**Cause:** GitHub's host key not in known_hosts.

**Solution:**
```bash
ssh-keyscan -H github.com >> ~/.ssh/known_hosts
```

---

### Issue: Git says "fatal: not a git repository"

**Cause:** Not in a Git repository directory.

**Solution:**
```bash
cd /path/to/consumer-finance-service
git init
```

---

### Issue: "remote origin already exists"

**Cause:** Remote already configured.

**Solution:**
```bash
# View current remote
git remote -v

# Remove existing remote
git remote remove origin

# Add new remote
git remote add origin git@github.com:harish-pathak/consumer-finance-service.git
```

---

## Quick Reference

### Most Used Commands

```bash
# Daily development
git status                          # Check status
git add .                          # Stage changes
git commit -m "message"            # Commit
git push                           # Push to GitHub
git pull                           # Pull from GitHub

# Check configuration
git config --global --list         # View all config
git config --global user.name      # Check name
git config --global user.email     # Check email

# SSH verification
ssh-add -l                         # List SSH keys
cat ~/.ssh/id_ed25519.pub         # Show public key
ssh -T git@github.com             # Test GitHub connection

# Remote operations
git remote -v                      # View remotes
git remote set-url origin <url>   # Change remote
git clone git@github.com:user/repo.git  # Clone with SSH
```

---

## Security Best Practices

1. **Protect Private Key**
   - Never share `~/.ssh/id_ed25519`
   - Keep secure backups

2. **Public Key Safety**
   - Safe to share `~/.ssh/id_ed25519.pub`
   - Add to any Git service

3. **File Permissions**
   - Keep `~/.ssh/config` and `id_ed25519` at 600
   - Keep `id_ed25519.pub` at 644

4. **Monitor GitHub**
   - Check SSH keys in GitHub Settings
   - Remove old/unused keys
   - Rotate keys periodically

5. **Never Commit Secrets**
   - Don't commit `.git/config` with credentials
   - Use SSH keys instead of passwords
   - Use personal access tokens for API access

---

## Setup Status

**Current Status:**

- [x] SSH key pair generated (Ed25519)
- [x] SSH agent configured and key loaded
- [x] SSH config file created
- [x] SSH permissions set correctly
- [x] GitHub host key accepted
- [x] Git user credentials configured
- [x] Git SSH URL alias configured
- [ ] SSH key added to GitHub **← DO THIS MANUALLY**
- [ ] SSH connection tested successfully
- [ ] Project pushed to GitHub

**Next Steps:**

1. Add your public key to GitHub (see "Manual Step: Add SSH Key to GitHub")
2. Test SSH connection: `ssh -T git@github.com`
3. Push your project: `git push -u origin main`

---

## Additional Resources

- GitHub SSH Docs: https://docs.github.com/en/authentication/connecting-to-github-with-ssh
- SSH Best Practices: https://www.ssh.com/academy/ssh/best-practices
- Git Documentation: https://git-scm.com/book/en/v2
- Ed25519 Algorithm: https://ed25519.cr.yp.to/

---

**Last Updated:** December 3, 2024
**User:** harish-pathak
**Email:** harish.pathak@infobeans.com
**Status:** Ready for GitHub key addition
