import sys
import subprocess
import urllib.parse

def search(query):
    # Properly encode the query for the URL
    encoded_query = urllib.parse.quote_plus(query)
    url = f"https://duckduckgo.com/html/?q={encoded_query}"
    
    # Run curl to get the HTML
    try:
        result = subprocess.run(
            ["curl", "-s", "-L", url],
            capture_output=True,
            text=True,
            timeout=10
        )
        
        if result.returncode != 0:
            return f"Error: Curl failed with exit code {result.returncode}"
            
        # Basic parsing (extracting titles and snippets from DDG HTML-lite)
        # This is simple to keep it robust for local models
        content = result.stdout
        if "No results" in content:
            return "No results found."
            
        return content[:2000] # Return the first 2000 chars for Claude to parse
        
    except Exception as e:
        return f"Exception during search: {str(e)}"

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python search.py 'QUERY'")
        sys.exit(1)
        
    query = " ".join(sys.argv[1:])
    print(search(query))
