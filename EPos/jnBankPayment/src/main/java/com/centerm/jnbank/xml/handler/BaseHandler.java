package com.centerm.jnbank.xml.handler;


import org.apache.log4j.Logger;
import org.xml.sax.helpers.DefaultHandler;

/**
 * author:wanliang527</br>
 * date:2016/10/18</br>
 */

public class BaseHandler extends DefaultHandler {
    protected Logger logger = Logger.getLogger(this.getClass());
    protected StringBuilder sBuilder = new StringBuilder();
}
