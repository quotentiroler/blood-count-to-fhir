import g4f
import sys
import asyncio

_providers = [
    g4f.Provider.Bing,
    g4f.Provider.GptGo,
    g4f.Provider.You,
]

async def run_provider(provider: g4f.Provider.BaseProvider):
    try:
        response = await g4f.ChatCompletion.create_async(
            model=g4f.models.default,
            messages=[{"role": "user", "content": sys.argv[1]}],
            provider=provider,
        )
        print(f"{provider.__name__}:", response)
        exit()
    except Exception as e:
        print(f"{provider.__name__}:", e)
        
async def run_all():
    calls = [
        run_provider(provider) for provider in _providers
    ]
    await asyncio.gather(*calls)

asyncio.run(run_all())
