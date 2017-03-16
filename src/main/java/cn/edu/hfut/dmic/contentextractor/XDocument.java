package cn.edu.hfut.dmic.contentextractor;

import cn.wanghaomiao.xpath.exception.NoSuchAxisException;
import cn.wanghaomiao.xpath.exception.NoSuchFunctionException;
import cn.wanghaomiao.xpath.exception.XpathSyntaxErrorException;
import cn.wanghaomiao.xpath.model.JXDocument;
import cn.wanghaomiao.xpath.model.JXNode;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class XDocument extends JXDocument {
    public XDocument(Document doc) {
        super(doc);
    }

    public XDocument(String html) {
        super(html);
    }

    public XDocument(Elements els) {
        super(els);
    }

    /**
     *
     * @param xpath
     * @return 返回匹配到的第一个元素。如果没有则返回空JXNode对象。
     * @throws XpathSyntaxErrorException
     */
    public JXNode selNFirst(String xpath) throws XpathSyntaxErrorException {
        try {
            List<JXNode> list=selN(xpath);
            if(list.size()==0) {
                JXNode temp=new JXNode();
                temp.setTextVal("");
                return temp;
            }else {
                return selN(xpath).get(0);
            }
        }catch (Exception e){
            String msg = "please check the xpath syntax";
            if (e instanceof NoSuchAxisException ||e instanceof NoSuchFunctionException){
                msg = e.getMessage();
            }
            throw new XpathSyntaxErrorException(msg);
        }
    }

    /**
     *
     * @param xpath
     * @return 返回匹配到的第一个元素内容。如果是文本则返回文本，否则返回null
     * @throws XpathSyntaxErrorException
     */
    public String selFirst(String xpath) throws XpathSyntaxErrorException {
        try {
            List<JXNode> list=selN(xpath);
            if(list.size()==0) {
                Object temp=new Object();
                return "";
            }else if(list.get(0).isText()){
                return list.get(0).getTextVal();
            }else  {
                return null;
            }
        }catch (Exception e){
            String msg = "please check the xpath syntax";
            if (e instanceof NoSuchAxisException ||e instanceof NoSuchFunctionException){
                msg = e.getMessage();
            }
            throw new XpathSyntaxErrorException(msg);
        }
    }

}
