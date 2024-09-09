import sys
import asyncio
from gpt4all import GPT4All

# Initialize the GPT4All model
try:
    model = GPT4All("qwen2-1_5b-instruct-q4_0.gguf")  # Ensure the model file is in the same directory
except Exception as e:
    print("Failed to initialize GPT4All model:", e)
    sys.exit(1)

async def run_provider():
    try:
        with model.chat_session():
            response = model.generate(sys.argv[1], max_tokens=1024)
            print("GPT4All:", response)
    except Exception as e:
        print("GPT4All:", e)

async def main():
    try:
        await run_provider()
    except Exception as e:
        print("Error:", e)

if __name__ == "__main__":
    asyncio.run(main())