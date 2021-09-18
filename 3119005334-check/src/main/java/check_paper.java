import com.huaban.analysis.jieba.JiebaSegmenter;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

public class check_paper {
    static String[] paths = {};

    static String truePath = "";
    static String choicePath = ""; // 选择查重的论文路径
    static String resultPath = ""; // 答案文件路径



    // 文本名
    static final String originName = "orig.txt";
    static String falseTxtName = "";

    // 余弦相似度
    private static final LinkedHashMap<String, Float[]> wordVector = new LinkedHashMap<>();

    // 两个文本的词频
    private static final HashMap<String, HashMap<String, Float>> allFrequency = new HashMap<>();
    private static final HashMap<String, HashMap<String, Integer>> allNormalFrequency = new HashMap<>();

    // 操作指示
    public static void indicator(String[] paths) {
        if (paths != null && paths.length != 0) {
            truePath = paths[0];
            choicePath = paths[1];
            resultPath = paths[2];
        } else {
            Scanner scanner = new Scanner(System.in);

            System.out.println("请输入论文原文的绝对路径：");
            truePath = scanner.nextLine();

            System.out.println("请输入抄袭版论文的绝对路径：");
            choicePath = scanner.nextLine();

            System.out.println("请输入抄答案文件的绝对路径：");
            resultPath = scanner.nextLine();

        }
    }

    // 读取 txt 文本
    public static String readTxt(String file) throws IOException {
        StringBuilder sb = new StringBuilder();

        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(is);
            String line = br.readLine(); // 读取文本行
            while(line != null) {
                // 去除符号，并添加到字符流中
                sb.append(line.replaceAll("[\\pP\\pS\\pZ]", ""));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.out.println("请检查输入的路径是否正确！");
            System.out.println(e);
            indicator(paths);
        }
        return sb.toString();
    }

    // 分词
    public static String[] participle(String txt) throws IOException {
        String[] res;
        JiebaSegmenter Segment = new JiebaSegmenter();
        String temp = Segment.sentenceProcess(txt).toString();

        // 返回的分词 [...] 中括号和逗号会被当前字符的一部分，因此需要去除
        res = temp.replaceAll("[\\[\\]\\,]","").split(" ");
        return res;
    }

    // 计算词频
    public static Map<Object, Object> wordFreq(String[] res) {
        int repeat;
        int resLen = res.length;

        HashMap<String, Float> frequency = new HashMap<>(); // 没有正规化
        HashMap<String, Integer> normalFrequency = new HashMap<>(); // 没有正规化
        Map<Object, Object> map = new HashMap<>();

        for(int i = 0; i < resLen; i++) {
            repeat = 0; // 某个词的重复次数

            if(!res[i].equals("")) {
                repeat++;
                for(int j = i + 1; j < resLen; j++) {
                    if(!res[j].equals("") && res[i].equals(res[j])) {
                        res[j] = "";
                        repeat++;
                    }
                }

                // 某一单词遍历结束，计算词频
                frequency.put(res[i], ((float) repeat) / resLen);
                normalFrequency.put(res[i], repeat);

                map.put("frequency", frequency);
                map.put("normalFrequency", normalFrequency);

                res[i] = "";
            }
        }
        return map;
    }

    // 获取两篇论文词频
    public static void AllWordFreq(String originName, String falseTxtName) throws IOException {
        // 词典，存放各个文本的关键字和词频
        HashMap<String, Float> dict;
        HashMap<String, Float> falseDict;

        dict = (HashMap<String, Float>) wordFreq(participle(readTxt(truePath))).get("frequency");
        falseDict = (HashMap<String, Float>) wordFreq(participle(readTxt(choicePath))).get("frequency");

        allFrequency.put(originName, dict);
        allFrequency.put(falseTxtName, falseDict);

    }
    public static void AllNormalWordFreq(String originName, String falseTxtName) throws IOException {
        // 词典，存放各个文本的关键字和词频
        HashMap<String, Integer> dict;
        HashMap<String, Integer> falseDict;

        dict = (HashMap<String, Integer>) wordFreq(participle(readTxt(truePath))).get("normalFrequency");
        falseDict = (HashMap<String, Integer>) wordFreq(participle(readTxt(choicePath))).get("normalFrequency");

        allNormalFrequency.put(originName, dict);
        allNormalFrequency.put(falseTxtName, falseDict);
    }

    // 计算逆文档频率 idf
    public static Map<String, Float> idf(String originName, String falseTxtName) throws IOException {
        AllNormalWordFreq(originName, falseTxtName); // 获取非正规化词频

        int Dt = 1; // 包含关键词t的文本数量
        int D = 2; // 文本数

        List<String> key = new ArrayList<>(); // 文本列表
        key.add(originName);
        key.add(falseTxtName);

        Map<String, Float> idf = new HashMap<>();
        List<String> totalWord = new ArrayList<>(); // 存储两个文本的关键词
        Map<String, HashMap<String, Integer>> totalFreq = allNormalFrequency; // 存储各个文本的 tf

        for(int i = 0; i < D; i++) {
            HashMap<String, Integer> temp = totalFreq.get(key.get(i));

            for (String word : temp.keySet()) {
                if (!totalWord.contains(word)) {
                    for (int k = 0; k < D; k++) {
                        if (k != i) {
                            HashMap<String, Integer> temp2 = totalFreq.get(key.get(k));
                            if (temp2.containsKey(word)) {
                                totalWord.add(word);
                                Dt = Dt + 1;
                            }
                        }
                    }
                    idf.put(word, (float) Math.log(1 + D) / Dt);
                }
            }
        }

        return idf;
    }

    // 计算TF-IDF 词频 * 逆文档频率，并获取词向量
    public static void tfidf(String originName, String falseTxtName) throws IOException {
        Map<String, Float> idf = idf(originName, falseTxtName);
        AllWordFreq(originName, falseTxtName);

        for (String freq : allFrequency.keySet()) {
            int index = 0;
            int idfLen = idf.size();
            Float[] arr = new Float[idfLen];
            Map<String, Float> temp = allFrequency.get(freq);

            for (String word : temp.keySet()) {
                temp.put(word, idf.get(word) * temp.get(word));
            }

            for (String word : idf.keySet()) {
                arr[index] = temp.get(word) != null ? temp.get(word) : 0f;
                index++;
            }
            wordVector.put(freq, arr);
        }
    }

    // 使用余弦相似度匹配
    public static String cosSim(String originName, String falseTxtName) throws IOException {
        tfidf(originName, falseTxtName);

        Float[] originArr = wordVector.get(originName);
        Float[] falseArr = wordVector.get(falseTxtName);
        int length = originArr.length;

        float originModulus = 0.00f; // 向量1的模
        float falseModulus = 0.00f; // 向量2的模
        float totalModulus = 0f;

        for (int i = 0; i < length; i++) {
            originModulus += originArr[i] * originArr[i];
            falseModulus += falseArr[i] * falseArr[i];
            totalModulus += originArr[i] * falseArr[i];
        }
        float result = (float)Math.sqrt(originModulus * falseModulus);

        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(totalModulus / result);
    }

    public static void main(String[] args) throws IOException {
        paths = args;

        // 选择查重的论文路径
        indicator(paths);
        String result = cosSim(originName, falseTxtName);

        File file = new File(resultPath);
        if(file.exists()) { // 判断文件是否存在
            System.out.println("查重结果请查看 result.txt 文件");
        } else {
            System.out.println("文件不存在，已在论文同目录下创建 result.txt 文件");
            file.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter output = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        output.write("论文原文的绝对路径：" + truePath + "\n");
        output.write("抄袭版论文的绝对路径：" + choicePath + "\n");
        output.write("论文的查重率为：" + result);

        output.close();
        fos.close();
    }
}
