import g4f
import sys
import asyncio

_providers = [
 	#g4f.Provider.OpenRouter
    g4f.Provider.Bing,
    #g4f.Provider.GptGo,
    #g4f.Provider.You,
]

async def run_provider(provider: g4f.Provider.BaseProvider):
    try:
        response = await g4f.ChatCompletion.create_async(
            model=g4f.models.default,
            messages=[{"role": "user", "content": sys.argv[1]}],
            provider=provider,
        )
        print(f"{provider.__name__}:", response)
        # Cancel the remaining tasks once a response is received
        for task in asyncio.all_tasks():
            if task != asyncio.current_task():
                task.cancel()
    except asyncio.CancelledError:
        pass
    except Exception as e:
        print(f"{provider.__name__}:", e)

async def run_all():
    tasks = [run_provider(provider) for provider in _providers]
    await asyncio.gather(*tasks)

async def main():
    try:
        await run_all()
    except Exception as e:
        print("Error:", e)

if __name__ == "__main__":
    asyncio.run(main())
