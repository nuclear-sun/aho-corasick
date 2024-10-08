package com.helipy.text.ahocorasick;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Pack:       com.kanzhun.fc.common.text.ahocorasick
 * File:       DatAutomatonTest
 * Desc:
 *
 * @author wangchuangfeng
 * CreateTime: 2024-01-08 16:28
 */
public class DatAutomatonChineseTest {

    @Test
    void parseText1() {
        List<String> wordList = Lists.newArrayList("开发经验", "数据库", "java开发", "java", "开发工程师", "计算机", "软件", "开发工程", "分布式", "后端", "java开发经验", "架构设计", "后端开发", "编程", "软件工程", "微服务", "后端工程师", "后端工程", "java开发工程师", "java开发工程", "设计模式", "分布式系统", "沟通能力", "java基础", "工作经验", "多线程", "spring", "计算机相关", "系统开发", "计算机相关专业", "3-5年", "软件开发", "mysql", "系统设计", "中间件", "需求分析", "数据结构", "统设计", "开源框架", "据结构", "分布式技术", "服务架构", "团队合作", "项目开发", "微服务架构", "mybatis", "设计经验", "开发框架", "相关技术", "系统开发经验", "java基础扎实", "系统架构", "1-3年", "高并发", "大数据", "基础扎实", "软件工程相关", "redis数据库", "服务器", "计算机/软件工程", "计算机/软件工程相关经验", "工程相关", "springboot", "性能优化", "团队协作", "sql", "前端", "业务需求", "性能优", "架构设计经验", "java语言", "团队合作精神", "性能调优", "分布式系统开发", "设计能力", "研发工", "算法", "软件工程师", "linux", "架构师", "高性能", "稳定性", "分布式系统开发经验", "互联网", "代码编写", "经验不限", "技术文档", "技术方案", "合作精神", "后端开发工程师", "数据库设计", "关系型数据", "关系型数据库", "mysql数据库", "解决方案", "大型项目", "开发技术", "设计开发", "协作能力", "spring cloud", "应用开发", "项目开发经验", "mysql数据", "redis", "springcloud", "springmvc", "java编程", "web", "jvm", "项目经验", "面向对象", "单元测试", "web开发", "操作系", "编程语言", "作系统", "项目架构", "文档编写", "解决问题的能力", "中大型项目", "消息队列", "全栈工程师", "全栈工程", "项目架构设计", "高可用", "系统架构设计", "jvm原理", "高级java", "研发经验", "spring boot", "业务系统", "java工程师", "居家办公", "常用设计模式", "编程基础", "常用设计", "管理经验", "中大型项目架构设计经验", "开发语言", "全栈", "数据处理", "团队协作能力", "前端开发", "基础知识", "详细设计", "技术架构", "代码开发", "java工程", "系统分析", "python", "开发任务", "linux操作", "设计文档", "开发工具", "mvc", "主流数据库", "软件开发工程", "软件开发工程师", "linux操作系统", "流数据", "hibernate", "掌握spring", "相关工作经验", "spring mvc", "计算机软件", "多线程编程", "应用服务", "高级java开发", "产品需求", "c++", "系统性能", "核心代码", "java技术", "sql优化", "计算机软", "方案设计", "功能模块", "团队管理", "mvc开发", "dubbo", "应用服务器", "项目管理", "表达能力", "消息中间件", "c/c++", "javascript", "代码质量", "精通java", "模块设计", "服务开发", "高级开发", "数据处", "前端技术", "软件设计", "大数据处理", "核心模块", "oracle", "相关文档", "抗压能力", "服务框架", "高可用性", "高级开发工程师", "优化经验", "软件产品", "产品开发", "思维能力", "研发工程师", "团队管理经验", "全日制", "主流框架", "nosql", "使用java", "对象设计", "数据库开发", "研发工程", "线程编程", "逻辑思维能力", "技术框架", "java高级", "服务端", "逻辑思维", "常用数据", "开发规范", "运维", "服务器配置", "软件系统", "开发流程", "程序设计", "网络协议", "操作系统", "分析能力", "高级软件", "应用系统", "精通java语言", "协调能力", "技术难点", "统招本科", "测试工", "网络编程", "数据结构和算法", "软件开发经验", "数据处理经验", "面向对象设计", "软件系", "微服务框架", "网络编", "java编程基础", "功能开发", "技术栈", "j2ee", "web应用", "理解业务", "kafka", "项目需求");
        String text = "岗位职责：\n" +
                "1、按照产品经理的需求设计、协调并完成相应产品功能的研发任务；\n" +
                "2、负责管理团队，对系统的架构调研、设计和开发；\n" +
                "3、管理团队工作进度，合理分配任务，并深入到关键模块细节，并对产品质量负责；\n" +
                "4、负责解决团队所遇重大技术问题，并对结果负责。\n" +
                "5、研究前沿互联网技术，行业顶尖解决方案，落实到本团队工作中；\n" +
                "6、负责指导培训团队，并同兄弟团队保持良好协助。\n" +
                "任职要求：\n" +
                "1、年龄25-35岁，计算机或相关专业本科以上学历，5年以上工作经验，3年以上团队管理经验，有知名互联网开发或金融系统经验者优先；\n" +
                "2、熟悉互联网服务架构体系，精通服务端的整体构建及交互流程；\n" +
                "3、精通互联网系统安全设计，并有实质的解决办法；\n" +
                "4、精通mysql等数据库的使用、精通SQL及其调优；具备mongoDB,redis,Memcached等NOSQL,缓存的使用，优化的相关工作经验；\n" +
                "5、精通springMVC,ibatis/mybatis,spring等常用开源框架的使用；\n" +
                "6、精通tomcat，Nginx的使用和调优、有Web Service，RESTful等WEB服务的构建和开发经验；\n" +
                "7、熟练使用设计模式，有JAVA调优，JVM优化的相关经验；\n" +
                "8、熟悉linux系统的相关命令及shell编程。";
        DatAutomaton.Builder<Void> builder = DatAutomaton.builder();
        wordList.forEach(word -> builder.add(word));
        DatAutomaton<Void> automaton = builder.build();
        List<Emit<Void>> emitList = automaton.parseText(text);
        for (Emit<Void> emit : emitList) {
            System.out.printf("%s %d %d%n", emit.getKeyword(), emit.getStart(), emit.getEnd());
        }
    }

    @Test
    public void parseTextTest3() {
        List<Emit<Void>> results = new LinkedList<>();
        MatchHandler<Void> listener = new MatchHandler<Void>() {
            @Override
            public boolean onMatch(int start, int end, String key, Void value) {
                Emit<Void> emit = new Emit<>(key, start, end, value);
                results.add(emit);
                return false;
            }
        };

        List<String> wordList = Lists.newArrayList("开发经验", "数据库", "java开发", "java", "开发工程师", "计算机", "软件", "开发工程", "分布式", "后端", "java开发经验", "架构设计", "后端开发", "编程", "软件工程", "微服务", "后端工程师", "后端工程", "java开发工程师", "java开发工程", "设计模式", "分布式系统", "沟通能力", "java基础", "工作经验", "多线程", "spring", "计算机相关", "系统开发", "计算机相关专业", "3-5年", "软件开发", "mysql", "系统设计", "中间件", "需求分析", "数据结构", "统设计", "开源框架", "据结构", "分布式技术", "服务架构", "团队合作", "项目开发", "微服务架构", "mybatis", "设计经验", "开发框架", "相关技术", "系统开发经验", "java基础扎实", "系统架构", "1-3年", "高并发", "大数据", "基础扎实", "软件工程相关", "redis数据库", "服务器", "计算机/软件工程", "计算机/软件工程相关经验", "工程相关", "springboot", "性能优化", "团队协作", "sql", "前端", "业务需求", "性能优", "架构设计经验", "java语言", "团队合作精神", "性能调优", "分布式系统开发", "设计能力", "研发工", "算法", "软件工程师", "linux", "架构师", "高性能", "稳定性", "分布式系统开发经验", "互联网", "代码编写", "经验不限", "技术文档", "技术方案", "合作精神", "后端开发工程师", "数据库设计", "关系型数据", "关系型数据库", "mysql数据库", "解决方案", "大型项目", "开发技术", "设计开发", "协作能力", "spring cloud", "应用开发", "项目开发经验", "mysql数据", "redis", "springcloud", "springmvc", "java编程", "web", "jvm", "项目经验", "面向对象", "单元测试", "web开发", "操作系", "编程语言", "作系统", "项目架构", "文档编写", "解决问题的能力", "中大型项目", "消息队列", "全栈工程师", "全栈工程", "项目架构设计", "高可用", "系统架构设计", "jvm原理", "高级java", "研发经验", "spring boot", "业务系统", "java工程师", "居家办公", "常用设计模式", "编程基础", "常用设计", "管理经验", "中大型项目架构设计经验", "开发语言", "全栈", "数据处理", "团队协作能力", "前端开发", "基础知识", "详细设计", "技术架构", "代码开发", "java工程", "系统分析", "python", "开发任务", "linux操作", "设计文档", "开发工具", "mvc", "主流数据库", "软件开发工程", "软件开发工程师", "linux操作系统", "流数据", "hibernate", "掌握spring", "相关工作经验", "spring mvc", "计算机软件", "多线程编程", "应用服务", "高级java开发", "产品需求", "c++", "系统性能", "核心代码", "java技术", "sql优化", "计算机软", "方案设计", "功能模块", "团队管理", "mvc开发", "dubbo", "应用服务器", "项目管理", "表达能力", "消息中间件", "c/c++", "javascript", "代码质量", "精通java", "模块设计", "服务开发", "高级开发", "数据处", "前端技术", "软件设计", "大数据处理", "核心模块", "oracle", "相关文档", "抗压能力", "服务框架", "高可用性", "高级开发工程师", "优化经验", "软件产品", "产品开发", "思维能力", "研发工程师", "团队管理经验", "全日制", "主流框架", "nosql", "使用java", "对象设计", "数据库开发", "研发工程", "线程编程", "逻辑思维能力", "技术框架", "java高级", "服务端", "逻辑思维", "常用数据", "开发规范", "运维", "服务器配置", "软件系统", "开发流程", "程序设计", "网络协议", "操作系统", "分析能力", "高级软件", "应用系统", "精通java语言", "协调能力", "技术难点", "统招本科", "测试工", "网络编程", "数据结构和算法", "软件开发经验", "数据处理经验", "面向对象设计", "软件系", "微服务框架", "网络编", "java编程基础", "功能开发", "技术栈", "j2ee", "web应用", "理解业务", "kafka", "项目需求");
        String text = "岗位职责：\n" +
                "1、按照产品经理的需求设计、协调并完成相应产品功能的研发任务；\n" +
                "2、负责管理团队，对系统的架构调研、设计和开发；\n" +
                "3、管理团队工作进度，合理分配任务，并深入到关键模块细节，并对产品质量负责；\n" +
                "4、负责解决团队所遇重大技术问题，并对结果负责。\n" +
                "5、研究前沿互联网技术，行业顶尖解决方案，落实到本团队工作中；\n" +
                "6、负责指导培训团队，并同兄弟团队保持良好协助。\n" +
                "任职要求：\n" +
                "1、年龄25-35岁，计算机或相关专业本科以上学历，5年以上工作经验，3年以上团队管理经验，有知名互联网开发或金融系统经验者优先；\n" +
                "2、熟悉互联网服务架构体系，精通服务端的整体构建及交互流程；\n" +
                "3、精通互联网系统安全设计，并有实质的解决办法；\n" +
                "4、精通mysql等数据库的使用、精通SQL及其调优；具备mongoDB,redis,Memcached等NOSQL,缓存的使用，优化的相关工作经验；\n" +
                "5、精通springMVC,ibatis/mybatis,spring等常用开源框架的使用；\n" +
                "6、精通tomcat，Nginx的使用和调优、有Web Service，RESTful等WEB服务的构建和开发经验；\n" +
                "7、熟练使用设计模式，有JAVA调优，JVM优化的相关经验；\n" +
                "8、熟悉linux系统的相关命令及shell编程。";
        DatAutomaton.Builder<Void> builder = DatAutomaton.builder();
        wordList.forEach(word -> builder.add(word));
        DatAutomaton<Void> automaton = builder.build();
        automaton.parseText(text, listener);

        for (Emit<Void> emit : results) {
            System.out.printf("%s %d %d%n", emit.getKeyword(), emit.getStart(), emit.getEnd());
        }
        /* output:
         * 互联网 133 136
         */
    }

    @Test
    public void parseTextTest4() {
        String wordDict1 = "上海虹桥\t{\"province\": \"上海\", \"city\": \"上海\", \"abroad\": \"0\", \"airportCode\": \"SHA\", \"country\": \"中国\"}\n" +
                "上海虹桥机场\t{\"province\": \"上海\", \"city\": \"上海\", \"abroad\": \"0\", \"airportCode\": \"SHA\", \"country\": \"中国\"}\n" +
                "阜阳机场\t{\"province\": \"安徽\", \"city\": \"阜阳\", \"abroad\": \"0\", \"airportCode\": \"FUG\", \"country\": \"中国\"}\n" +
                "邯郸机场\t{\"province\": \"河北\", \"city\": \"邯郸\", \"abroad\": \"0\", \"airportCode\": \"HDG\", \"country\": \"中国\"}\n" +
                "张家界机场\t{\"province\": \"湖南\", \"city\": \"张家界\", \"abroad\": \"0\", \"airportCode\": \"DYG\", \"country\": \"中国\"}\n" +
                "固原六盘山机场\t{\"province\": \"宁夏\", \"city\": \"固原\", \"abroad\": \"0\", \"airportCode\": \"GYU\", \"country\": \"中国\"}\n" +
                "惠州惠东机场\t{\"province\": \"广东\", \"city\": \"惠州\", \"abroad\": \"0\", \"airportCode\": \"HUZ\", \"country\": \"中国\"}\n" +
                "舟山普陀山机场\t{\"province\": \"浙江\", \"city\": \"舟山\", \"abroad\": \"0\", \"airportCode\": \"HSN\", \"country\": \"中国\"}\n" +
                "庐山机场\t{\"province\": \"江西\", \"city\": \"庐山\", \"abroad\": \"0\", \"airportCode\": \"LUZ\", \"country\": \"中国\"}\n" +
                "路桥机场\t{\"province\": \"浙江\", \"city\": \"台州\", \"abroad\": \"0\", \"airportCode\": \"HYN\", \"country\": \"中国\"}\n" +
                "永强机场\t{\"province\": \"浙江\", \"city\": \"温州\", \"abroad\": \"0\", \"airportCode\": \"WNZ\", \"country\": \"中国\"}";
        String wordDict2 = "上海\t{\"province\": \"上海\", \"city\": \"上海\", \"lgt_baidu\": \"121.462096\", \"name\": \"上海\", \"district\": \"闸北区\", \"city_or_subcity\": \"1\", \"lat_baidu\": \"31.256071 \"}\n" +
                "上海站\t{\"province\": \"上海\", \"city\": \"上海\", \"lgt_baidu\": \"121.462096\", \"name\": \"上海\", \"district\": \"闸北区\", \"city_or_subcity\": \"1\", \"lat_baidu\": \"31.256071 \"}\n" +
                "上海南\t{\"province\": \"上海\", \"city\": \"上海\", \"lgt_baidu\": \"121.435774\", \"name\": \"上海南\", \"district\": \"徐汇区\", \"city_or_subcity\": \"1\", \"lat_baidu\": \"31.159523 \"}\n" +
                "上海南站\t{\"province\": \"上海\", \"city\": \"上海\", \"lgt_baidu\": \"121.435774\", \"name\": \"上海南\", \"district\": \"徐汇区\", \"city_or_subcity\": \"1\", \"lat_baidu\": \"31.159523 \"}\n" +
                "上海虹桥\t{\"province\": \"上海\", \"city\": \"上海\", \"lgt_baidu\": \"121.326321\", \"name\": \"上海虹桥\", \"district\": \"闵行区\", \"city_or_subcity\": \"1\", \"lat_baidu\": \"31.200456 \"}\n" +
                "上海虹桥站\t{\"province\": \"上海\", \"city\": \"上海\", \"lgt_baidu\": \"121.326321\", \"name\": \"上海虹桥\", \"district\": \"闵行区\", \"city_or_subcity\": \"1\", \"lat_baidu\": \"31.200456 \"}\n" +
                "上海西\t{\"province\": \"上海\", \"city\": \"上海\", \"lgt_baidu\": \"121.409451\", \"name\": \"上海西\", \"district\": \"普陀区\", \"city_or_subcity\": \"1\", \"lat_baidu\": \"31.268828 \"}\n" +
                "上海西站\t{\"province\": \"上海\", \"city\": \"上海\", \"lgt_baidu\": \"121.409451\", \"name\": \"上海西\", \"district\": \"普陀区\", \"city_or_subcity\": \"1\", \"lat_baidu\": \"31.268828 \"}\n" +
                "天津北\t{\"province\": \"天津\", \"city\": \"天津\", \"lgt_baidu\": \"117.215953\", \"name\": \"天津北\", \"district\": \"河北区\", \"city_or_subcity\": \"1\", \"lat_baidu\": \"39.172530 \"}";

        DatAutomaton.Builder<Map<String, JSONObject>> builder = DatAutomaton.<Map<String, List<JSONObject>>>builder();

        for (String line : Splitter.on("\n").split(wordDict1)) {
            List<String> pieces = Splitter.on("\t").splitToList(line);
            String word = pieces.get(0);
            JSONObject jsonObject = JSON.parseObject(pieces.get(1));
            Map<String, JSONObject> obj = builder.get(word);
            if (obj == null) {
                Map<String, JSONObject> newMap = Maps.newHashMap();
                newMap.put("airport", jsonObject);
                builder.put(word, newMap);
            } else {
                obj.put("airport", jsonObject);
            }
        }

        for (String line : Splitter.on("\n").split(wordDict2)) {
            List<String> pieces = Splitter.on("\t").splitToList(line);
            String word = pieces.get(0);
            JSONObject jsonObject = JSON.parseObject(pieces.get(1));
            Map<String, JSONObject> obj = builder.get(word);
            if (obj == null) {
                Map<String, JSONObject> newMap = Maps.newHashMap();
                newMap.put("trainStation", jsonObject);
                builder.put(word, newMap);
            } else {
                obj.put("trainStation", jsonObject);
            }
        }

        String text = "我想去上海虹桥,怎么走?";
        DatAutomaton<Map<String, JSONObject>> automaton = builder.build();
        List<Emit<Map<String, JSONObject>>> emitList = automaton.parseText(text);

        for (Emit<Map<String, JSONObject>> emit : emitList) {
            System.out.printf("word:%s, start:%d, end:%d, obj:%s%n",
                    emit.getKeyword(), emit.getStart(), emit.getEnd(), JSON.toJSONString(emit.getValue()));
        }
        /* output:
         * word:上海, start:3, end:5, obj:{"trainStation":{"province":"上海","city":"上海","lgt_baidu":"121.462096","name":"上海","district":"闸北区","city_or_subcity":"1","lat_baidu":"31.256071 "}}
         * word:上海虹桥, start:3, end:7, obj:{"trainStation":{"province":"上海","city":"上海","lgt_baidu":"121.326321","name":"上海虹桥","district":"闵行区","city_or_subcity":"1","lat_baidu":"31.200456 "},"airport":{"province":"上海","city":"上海","abroad":"0","airportCode":"SHA","country":"中国"}}
         * */
    }
}