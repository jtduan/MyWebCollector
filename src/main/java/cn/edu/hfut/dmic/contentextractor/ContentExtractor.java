/*
 * Copyright (C) 2015 hu
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package cn.edu.hfut.dmic.contentextractor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wanghaomiao.xpath.exception.XpathSyntaxErrorException;

/**
 * ContentExtractor could extract content,title,time from news webpage
 * 来源于github:WebCollector
 * https://github.com/CrawlScript/WebCollector
 */
public class ContentExtractor {

    public static final Logger LOG = LoggerFactory.getLogger(ContentExtractor.class);

    public static DecimalFormat TWONUMBERFORMAT = new DecimalFormat("00");

    protected Document doc;

    ContentExtractor(Document doc) {
        this.doc = doc;
    }

    protected HashMap<Element, CountInfo> infoMap = new HashMap<Element, CountInfo>();

    /**
     * 新加：标记查找到的时间在页面中的原始串，进而找到时间所在的Element，根据时间与作者在一起的特性寻找作者
     */
    private String srcTime = "";

    private String author_bak = "";

    class CountInfo {

        int textCount = 0;
        int linkTextCount = 0;
        int tagCount = 0;
        int linkTagCount = 0;
        double density = 0;
        double densitySum = 0;
        double score = 0;
        int pCount = 0;
        ArrayList<Integer> leafList = new ArrayList<Integer>();

    }

    /**
     * 去掉删除<br>标签
     */
    protected void clean() {
        doc.select("script,noscript,style,iframe").remove();
    }

    /**
     * @param node 新加：
     *             1. 移除style和class属性
     *             2. 出现当前位置的索引时，降低该块的density提升正文精度
     *             3. p标签不能单独形成正文
     * @return
     */
    protected CountInfo computeInfo(Node node) {
        if (node instanceof Element) {
            node.removeAttr("style").removeAttr("class");
            Element tag = (Element) node;

            if (tag.text().matches(".{1,20}>.{1,10}>.{1,20}")) {
                CountInfo countInfo = new CountInfo();
                countInfo.density = -200;
                return countInfo;
            }
            CountInfo countInfo = new CountInfo();
            for (Node childNode : tag.childNodes()) {
                CountInfo childCountInfo = computeInfo(childNode);
                countInfo.textCount += childCountInfo.textCount;
                countInfo.linkTextCount += childCountInfo.linkTextCount;
                countInfo.tagCount += childCountInfo.tagCount;
                countInfo.linkTagCount += childCountInfo.linkTagCount;
                countInfo.leafList.addAll(childCountInfo.leafList);
                countInfo.densitySum += childCountInfo.density;
                countInfo.pCount += childCountInfo.pCount;
            }

            countInfo.tagCount++;
            String tagName = tag.tagName();
            if (tagName.equals("a") || tagName.equals("img")) {
                countInfo.linkTextCount = countInfo.textCount;
                countInfo.linkTagCount++;
            } else if (tagName.equals("p")) {
                countInfo.pCount++;
            }

            int pureLen = countInfo.textCount - countInfo.linkTextCount;
            int len = countInfo.tagCount - countInfo.linkTagCount;
            if (pureLen == 0 || len == 0) {
                countInfo.density = 0;
            } else {
                countInfo.density = (pureLen + 0.0) / len;
            }

            infoMap.put(tag, countInfo);

            return countInfo;
        } else if (node instanceof TextNode) {
            TextNode tn = (TextNode) node;
            CountInfo countInfo = new CountInfo();
            String text = tn.text();
            int len = text.length();
            countInfo.textCount = len;
            countInfo.leafList.add(len);
            return countInfo;
        } else {
            return new CountInfo();
        }
    }

    private double computeScore(Element tag) {
        CountInfo countInfo = infoMap.get(tag);
        double var = Math.sqrt(computeVar(countInfo.leafList) + 1);
        double score = Math.log(var) * countInfo.densitySum * Math.log(countInfo.textCount - countInfo.linkTextCount + 1) * Math.log10(countInfo.pCount + 10);
        return score;
    }

    private double computeVar(ArrayList<Integer> data) {
        if (data.size() == 0) {
            return 0;
        }
        if (data.size() == 1) {
            return data.get(0) / 2;
        }
        double sum = 0;
        for (Integer i : data) {
            sum += i;
        }
        double ave = sum / data.size();
        sum = 0;
        for (Integer i : data) {
            sum += (i - ave) * (i - ave);
        }
        sum = sum / data.size();
        return sum;
    }

    private Element getContentElement() throws Exception {
        clean();
        computeInfo(doc.body());
        double maxScore = 0;
        Element content = null;
        for (Map.Entry<Element, CountInfo> entry : infoMap.entrySet()) {
            Element tag = entry.getKey();
            if (tag.tagName().equals("a") || tag.tagName().equals("p") || tag == doc.body()) {
                continue;
            }
            double score = computeScore(tag);
            if (score > maxScore) {
                maxScore = score;
                content = tag;
            }
        }
        if (content == null) {
            throw new Exception("extraction failed");
        }
        return content;
    }

    private News getNews(boolean flag) throws Exception {
        News news = new News();
        Element contentElement;
        try {
            contentElement = getContentElement();
            news.setContentElement(contentElement);
        } catch (Exception ex) {
            LOG.info("news content extraction failed,extraction abort", ex);
            throw new Exception(ex);
        }

        if (doc.baseUri() != null) {
            news.setUrl(doc.baseUri());
        }

        if (flag) {
            clearLi();
        }

        try {
            news.setTime(getTime(contentElement));
        } catch (Exception ex) {
            LOG.info("news title extraction failed", ex);
        }

        try {
            news.setAuthor(getAuthor());
        } catch (Exception ex) {
            LOG.info("news author extraction failed", ex);
        }

        try {
            news.setTitle(getTitle(contentElement));
        } catch (Exception ex) {
            LOG.info("title extraction failed", ex);
        }
        news.setSrcTime(srcTime);
        return news;
    }

    private void clearLi() {
        doc.select("li").remove();
    }

    /**
     * 添加获取作者接口，优先级:
     * 1. 挨着时间字段的作者，来源，编辑
     * 2. 去除阅读量后，在时间字段前面，冒号后面的中文
     * 3. 在时间字段后面的中文
     * 4. 全文中编辑，作者，来源 后面的字符串
     * 5. 挨着时间字段的英文
     *
     * @return
     * @throws XpathSyntaxErrorException
     */
    private String getAuthor() throws XpathSyntaxErrorException {
        String author = "";
        if (StringUtils.isBlank(srcTime)) {
            author = getAuthor(doc.body().html());
            return author;
        }
        Element cur = doc.body().select("*:containsOwn(" + srcTime + ")").first();
        if (cur == null) {
            LOG.warn("解析到错误的srcTime=" + srcTime);
            author = getAuthor(doc.body().html());
            return author;
        }

        if (!noText(cur)) {
            String arr[] = cur.html().split(srcTime);
            for (String text : arr) {
                author = getShortText(text);
                if (!StringUtils.isBlank(author)) return author;
            }
        }
        Element parent = cur.parent();
        while (parent != null && noText(parent)) {
            cur = parent;
            parent = parent.parent();
        }
        author = getAuthor(parent.html());
        if (!StringUtils.isBlank(author)) return author;

        Element pre = cur.previousElementSibling();
        while (pre != null && noText(pre)) {
            pre = pre.previousElementSibling();
        }
        if (pre != null) {
            author = getShortText(pre.text());
        }
        if (!StringUtils.isBlank(author)) return author;
        Element next = cur.nextElementSibling();
        while (next != null && noText(next)) {
            next = next.nextElementSibling();
        }
        if (next != null) {
            author = getShortText(next.text());
        }
        if (!StringUtils.isBlank(author)) return author;

        author = getShortText(parent.html().replace(srcTime, " "));
        if (!StringUtils.isBlank(author)) return author;

        author = getAuthor(doc.body().html());
        if (StringUtils.isBlank(author)) {
            return author_bak;
        }
        return author;
    }

    private String getAuthor(String str) {
        str = str.replaceAll("</?.*?>", " ").replace("&nbsp;", " ");
        String reg = "(来源|作者|编辑|稿源|出处)[:：  /]{1,3}(.{1,10}?)\\b";
        Pattern authorPattern = Pattern.compile(reg);
        Matcher matcher = authorPattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return "";
    }

    private String getShortText(String str) {
        String author = getAuthor(str);
        if (!StringUtils.isBlank(author)) return author;

        str = str.replaceAll("</?.*?>", " ").replace("&nbsp;", " ").replaceAll("阅读[:：  /]{0,3}?(.{2,6}?)\\b", " ");
        while (str.contains(":")) {
            str = str.substring(str.indexOf(":") + 1, str.length());
        }
        while (str.contains("：")) {
            str = str.substring(str.indexOf("：") + 1, str.length());
        }
        String reg = "[\\u4e00-\\u9fa5a-zA-Z]{1,15}";
        Pattern authorPattern = Pattern.compile(reg);
        Matcher matcher = authorPattern.matcher(str);
        while (matcher.find()) {
            author = matcher.group(0);
            if (!hasChinese(author)) {
                author_bak = author;
                continue;
            }
            if (!(author.contains("分享") || author.contains("手机"))) {
                return author;
            }
        }
        return "";
    }

    private boolean hasChinese(String author) {
        return author.matches(".*[\\u4e00-\\u9fa5].*");
    }

    private boolean noText(Element ele) {
        return noText(ele.text());
    }

    private boolean noText(String str) {
        return str.replace(srcTime, "").matches("[^\\u4e00-\\u9fa5a-zA-Z]*");
    }

    /**
     * 多次通过匹配寻找时间
     * 先去除<li>标签寻找其他标签中的时间，若找不到再在全文寻找时间
     *
     * @param contentElement
     * @return
     * @throws Exception
     */
    protected String getTime(Element contentElement) throws Exception {
        String regex = "\\b([1-2][0-9]{3})[^0-9]{1,5}?([0-1]?[0-9])[^0-9]{1,5}?([0-3]?[0-9])[^0-9]{1,6}?([0-5]?[0-9])[:：]([0-5]?[0-9])[:：]([0-5]?[0-9])\\b";
        String time = getTime(contentElement, regex);
        if (!StringUtils.isBlank(time)) {
            return time;
        }
        regex = "\\b([1-2][0-9]{3})[^0-9]{1,5}?([0-1]?[0-9])[^0-9]{1,5}?([0-3]?[0-9])[^0-9]{1,6}?([0-5]?[0-9])[:：]([0-5]?[0-9])\\b";
        time = getTime(contentElement, regex);
        if (!StringUtils.isBlank(time)) {
            return time;
        }
        regex = "\\b([1-2][0-9]{3})[^0-9]{1,5}?([0-1]?[0-9])[^0-9]{1,5}?([0-3]?[0-9])[^0-9]{0,6}?\\b";
        time = getTime(contentElement, regex);
        if (!StringUtils.isBlank(time)) {
            return time;
        }
        return "";
    }

    protected String getTime(Element contentElement, String regex) throws Exception {
        Pattern pattern = Pattern.compile(regex);
        Element current = contentElement;
        for (int i = 0; i < 2; i++) {
            if (current != null && current != doc.body()) {
                Element parent = current.parent();
                if (parent != null) {
                    current = parent;
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            if (current == null) {
                break;
            }
            String currentHtml = current.outerHtml();
            Matcher matcher = pattern.matcher(currentHtml);
            if (matcher.find()) {
                srcTime = matcher.group(0);
                StringBuilder sb = new StringBuilder(matcher.group(1) + "-" + format(matcher.group(2)) + "-" + format(matcher.group(3)));
                if (matcher.groupCount() >= 4) {
                    sb.append(" ").append(format(matcher.group(4)));
                }
                if (matcher.groupCount() >= 5) {
                    sb.append(":").append(format(matcher.group(5)));
                }
                if (matcher.groupCount() >= 6) {
                    sb.append(":").append(format(matcher.group(6)));
                }
                return sb.toString();
            }
            if (current != doc.body()) {
                current = current.parent();
            }
        }
        return "";
    }

    private String format(String str) {
        return TWONUMBERFORMAT.format(Integer.parseInt(str));
    }

    protected double strSim(String a, String b) {
        int len1 = a.length();
        int len2 = b.length();
        if (len1 == 0 || len2 == 0) {
            return 0;
        }
        double ratio;
        if (len1 > len2) {
            ratio = (len1 + 0.0) / len2;
        } else {
            ratio = (len2 + 0.0) / len1;
        }
        if (ratio >= 3) {
            return 0;
        }
        return (lcs(a, b) + 0.0) / Math.max(len1, len2);
    }

    /**
     * 新加：文中与metaTitle匹配度过低时优先使用metaTitle,若metaTitle不符合条件再根据排序规则选择title
     *
     * @param contentElement
     * @return
     * @throws Exception
     */
    protected String getTitle(final Element contentElement) throws Exception {
        final ArrayList<Element> titleList = new ArrayList<Element>();
        final ArrayList<Double> titleSim = new ArrayList<Double>();
        final String metaTitle = getText(doc.title().trim());
        if (!metaTitle.isEmpty()) {
            doc.body().traverse(new NodeVisitor() {
                @Override
                public void head(Node node, int i) {
                    if (node instanceof Element) {
                        Element tag = (Element) node;
                        String tagName = tag.tagName();
                        if (Pattern.matches("h[1-6]", tagName)) {
                            String title = tag.text().trim();
                            double sim = strSim(title, metaTitle);
                            titleSim.add(sim);
                            titleList.add(tag);
                        }
                    }
                }

                @Override
                public void tail(Node node, int i) {
                }
            });
            int index = titleSim.size();
            if (index >= 0) {
                double maxScore = 0;
                int maxIndex = -1;
                for (int i = 0; i < index; i++) {
                    double score = (i + 1) * titleSim.get(i);
                    if (score > maxScore) {
                        maxScore = score;
                        maxIndex = i;
                    }
                }

                if (maxIndex == -1 || titleSim.get(maxIndex) < 0.3) {
                    String title = getText(metaTitle);
                    if (!title.endsWith("网") && title.length() > 7) {
                        return title;
                    }
                    Collections.sort(titleList, new Comparator<Element>() {
                        @Override
                        public int compare(Element o1, Element o2) {
                            int len1 = 1;
                            int len2 = 1;
                            if (o1.text().replaceAll("[^\\u4e00-\\u9fa5]", "").length() > 26 || o1.text().replaceAll("[^\\u4e00-\\u9fa5]", "").length() < 7) {
                                len1 = 0;
                            }
                            if (o2.text().replaceAll("[^\\u4e00-\\u9fa5]", "").length() > 26 || o2.text().replaceAll("[^\\u4e00-\\u9fa5]", "").length() < 7) {
                                len2 = 0;
                            }
                            if (len1 == len2) {
                                return o1.tagName().charAt(1) - o2.tagName().charAt(1);
                            }
                            return len2 - len1;
                        }
                    });
                    return getText(titleList.get(0).text());
                }
                return titleList.get(maxIndex).text();
            }
        }

        /**
         * 几乎不能能走到这
         */
        Elements titles = doc.body().select("*[id^=title],*[id$=title],*[class^=title],*[class$=title]");
        if (titles.size() > 0) {
            String title = titles.first().text();
            if (title.length() > 5 && title.length() < 40) {
                return titles.first().text();
            }
        }
        try {
            return getTitleByEditDistance(contentElement);
        } catch (Exception ex) {
            throw new Exception("title not found");
        }

    }

    private String getText(String metaTitle) {
        return metaTitle.replaceAll("[-/_–|]{1,3}.*", "");
    }

    protected String getTitleByEditDistance(Element contentElement) throws Exception {
        final String metaTitle = doc.title();

        final ArrayList<Double> max = new ArrayList<Double>();
        max.add(0.0);
        final StringBuilder sb = new StringBuilder();
        doc.body().traverse(new NodeVisitor() {

            public void head(Node node, int i) {

                if (node instanceof TextNode) {
                    TextNode tn = (TextNode) node;
                    String text = tn.text().trim();
                    double sim = strSim(text, metaTitle);
                    if (sim > 0) {
                        if (sim > max.get(0)) {
                            max.set(0, sim);
                            sb.setLength(0);
                            sb.append(text);
                        }
                    }

                }
            }

            public void tail(Node node, int i) {
            }
        });
        if (sb.length() > 0) {
            return sb.toString();
        }
        throw new Exception();

    }

    protected int lcs(String x, String y) {

        int M = x.length();
        int N = y.length();
        if (M == 0 || N == 0) {
            return 0;
        }
        int[][] opt = new int[M + 1][N + 1];

        for (int i = M - 1; i >= 0; i--) {
            for (int j = N - 1; j >= 0; j--) {
                if (x.charAt(i) == y.charAt(j)) {
                    opt[i][j] = opt[i + 1][j + 1] + 1;
                } else {
                    opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
                }
            }
        }

        return opt[0][0];

    }

    protected int editDistance(String word1, String word2) {
        int len1 = word1.length();
        int len2 = word2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 0; i < len1; i++) {
            char c1 = word1.charAt(i);
            for (int j = 0; j < len2; j++) {
                char c2 = word2.charAt(j);

                if (c1 == c2) {
                    dp[i + 1][j + 1] = dp[i][j];
                } else {
                    int replace = dp[i][j] + 1;
                    int insert = dp[i][j + 1] + 1;
                    int delete = dp[i + 1][j] + 1;

                    int min = replace > insert ? insert : replace;
                    min = delete > min ? min : delete;
                    dp[i + 1][j + 1] = min;
                }
            }
        }

        return dp[len1][len2];
    }

    /*输入Jsoup的Document，获取结构化新闻信息*/
    private static News getNewsByDoc(Document doc, boolean flag) throws Exception {
        ContentExtractor ce = new ContentExtractor(doc);
        return ce.getNews(flag);
    }

    /*输入HTML，获取结构化新闻信息*/
    private static News getNewsByHtml(String html) throws Exception {
        html = html.replaceAll("\\<!--.*?--\\>", "").replace(" ", " ");
        Document doc = Jsoup.parse(html);
        News news = getNewsByDoc(doc, true);
        if (StringUtils.isBlank(news.getTime())) {
            doc = Jsoup.parse(html);
            news = getNewsByDoc(doc, false);
        }
        return news;
    }

    /*输入HTML和URL，获取结构化新闻信息*/
    private static News getNewsByHtml(String html, String url) throws Exception {
        if (StringUtils.isBlank(url)) {
            return getNewsByHtml(html);
        }
        html = html.replaceAll("\\<!--.*?--\\>", "").replace(" ", " ");
        Document doc = Jsoup.parse(html, url);
        News news = getNewsByDoc(doc, true);
        if (StringUtils.isBlank(news.getTime())) {
            doc = Jsoup.parse(html, url);
            news = getNewsByDoc(doc, false);
        }
        return news;
    }

    /*输入URL，获取结构化新闻信息*/
    public static News getNewsByUrl(String url) throws Exception {
        HttpRequest request = new HttpRequest(url);
        String html = request.response().decode();
        return getNewsByHtml(html, url);
    }

    public static void main(String[] args) {
        System.out.println("2017年03月17日".matches("\\b([1-2][0-9]{3})[^0-9]{1,5}?([0-1]?[0-9])[^0-9]{1,5}?([0-3]?[0-9])[^0-9]{1,5}?\\b"));
    }
}
