[English](README.md) | 中文

# 关于
基于双数组的AC自动机实现,收到下面两个项目的启发完成
* https://www.linux.thai.net/~thep/datrie/
* https://github.com/robert-bor/aho-corasick

通常,一个 AC自动机是一个 trie 结构,节点状态由下面几种结构修改
1. goto 表(goto table)
2. 失败指针(failure pointer)
3. 输出表(output table)

这个实现在双数组上重用了上面所有的元素,达到了卓越的查询性能,尤其是对大文件的查询.

然而,构造这个数据结构(Trie)的过程比链接表要慢,因此,这个实现更适用于一次构建,多次查询的场景.

# 用法
## 构造自动机
```
// 构造过程
DATAutomaton.Builder builder = DATAutomaton.builder();
builder.add("he")
        .add("she")
        .add("say");
Automaton automaton = builder.build();
```

如果需要对每个关键词关联一个对象, 例如一个 Float 的权重
```
DATAutomaton.Builder<Float> builder = DATAutomaton.builder();
builder.put("he", 0.5f)
       .put("she", 0.6f)
       .put("say", 0.4f);
Automaton<Float> automaton = builder.build();
```
也可以使用 `addAll`, `putAll` 一次添加一组关键词.

## 查询自动机

### 1. 收集所有命中的关键词
```
List<Emit<V>> list = automaton.parseText(text);

for (Emit<Void> emit : emitList) {
    // 打印命中的关键词和在文本中的位置
    System.out.printf("%s %d %d%n", emit.getKeyword(), emit.getStart(), emit.getEnd());
}
```

如果给关键词关联了一个对象,例如一个 Float 的权重
```
List<Emit<Float>> emitList = automaton.parseText(text);
for (Emit<Float> emit : emitList) {
    // 打印命中的关键词,在文本中的位置,以及关联的权重
    System.out.printf("%s %d %d %f%n", emit.getKeyword(), emit.getStart(), emit.getEnd(), emit.getValue());
}
```

### 2. 为匹配命中配置回调机制

```
MatchHandler<V> handler = new MatchHandler<V> {
  boolean onMatch(int start, int end, String key, V value) {
    // 处理逻辑
    // ...
    return true;   // 如果希望命中后停止解析,请 return false
  }
};
automaton.parseText(text, handler);
```

### 3. 中途停止解析
如果希望中途停止解析,例如,测试一个输入文档是否包含 "demon",这并不需要遍历整个文档,只需要在发现 "demon"时停止.
```
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
automaton.parseText(document, handler); 
```
当 handler 返回 false, parse 函数将在找到这个关键词后立即返回.


### 4. 跳过一些字符
有时,查询字符串包含一些你希望忽略的字符,例如:
"sh##e" 需要被当作 "she" 来处理,而你在构造自动机时添加了关键词 "she",你可以把特殊字符转换为 '\0', 因为 '\0'在解析时会被跳过.
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

### 5. 在解析前转换字符
像上面一样重载 `charAt` 函数. 例如,对于大小写不敏感的场景,可以像下面这样做:
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

    // 这个方法不会被使用,不实现它是可以的
    @Override
    public CharSequence subSequence(int start, int end) {
        return null;
    }

}

automaton.parseText(new LowerCaseCS(text));

```

## 其他特性
### 线程中断
有时中断查询过程是有必要的，如超时取消，关机等，可以按照如下方式设置：
```java
DATAutomaton.Builder builder = DATAutomaton.builder();
builder.setInterruptable(true)  // 设置可以被中断
        .add("he")
        .add("she")
        .add("say");
Automaton automaton = builder.build();
```
`setInterruptable` 方法表示该自动机是否可以监听线程中断信号。如果设置为 true（默认为 false）, 当线程被中断时会停止查询（线程中断状态会被保留）。
在另一个线程中，可以这样中断该自动机：
```java
t1.interrupt(); // 假定该自动机工作的线程为 t1
```


# 性能
我在我的笔记本上测试了性能,我的机器配置为 Apple MacBook Pro 15.4, CPU:2.2Hz Intel Core i7, 内存:16G, 结果如下:

| text length | query avg (ns) | query tp99 (ns) |
| ----------: | --------------:| ---------------:|
| 300 ~ 600   | 15077          | 53644           |
| 10k ~ 30k   | 714744         | 2162130         |
| 10m         | 704315370      | 704315370       |
| 20m         | 178099589      | 224774130       |

上表表明,这个实现可以在1秒内查询千万级字符.

一个更专业的基准测试比较了两种实现(链接表/双数组)的查询用时(纳秒)

| Log10(text_length) | Linking Implementation | Double-Array Implementation |
| ----------: | --------------:| ---------------:|
| 1   | 5511          | 3609           |
| 2   | 19055         | 4918           |
| 3   | 253760        | 16289          |
| 4   | 1674961       | 132835         |

