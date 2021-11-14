# About
Double-array based aho-corasick automaton implementation, inspired by 
* https://www.linux.thai.net/~thep/datrie/
* https://github.com/robert-bor/aho-corasick

Typically, an aho-corasick automaton consists of 
1. trie nodes
2. failure pointer
3. outputs

This implementation fuse all above elements into the double array, resulting excellent query performance, especially for large documents.

However, the process of building such data structure is longer than implementations of linked nodes. Therefore it is suitable for scenario where build once, query all the time.

# Usage
## Build the Automaton
```
// build process
DATAutomaton.Builder builder = DATAutomaton.builder();
builder.add("he")
        .add("she")
        .add("say");
Automaton automaton = builder.build();
```
If you want to attach each keyword with a generic object
```
DATAutomaton.Builder<Object> builder = DATAutomaton.builder();
builder.put("he", obj1)
       .put("she", obj2)
       .put("say", obj3);
Automaton<Object> automaton = builder.build();
```
In above two cases, `allAll` and `putAll` is also provided to support collections.


## Query the Automaton

### 1. Generally collect all keywords encountered
```
List<Emit<V>> list = automaton.parse(text); 
```

### 2. Uniform callback machenism

```
MatchLister<V> listener = new MatchListener<V> {
  boolean onMatch(int start, int end, String key, V value) {
    // do something
    // ...
    return true;   // return false if you want to stop parseing half way
  }
};
automaton.parse(str, listener);
```

### 3. Stop parsing half way
If you want to stop parsing half way, say, test if a input document contains the word "demon" or not, and unnecessary to traverse the whole document, you can
```
boolean hasDemon = false;
MatchLister<V> listener = new MatchListener<V> {
  boolean onMatch(int start, int end, String key, V value) {
    if("demon".equals(key)) {
       hasDemon = true;
       return false;
    }
    return true;
  }
};
automaton.parse(document, listener); 
```
When the listener returns false, the parse function will find it and return immediately.


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

List<Emit<V>> list = automaton.parse(convertStr);
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

automaton.parse(new LowerCaseCS(text));

```

