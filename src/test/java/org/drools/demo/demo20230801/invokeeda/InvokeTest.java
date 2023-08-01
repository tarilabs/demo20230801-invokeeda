package org.drools.demo.demo20230801.invokeeda;

import org.junit.jupiter.api.Test;

import jep.Interpreter;
import jep.SharedInterpreter;

public class InvokeTest {
    static { // TODO: limit of my Mac as LD_LIBRARYPATH is not correctly resolved?
        jep.MainInterpreter.setJepLibraryPath("/usr/local/lib/python3.11/site-packages/jep/libjep.jnilib");
    }

    @Test
    public void test101() {
        try (Interpreter interp = new SharedInterpreter()) {
            interp.exec("from java.lang import System");
            interp.exec("s = 'Hello World'");
            interp.exec("System.out.println(s)");
            interp.exec("print(s)");
            interp.exec("print(s[1:-1])");
        }
    }

    public static class HereIsJava {
        public void receive(Object payload) {
            System.out.println("Here is Java, received: "+payload.getClass());
            System.out.println("Here is Java, received: "+payload);
        }
    }

    @Test
    public void test102() {
        try (Interpreter interp = new SharedInterpreter()) {
            HereIsJava hereIsJavaInstance = new HereIsJava();
            interp.set("arg", hereIsJavaInstance);
            interp.exec("arg.receive([{\"i\": 1}, {\"f\": 3.14159}, {\"b\": False}])");
        }
    }

    @Test
    public void testEDA() {
        try (Interpreter interp = new SharedInterpreter()) {
            HereIsJava hereIsJavaInstance = new HereIsJava();
            interp.exec("""
                import runpy
                import asyncio
                from ansible_rulebook.collection import find_source

                class MioQueue:
                    def __init__(self, w):
                        self.wrapped = w

                    async def put_nowait(self: "MioQueue", event: dict) -> None:
                        self.wrapped.receive(event)

                    async def put(self: "MioQueue", event: dict) -> None:
                        self.wrapped.receive(event)
                """);
            interp.set("hereIsJavaInstance", hereIsJavaInstance);
            interp.exec("""
                source = find_source("ansible.eda", "generic")
                module = runpy.run_path(source)
                entrypoint = module["main"]
                asyncio.run(
                    entrypoint(
                        MioQueue(hereIsJavaInstance),
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
                """);
        }
    }
}
