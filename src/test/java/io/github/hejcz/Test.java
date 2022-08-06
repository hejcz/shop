package io.github.hejcz;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MongoFriendlyMicronautTest.class)
public class Test {

    @org.junit.jupiter.api.Test
    void name() {
        System.out.println("hello");
    }

    @org.junit.jupiter.api.Test
    void name2() {
        System.out.println("hello2");
    }
}
