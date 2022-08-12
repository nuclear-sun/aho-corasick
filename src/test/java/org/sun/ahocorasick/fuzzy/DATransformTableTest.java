package org.sun.ahocorasick.fuzzy;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DATransformTableTest {



    @Test
    public void testGetTransformedChars() {

        DATransformTable.Builder builder = DATransformTable.builder();

        String targets1 = "úùūézh";
        builder.putTransforms(3, 'u', targets1);

        String targets2 = "蜉蜡蜊蜞蜻蛸蜩蜓蜗蜒蜴蛹蝓";
        builder.putTransforms(10, '蜥', "蜉蜡蜊蜞蜻蛸蜩蜓蜗蜒蜴蛹蝓");

        String targets3 = "蒡蓓蔽薜蔡蔟荻蔸茯蒿荷蕻获蒺蒹蕨蔻蓠蓼蔺蒙蔷蓐蕤萨蓍蔌蒜萜葶蔚薤荇蓿蓣蔗";

        builder.putTransforms(10, '蓰', targets3);

        String targets4 = "锿铵钣钡锛铋钵钹铂钸钚钗铲钞铖铳锄钏锤锉错铛钿钓铞铫钉铤铥钭钝铎锇铒钒钫镄锋钆钙钢锆铬钩钴锅铪铧锪钬铗钾锏铰镜锔钜锩钧锎铠锴钪铐钶铿锟铼锒铹铑锂链钌铃锍铝锊锚铆钔锰铭钼钠铙铌钮锘钕钯铍钋钷铺钎铅钤钱钳锵钦锓铨铷锐铯铩钐铈铄锁铊钛钽铴铽锑铁铜钍钨锨销锌锈铉钥铘铱钇镒铟银铀铕钰钺锃铡钊针钲铮钟锺铢铸锥钻";
        builder.putTransforms(3, '铣', targets4);

        DATransformTable transformTable = builder.build();

        CharSequence result1 = transformTable.getTransformedChars(3, 'u');
        assertEquals(result1, targets1);

        CharSequence result2 = transformTable.getTransformedChars(10, '蜥');
        assertEquals(result2, targets2);

        CharSequence result3 = transformTable.getTransformedChars(10, '蓰');
        assertEquals(result3, targets3);

        CharSequence result4 = transformTable.getTransformedChars(3, '铣');
        assertEquals(result4, targets4);

    }

}