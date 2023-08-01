import runpy
import asyncio
from ansible_rulebook.collection import (
    find_collection,
    find_playbook,
    find_source,
    has_playbook,
    has_rulebook,
    load_rulebook,
    split_collection_name,
)

class MockQueue:
    async def put_nowait(self: "MockQueue", event: dict) -> None:
        print(event)  # noqa: T201
    async def put(self: "MockQueue", event: dict) -> None:
        print(event)  # noqa: T201

source = find_source("ansible.eda", "generic")
module = runpy.run_path(source)
entrypoint = module["main"]
asyncio.run(
    entrypoint(
        MockQueue(),
        {
            "randomize": True,
            "startup_delay": 1,
            "create_index": "my_index",
            "loop_count": 2,
            "repeat_count": 2,
            "repeat_delay": 1,
            "event_delay": 2,
            "loop_delay": 3,
            "shutdown_after": 11,
            "timestamp": True,
            "display": True,
            "payload": [{"i": 1}, {"f": 3.14159}, {"b": False}],
        },
    ),
)
