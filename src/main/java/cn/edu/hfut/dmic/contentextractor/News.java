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

import org.jsoup.nodes.Element;

/**
 * @author hu
 */
public class News {

    protected String url = null;
    protected String title = null;
    protected String content = null;
    protected String time = null;
    protected String srcTime = null;
    protected String author = null;

    protected Element contentElement = null;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSrcTime() {
        return srcTime;
    }

    public void setSrcTime(String srcTime) {
        this.srcTime = srcTime;
    }

    @Override
    public String toString() {
        return "URL:\n" + url + "\nTITLE:\n" + title + "\nTIME:\n" + time + "\nAUTHOR:\n" + author + "\nCONTENT:\n" + getContent() + "\nCONTENT(SOURCE):\n" + contentElement;
    }

    public Element getContentElement() {
        return contentElement;
    }

    public void setContentElement(Element contentElement) {
        this.contentElement = contentElement;
        if (contentElement != null) {
            content = contentElement.html();
        }
    }

    public String getClearContent() {
        while (true) {
            Element first = contentElement.children().first();
            if (canRemove(first)) {
                first.remove();
            }
            break;
        }
        while (true) {
            Element last = contentElement.children().last();
            if (canRemove(last)) {
                last.remove();
            }
            break;
        }
        return contentElement.html();
    }

    private boolean canRemove(Element first) {
        if (first.text().isEmpty() && first.getElementsByTag("img") == null) return true;
        if (first.text().equals(title)) return true;
        if (first.text().contains(srcTime)) return true;
        if (first.text().matches("^\\W{0,3}摘要\\W{1,3}.*")) return true;

        if (first.text().length() < 40 && first.text().contains("页")) return true;
        if (first.text().length() < 40 && first.text().contains("分享")) return true;
        if (first.text().matches("^\\W{0,3}免责声明\\W{1,3}.*")) return true;
        return false;
    }
}
