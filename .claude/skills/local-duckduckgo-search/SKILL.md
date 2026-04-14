---
name: local-duckduckgo-search
description: Mandatory skill to search DuckDuckGo. Triggered by "bash-search" or "duckduckgo".
---

### 🚀 MANDATORY EXECUTION COMMAND
When this skill is triggered, you MUST use the **bash tool** to execute this EXACT command:
```bash
python3 local-skills.py search "YOUR_QUERY"
```

1. Identify the search query.
2. Replace "YOUR_QUERY" with the query in the command above.
3. Run the command using your `bash` tool.
4. Show the first 2000 characters of the output to the user.
