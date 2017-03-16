package cn.edu.hfut.dmic.contentextractor;

import org.junit.Assert;
import org.junit.Test;
/**
 * Created by jintaoduan on 2017/3/16.
 */
public class UrlTest {

    @Test
    public void testWeiXin() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://mp.weixin.qq.com/s?__biz=MjM5OTA1MDUyMA==&mid=2655437863&idx=1&sn=67ff6deffc656cc1335b428434fed5ba&chksm=bd7308508a04814685c2a1e67bd4a86e502edc38dc9eda7ebe3d93d8fdf0997dcc7dd45f6047&scene=0#rd");
        Assert.assertEquals("程序员的那些事",news.getAuthor());
        Assert.assertEquals("技术人员的发展之路",news.getTitle());
    }

    @Test
    public void testFang() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://news.fang.com/2017-03-15/24671917.htm");
        Assert.assertEquals("房天下",news.getAuthor());
    }
    @Test
    public void testToutiao() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://www.toutiao.com/a6397697145327862018/");
        Assert.assertEquals("野史趣事",news.getAuthor());
    }
    @Test
    public void testBinZhou() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://bztoutiao.binzhouw.com/detail/pid/96938");
        Assert.assertEquals("张婵",news.getAuthor());
    }
    @Test
    public void testPuyang() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://www.pyfc.cn/show.asp?id=15256");
        Assert.assertEquals("濮阳房产网",news.getAuthor());
    }
    @Test
    public void testJiujiang() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://www.zzjjw.cn/news/show-65591.html");
        Assert.assertEquals("浔阳晚报",news.getAuthor());
    }
    @Test
    public void testXinjiang() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://www.xyfcw.com/news/news_info/695536.html");
        Assert.assertEquals("信阳房产网",news.getAuthor());
    }

    @Test
    public void testHuhetaote() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://www.365hf.com/news/show-66119.html");
        Assert.assertEquals("21世纪经济报道",news.getAuthor());
    }
    @Test
    public void testXining() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://www.xnfcxx.com/show.asp?id=2548");
        Assert.assertEquals("本站",news.getAuthor());
    }
    @Test
    public void testShijiazhuang() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://house.inhe.net/news/2017/0316/105028.shtml");
        Assert.assertEquals("石家庄新闻网",news.getAuthor());
    }
    @Test
    public void testZhenjiang() throws Exception {
        News news = ContentExtractor.getNewsByUrl("http://zj.fccs.com/news/201703/5114112.shtml");
        Assert.assertEquals("新城吾悦广场",news.getAuthor());
    }
}
