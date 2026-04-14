import sys
import subprocess
import urllib.parse
import json

def ddg_search(query):
    encoded_query = urllib.parse.quote_plus(query)
    url = f"https://duckduckgo.com/html/?q={encoded_query}"
    try:
        result = subprocess.run(["curl", "-s", "-L", url], capture_output=True, text=True, timeout=10)
        return result.stdout[:2000] if result.returncode == 0 else f"Error: {result.stderr}"
    except Exception as e:
        return f"Error: {str(e)}"

def ollama_summarize(text, model="llama3.1"):
    try:
        data = json.dumps({"model": model, "prompt": f"Summarize concisely:\n{text}", "stream": False})
        result = subprocess.run(
            ["curl", "-s", "-X", "POST", "http://localhost:11434/api/generate", "-d", data],
            capture_output=True, text=True, timeout=30
        )
        if result.returncode == 0:
            return json.loads(result.stdout).get("response", "No response field found.")
        return f"Error: {result.stderr}"
    except Exception as e:
        return f"Error: {str(e)}"

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python3 local-skills.py [search|summarize] [input]")
        sys.exit(1)
    
    command = sys.argv[1]
    data = " ".join(sys.argv[2:])
    
    if command == "search":
        print(ddg_search(data))
    elif command == "summarize":
        print(ollama_summarize(data))
    else:
        print(f"Unknown command: {command}")
