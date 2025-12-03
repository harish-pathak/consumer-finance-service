# GitHub SSH Setup Guide

## Overview
This guide documents all steps needed to set up GitHub SSH access on your macOS machine and prepare it for pushing projects to GitHub.

---

## Prerequisites
- macOS with Terminal (zsh)
- Git installed (verify with `git --version`)
- GitHub account with admin or SSH access

---

## Step 1: Check Existing SSH Keys

```bash
ls -la ~/.ssh
```

**Expected Output:** You should see `id_ed25519` and `id_ed25519.pub` files if keys already exist.

---

## Step 2: Generate SSH Key Pair (if needed)

If you don't have SSH keys, generate a new Ed25519 key pair:

```bash
ssh-keygen -t ed25519 -C "harish.pathak@infobeans.com" -N "" -f ~/.ssh/id_ed25519
```

**Parameters explained:**
- `-t ed25519`: Uses Ed25519 algorithm (more secure than RSA)
- `-C`: Comment/email for identification
- `-N ""`: No passphrase (press Enter if prompted)
- `-f`: File path for the key

**Output:** You'll see a key fingerprint and randomart image confirming successful generation.

---

## Step 3: Display Your Public Key

```bash
cat ~/.ssh/id_ed25519.pub
```

**Save this output** - you'll need it for GitHub.

**Your Public Key:**
```
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIPBlvzAGUePUIcyjRD2T6Os/QSvFCyuAPVc1qRf5MyIu harish.pathak@infobeans.com
```

---

## Step 4: Configure SSH Agent

### Start SSH Agent and Add Your Key

```bash
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519
```

**Expected Output:**
```
Agent pid XXXXX
Identity added: /Users/harish.pathak/.ssh/id_ed25519 (harish.pathak@infobeans.com)
```

### Create SSH Config File

Create/edit `~/.ssh/config`:

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

**Permissions:** Make sure config has correct permissions:

```bash
chmod 600 ~/.ssh/config
chmod 600 ~/.ssh/id_ed25519
chmod 644 ~/.ssh/id_ed25519.pub
```

---

## Step 5: Add SSH Key to GitHub (Manual Step)

### Via Web Interface

1. Go to GitHub Settings: https://github.com/settings/keys
2. Click the **"New SSH key"** button (top right)
3. Fill in the form:
   - **Title:** "MacBook SSH Key" (or any descriptive name)
   - **Key type:** "Authentication Key"
   - **Key:** Paste your entire public key (from Step 3)
4. Click **"Add SSH key"**
5. You may be prompted to confirm with your GitHub password

---

## Step 6: Accept GitHub's Host Key

```bash
ssh-keyscan -H github.com >> ~/.ssh/known_hosts 2>/dev/null
```

---

## Step 7: Test SSH Connection to GitHub

```bash
ssh -T git@github.com
```

**Expected Output:**
```
Hi <username>! You've successfully authenticated, but GitHub does not provide shell access.
```

**If you see an error** like "Permission denied (publickey)":
- Verify the SSH key was added to GitHub correctly
- Double-check the public key format
- Wait a few minutes for GitHub to sync

---

## Step 8: Configure Git Credentials

### Set Your Git User Name and Email

```bash
git config --global user.name "harish-pathak"
git config --global user.email "harish.pathak@infobeans.com"
```

### Verify Configuration

```bash
git config --global --list
```

**Expected output should include:**
```
user.name=harish-pathak
user.email=harish.pathak@infobeans.com
```

---

## Step 9: Configure Git to Use SSH for GitHub

```bash
git config --global url."git@github.com:".insteadOf "https://github.com/"
```

This ensures Git uses SSH instead of HTTPS when cloning from GitHub.

---

## Step 10: Clone or Push Your Project

### For a New Repository

```bash
# Initialize local repo
cd /path/to/consumer-finance-service
git init
git add .
git commit -m "Initial commit"

# Add remote (using SSH)
git remote add origin git@github.com:harish-pathak/consumer-finance-service.git

# Push to GitHub
git branch -M main
git push -u origin main
```

### For an Existing Repository

```bash
cd /path/to/consumer-finance-service
git remote add origin git@github.com:harish-pathak/consumer-finance-service.git
git branch -M main
git push -u origin main
```

---

## Troubleshooting

### Issue: "Permission denied (publickey)"

**Solutions:**
1. Verify SSH key exists: `ls ~/.ssh/id_ed25519`
2. Check GitHub has the correct public key
3. Verify SSH agent has your key: `ssh-add -l`
4. Re-add key if needed: `ssh-add ~/.ssh/id_ed25519`

### Issue: "Host key verification failed"

**Solution:**
```bash
ssh-keyscan -H github.com >> ~/.ssh/known_hosts
```

### Issue: "Could not open a connection to your authentication agent"

**Solution:**
```bash
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519
```

### View Current SSH Configuration

```bash
# Check SSH keys
ssh-add -l

# Check SSH config
cat ~/.ssh/config

# Check git config
git config --global --list
```

---

## Security Best Practices

1. **Never share your private key** (`id_ed25519`)
2. **Keep your public key safe** from unauthorized users
3. **Use a passphrase** for production machines (optional, not used here)
4. **Rotate SSH keys** periodically (yearly recommended)
5. **Monitor SSH key usage** in GitHub Settings

---

## Useful Git Commands

```bash
# Clone with SSH
git clone git@github.com:harish-pathak/repo.git

# Change existing remote to SSH
git remote set-url origin git@github.com:harish-pathak/repo.git

# View current remotes
git remote -v

# Push all branches
git push --all

# Delete remote branch
git push origin --delete branch-name
```

---

## Summary Checklist

- [ ] SSH key pair generated (`~/.ssh/id_ed25519`)
- [ ] SSH config file created (`~/.ssh/config`)
- [ ] SSH key added to GitHub (https://github.com/settings/keys)
- [ ] SSH connection tested (`ssh -T git@github.com`)
- [ ] GitHub host key accepted
- [ ] Git user name configured globally
- [ ] Git user email configured globally
- [ ] Git SSH url alias configured
- [ ] Ready to clone/push projects

---

## Quick Reference

```bash
# View your SSH public key
cat ~/.ssh/id_ed25519.pub

# Start SSH agent
eval "$(ssh-agent -s)"

# Test GitHub connection
ssh -T git@github.com

# View Git global config
git config --global --list

# Clone a repo via SSH
git clone git@github.com:harish-pathak/repo.git
```

---

**Last Updated:** December 3, 2024
**Setup for:** harish-pathak
**Email:** harish.pathak@infobeans.com
**GitHub Username:** harish-pathak
