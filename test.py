from g4f.client import Client

client = Client()

chat_completion = client.chat.completions.create(model="gpt-3.5-turbo",
    messages=[{"role": "user", "content": "What comes after 3?"}], stream=True)

for completion in chat_completion:
    print(completion.choices[0].delta.content or "", end="", flush=True)