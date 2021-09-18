import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

public class check_paperTest {

    @Test
    public void testParticiple() throws IOException {
        String content = "我喜欢软工这门课，它让我学到了很多";
        String[] res = check_paper.participle(content);
        for (String word : res) {
            System.out.println(word);
        }
    }
    @Test
    public void testWordFreq() throws IOException {
        String[] content = {"喜欢", "软工", "这门", "课", "让我", "学到了", "很多"};
        Map<Object, Object> res = check_paper.wordFreq(content);
        System.out.println(res);
    }
    @Test
    public void testMain() throws IOException {
        String[] paths = {
                "text\\orig.txt",
                "text\\orig_0.8_add.txt",
                "text\\result.txt"
        };
        check_paper.main(paths);
    }
    @Test
    public void testMain1() throws IOException {
        String[] paths = {
                "text\\orig.txt",
                "text\\orig_0.8_del.txt",
                "text\\result.txt"
        };
        check_paper.main(paths);
    }
    @Test
    public void testMain2() throws IOException {
        String[] paths = {
                "text\\orig.txt",
                "text\\orig_0.8_dis_1.txt",
                "text\\result.txt"
        };
        check_paper.main(paths);
    }
    @Test
    public void testMain3() throws IOException {
        String[] paths = {
                "text\\orig.txt",
                "text\\orig_0.8_dis_10.txt",
                "text\\result.txt"
        };
        check_paper.main(paths);
    }
    @Test
    public void testMain4() throws IOException {
        String[] paths = {
                "text\\orig.txt",
                "text\\orig_0.8_dis_15.txt",
                "text\\result.txt"
        };
        check_paper.main(paths);
    }

}
