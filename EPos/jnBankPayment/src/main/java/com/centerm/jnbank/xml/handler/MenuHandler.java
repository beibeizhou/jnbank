package com.centerm.jnbank.xml.handler;

import com.centerm.jnbank.xml.XmlTag;
import com.centerm.jnbank.xml.menu.Menu;
import com.centerm.jnbank.xml.menu.MenuItem;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Stack;

import static com.centerm.jnbank.xml.XmlAttrs.MenuAttrs;

/**
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class MenuHandler extends BaseHandler {

    private Menu rootMenu;
    //    private MenuItem tempItem;
    //    private Menu tempMenu;
    private Stack<MenuItem> itemStack = new Stack<>();
    private Stack<String> tagStack = new Stack<>();


    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        rootMenu = new Menu();
        rootMenu.setStructure(Menu.ViewStructure.GRID);
        itemStack.push(rootMenu);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (XmlTag.MenuTag.Root.equals(localName)) {
            return;
        }
        tagStack.push(localName);
        String iconResName = attributes.getValue(MenuAttrs.iconResName);
        String textResName = attributes.getValue(MenuAttrs.textResName);
        String viewStructure = attributes.getValue(MenuAttrs.viewStructure);
        String chnTag = attributes.getValue(MenuAttrs.chnTag);
        String process = attributes.getValue(MenuAttrs.process);
        MenuItem item;
        if (viewStructure != null) {
            item = new Menu(iconResName, textResName);
            item.setEntag(localName);
            item.setChnTag(chnTag);
            item.setTransCode(localName);
            ((Menu) item).setStructure("grid".equals(viewStructure) ? Menu.ViewStructure.GRID : Menu.ViewStructure.LIST);
            if (iconResName == null) {
                item.setIconResName("menu_" + localName.toLowerCase());
            }
            if (textResName == null) {
                item.setTextResName("menu_" + localName.toLowerCase());
            }
        } else {
            item = new MenuItem(iconResName, textResName);
            item.setChnTag(chnTag);
            item.setEntag(localName);
            item.setProcessFile(process);
            item.setTransCode(localName);
            if (process == null) {
                item.setProcessFile(localName);
            }
            if (iconResName == null) {
                item.setIconResName("menu_" + localName.toLowerCase());
            }
            if (textResName == null) {
                item.setTextResName("menu_" + localName.toLowerCase());
            }
        }
        itemStack.push(item);
           /* if (XmlTag.MenuTag.MENU.equals(localName)) {
                tempMenu = new Menu(iconResName, textResName);
                tempMenu.setEntag(localName);
                tempMenu.setChnTag(chnTag);
                tempMenu.setStructure("grid".equals(viewStructure) ? Menu.ViewStructure.GRID : Menu.ViewStructure.LIST);
            } else if (XmlTag.MenuTag.MENU_ITEM.equals(localName)) {
                tempItem = new MenuItem(iconResName, textResName);
                tempItem.setChnTag(chnTag);
                tempItem.setEntag(localName);
            }*/
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        sBuilder.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (XmlTag.MenuTag.Root.equals(localName)) {
            return;
        }
        MenuItem item = itemStack.pop();
        if (itemStack.size() > 0) {
            MenuItem temp = itemStack.peek();
            boolean check = temp instanceof Menu ? true : false;
            ((Menu) temp).add(item);
        }


       /* if (XmlTag.MenuTag.MENU_ITEM.equals(localName)) {
            if (tempMenu == null) {
                rootMenu.add(tempItem);
            } else {
                tempMenu.add(tempItem);
            }
            tempItem = null;
        } else if (XmlTag.MenuTag.MENU.equals(localName)) {

        }*/

    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        itemStack.pop();
    }


    public Menu getMenu() {
        return rootMenu;
    }
}
