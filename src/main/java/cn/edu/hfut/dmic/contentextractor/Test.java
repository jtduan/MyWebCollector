package cn.edu.hfut.dmic.contentextractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jintaoduan on 2017/3/15.
 */
public class Test {
    public static void main(String[] args) throws Exception {
//        News news = ContentExtractor.getNewsByUrl("http://news.fang.com/2017-03-15/24671917.htm");
//        News news = ContentExtractor.getNewsByUrl("http://mp.weixin.qq.com/s?__biz=MjM5OTA1MDUyMA==&mid=2655437863&idx=1&sn=67ff6deffc656cc1335b428434fed5ba&chksm=bd7308508a04814685c2a1e67bd4a86e502edc38dc9eda7ebe3d93d8fdf0997dcc7dd45f6047&scene=0#rd");
//        News news = ContentExtractor.getNewsByUrl("http://www.toutiao.com/a6397607841340031234/");
//        News news = ContentExtractor.getNewsByUrl("http://news.cnnb.com.cn/system/2017/03/15/008611885.shtml");
//        News news = ContentExtractor.getNewsByUrl("http://bztoutiao.binzhouw.com/detail/pid/96938");
//        News news = ContentExtractor.getNewsByUrl("http://www.pyfc.cn/show.asp?id=15256");
//        News news = ContentExtractor.getNewsByUrl("http://www.365hf.com/news/show-66119.html");
//        News news = ContentExtractor.getNewsByUrl("http://www.xnfcxx.com/show.asp?id=2548");
//        News news = ContentExtractor.getNewsByUrl("http://house.inhe.net/news/2017/0316/105028.shtml");
        News news = ContentExtractor.getNewsByUrl("http://bztoutiao.binzhouw.com/detail/pid/96938");


//        News news = ContentExtractor.getNewsByUrl("http://news.hf365.com/system/2017/03/16/015202754.shtml");

        System.out.println(news.getTitle());
        System.out.println("--------");
        System.out.println(news.getTime());
        System.out.println("--------");
        System.out.println(news.getAuthor());
        System.out.println("--------");
        System.out.println(news.getClearContent());

//        String str = "【 摘要 】 isis";
//        Pattern pattern = Pattern.compile("^\\W{1,3}摘要\\W{1,3}");
//        Matcher matcher = pattern.matcher(str);
//        if (matcher.find()) {
//            System.out.println("true");
//        }
    }
}
