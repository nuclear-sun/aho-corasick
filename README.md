English | [中文](README_CN.md)

# About
A double-array based aho-corasick automaton implementation, inspired by 
* https://www.linux.thai.net/~thep/datrie/
* https://github.com/robert-bor/aho-corasick

Typically, an aho-corasick automaton is mainly a trie, whose nodes are modified and consists of
1. goto table
2. failure pointer
3. output table

This implementation fuse all above elements into the double array, resulting excellent query performance, especially for large documents.

However, the process of building such data structure (DAT) is longer than implementations of linked nodes. Therefore it is suitable for scenario where build once, query all the time.

# Usage

## import by maven

```xml
        <dependency>
            <groupId>com.helipy.text</groupId>
            <artifactId>ahocorasick-doublearray</artifactId>
            <version>1.1.0</version>
        </dependency>
```

## Build the Automaton
```java
import com.helipy.text.ahocorasick.DatAutomaton;
import com.helipy.text.ahocorasick.Emit;

// build process
DatAutomaton.Builder<Void> builder = DatAutomaton.<Void>builder();
builder.add("she")
        .add("he")
        .add("say");
DatAutomaton<Void> automaton = builder.build();
```
If you want to attach each keyword with a generic object, such as a Float weight
```java
DatAutomaton.Builder<Float> builder = DatAutomaton.builder();
builder.put("he", 0.5f)
       .put("she", 0.6f)
       .put("say", 0.4f);

// get the associated object (since 1.1.0)
Float weight = builder.get("he");

Automaton<Float> automaton = builder.build();
```
In above two cases, `addAll` and `putAll` is also provided to support collections.


## Query the Automaton

### 1. Generally collect all keywords encountered
```
List<Emit<Void>> list = automaton.parseText(text); 

for (Emit<Void> emit : emitList) {
    // print matched keyword and location in text
    System.out.printf("%s %d %d%n", emit.getKeyword(), emit.getStart(), emit.getEnd());
}
```

if have attached a generic object for each keyword, such as a Float weight, the weight can fetch by Emit object.
```
List<Emit<Float>> emitList = automaton.parseText(text);
for (Emit<Float> emit : emitList) {
    // print matched keyword, location in text and attached weight
    System.out.printf("%s %d %d %f%n", emit.getKeyword(), emit.getStart(), emit.getEnd(), emit.getValue());
}
```

### 2. Uniform callback machenism

```
import com.helipy.text.ahocorasick.MatchHandler;

MatchHandler<V> handler = new MatchHandler<V> {
  boolean onMatch(int start, int end, String key, V value) {
    // do something
    // ...
    return true;   // return false if you want to stop parseing half way
  }
};
List<Emit<V>> emitList = automaton.parseText(text, handler);
```

### 3. Stop parsing half way
If you want to stop parsing half way, say, test if a input document contains the word "demon" or not, and unnecessary to traverse the whole document, you can
```
import com.helipy.text.ahocorasick.MatchHandler;

MatchHandler<V> handler = new MatchHandler<V> {

  boolean hasDemon = false;
  
  boolean onMatch(int start, int end, String key, V value) {
    if("demon".equals(key)) {
       hasDemon = true;
       return false;
    }
    return true;
  }
};
List<Emit<V>> emitList = automaton.parseText(document, handler); 
```
When the handler returns false, the parse function will find it and return immediately.


### 4. Skip some characters
Sometimes, the query text may contain special chars you want to ignore during parsing, for example:
"sh##e" needs to be matched as "she" if it is added during building process, you can convert special chars to '\0', because '\0' will be skipped during parsing.
```
String str = "sh##e";

CharSequence convertStr = new CharSequence() {

    @Override
    public int length() {
        return str.length();
    }

    @Override
    public char charAt(int index) {
        char ch = str.charAt(index);
        if(ch == '#') {
            return '\0';
        }
        return ch;
    }

    // this method will not be used, it is ok with no implementation
    @Override
    public CharSequence subSequence(int start, int end) {
        return null;
    }
};

List<Emit<V>> list = automaton.parseText(convertStr);
```

### 5. Convert characters before parsing
Overwrite `charAt` as above. Say, you are in case-insensitive situation:
```
class LowerCaseCS implements CharSequence {

    private final CharSequence text;
    
    public LowerCaseCS(CharSequence text) {
        this.text = text;
    }

    @Override
    public int length() {
        return text.length();
    }

    @Override
    public char charAt(int index) {
        char ch = text.charAt(index);
        if(ch >= 'A' && ch <= 'Z') {
            return ch - ('A' - 'a');
        }
        return ch;
    }

    // this method will not be used, it is ok with no implementation
    @Override
    public CharSequence subSequence(int start, int end) {
        return null;
    }

}

automaton.parseText(new LowerCaseCS(text));

```

## Other Features
### Thread Interruption
It is sometimes necessary to stop parsing even though the process has not complete yet, such as canceled query, shutdown:
```java
DatAutomaton.Builder<Void> builder = DatAutomaton.<>builder();
builder.setInterruptable(true)
        .add("he")
        .add("she")
        .add("say");
Automaton<Void> automaton = builder.build();
```
The `setInterruptable` method indicates whether this automaton listens on thread interruption:
if set true(default false), the automaton will stop parsing on thread interrupted(thread interruption status not reset).
In another thread, you can interrupt it explicitly:
```java
t1.interrupt(); // assume that the automaton works in thread t1.
```

# Performance
I tested the following cases on my laptop, Apple MacBook Pro 15.4 with 2.2Hz Intel Core i7, 16G memory.

| text length | query avg (ns) | query tp99 (ns) |
| ----------: | --------------:| ---------------:|
| 300 ~ 600   | 15077          | 53644           |
| 10k ~ 30k   | 714744         | 2162130         |
| 10m         | 704315370      | 704315370       |
| 20m         | 178099589      | 224774130       |

The table above shows that this implementation can process huge documents whose size of characters exceeds 
tens of millions within 1 second. 

A more dedicated benchmark compares average time consumption (nano seconds) for two implementation:

| Log10(text_length) | Linking Implementation | Double-Array Implementation |
| ----------: | --------------:| ---------------:|
| 1   | 5511          | 3609           |
| 2   | 19055         | 4918           |
| 3   | 253760        | 16289          |
| 4   | 1674961       | 132835         |

