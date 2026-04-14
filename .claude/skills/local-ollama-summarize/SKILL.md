---
name: local-ollama-summarize
description: Mandatory skill to summarize via local Llama3.1/Ollama. Triggered by "summarize with ollama" or "summarize with llama".
---

### 🚀 MANDATORY EXECUTION COMMAND
When this skill is triggered, you MUST use the **bash tool** to execute this EXACT command:
```bash
python3 local-skills.py summarize "YOUR_TEXT"
```

1. Identify the text to summarize.
2. Run the command using your `bash` tool.
3. Show the output to the user.
