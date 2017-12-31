package org.mac;

/**
 * Created by Liang Hao on 2017/12/29.
 */
public class Demo {

    private static final String started = "\\u0002 M65UFJ4L5VNGTO5CDXVE04D5KLZR5EL4C69LBKSL1LBT8VSRWX1LQS2Y5IAPPXET";
    private static final String sstarted = "\\u0001       p\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000!\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0002\\u0001 #\\u0000\\u0004\\u0001\\u0001\\u0002\\u0002 M65UFJ4L5VNGTO5CDXVE04D5KLZR5EL4C69LBKSL1LBT8VSRWX1LQS2Y5IAPPXET";


    public static void main(String[] args) {
        Long val = Long.valueOf("1514544091975") - Long.valueOf("1514544090695");
        //System.out.println("start:" + decodeUnicode(sstarted));
        //PerformanceAnalysisUtils.throughput(Long.valueOf("1514544091975"),123,val/1000.0);
        PerformanceAnalysisUtils.useRate(1234/1000,System.currentTimeMillis()-2000);
    }

    public static String decodeUnicode(final String dataStr) {
        //String[] strs = dataStr.split(" ");
        int start = 0;
        int end = 0;
        char letter;
        final StringBuffer buffer = new StringBuffer();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start + 2);
            String charStr = "";
            if (end == -1) {
                charStr = dataStr.substring(start + 2, start + 6);
                letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
                buffer.append(new Character(letter).toString());
                buffer.append(dataStr.substring(start + 6, dataStr.length()));
            } else {
                charStr = dataStr.substring(start + 2, start + 6);
                letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
                System.out.println(String.valueOf(new Character(letter)));
                buffer.append(String.valueOf(new Character(letter)));
            }

            start = end;
        }
        return buffer.toString();
    }
}
