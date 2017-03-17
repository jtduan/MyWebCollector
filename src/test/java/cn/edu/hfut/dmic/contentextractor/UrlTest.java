package cn.edu.hfut.dmic.contentextractor;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
/**
 * Created by jintaoduan on 2017/3/16.
 */
public class UrlTest {

    @Test
    public void testAll() throws Exception {
        test("http://news.fang.com/2017-03-15/24671917.htm","房天下","上周楼市整体成交趋稳 城市成交出现两极分化","2017-03-15 14:36",2507);
        test("http://www.0831home.com/archive.php?aid=50593","三江房产网","快来认宜宾临港这几条新路！要通大学城和滨江大道！","2017-03-13 15:50",5517);
    }

    public void test(String url, String author,String title,String time,int words) throws Exception {
        News news = ContentExtractor.getNewsByUrl(url);
        Assert.assertEquals(author,news.getAuthor());
        Assert.assertEquals(title,news.getTitle());
        Assert.assertEquals(time,news.getTime());
        Assert.assertEquals(words,news.getContent().length());
    }


    @Test
    public void testWeiXin() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://mp.weixin.qq.com/s?__biz=MjM5OTA1MDUyMA==&mid=2655437863&idx=1&sn=67ff6deffc656cc1335b428434fed5ba&chksm=bd7308508a04814685c2a1e67bd4a86e502edc38dc9eda7ebe3d93d8fdf0997dcc7dd45f6047&scene=0#rd");
        Assert.assertEquals("程序员的那些事",news.getAuthor());
        Assert.assertEquals("技术人员的发展之路",news.getTitle());
        Assert.assertEquals("2017-02-18",news.getTime());
        Assert.assertEquals(10872,news.getContent().length());
    }

    @Test
    public void testToutiao() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://www.toutiao.com/a6397697145327862018/");
        Assert.assertEquals("野史趣事",news.getAuthor());
        Assert.assertEquals("你知道钱学森归国后享受的待遇如何吗？真相超乎你想象",news.getTitle());
        Assert.assertEquals("2017-03-15 20:28",news.getTime());
        Assert.assertEquals(1539,news.getContent().length());
    }
    @Test
    public void testBinZhou() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://bztoutiao.binzhouw.com/detail/pid/96938");
        Assert.assertEquals("张婵",news.getAuthor());
        Assert.assertEquals("阳信将举办水韵梨乡（国际）音乐节",news.getTitle());
        Assert.assertEquals("2017-03-16",news.getTime());
        Assert.assertEquals(669,news.getContent().length());
    }
    @Test
    public void testPuyang() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://www.pyfc.cn/show.asp?id=15256");
        Assert.assertEquals("濮阳房产网",news.getAuthor());
        Assert.assertEquals("楼市三大新现象：超级繁荣、房贷收紧、三四线火爆",news.getTitle());
        Assert.assertEquals("2017-03-14",news.getTime());
        Assert.assertEquals(3805,news.getContent().length());
    }
    @Test
    @Ignore
    public void testJiujiang() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://www.zzjjw.cn/news/show-65591.html");
        Assert.assertEquals("浔阳晚报",news.getAuthor());
        Assert.assertEquals("技术人员的发展之路",news.getTitle());
        Assert.assertEquals("2017-03-16",news.getTime());
        Assert.assertEquals(3337,news.getContent().length());
    }
    @Test
    public void testXinjiang() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://www.xyfcw.com/news/news_info/695536.html");
        Assert.assertEquals("信阳房产网",news.getAuthor());
        Assert.assertEquals("【香江帝景】好房子不用“装” 定制精装全新上线",news.getTitle());
        Assert.assertEquals("2017-03-14",news.getTime());
        Assert.assertEquals(1517,news.getContent().length());
    }

    @Test
    public void testHuhetaote() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://www.365hf.com/news/show-66119.html");
        Assert.assertEquals("21世纪经济报道",news.getAuthor());
        Assert.assertEquals("中国跨境地产投资放缓 规模同比缩小近20倍",news.getTitle());
        Assert.assertEquals("2017-03-15 09:30:16",news.getTime());
        Assert.assertEquals(3416,news.getContent().length());
    }
    @Test
    public void testShijiazhuang() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://house.inhe.net/news/2017/0316/105028.shtml");
        Assert.assertEquals("石家庄新闻网",news.getAuthor());
        Assert.assertEquals("如何避免购房陷阱 让你轻松买房不中招",news.getTitle());
        Assert.assertEquals("2017-03-16 06:59",news.getTime());
        Assert.assertEquals(730,news.getContent().length());
    }
    @Test
    public void testZhenjiang() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://zj.fccs.com/news/201703/5114112.shtml");
        Assert.assertEquals("新城吾悦广场",news.getAuthor());
        Assert.assertEquals("宁镇扬一体化真的来了：长三角机遇看镇江 镇江投资看吾悦",news.getTitle());
        Assert.assertEquals("2017-03-16 10:20",news.getTime());
        Assert.assertEquals(3053,news.getContent().length());
    }
}
