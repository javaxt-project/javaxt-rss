package javaxt.rss;

import org.w3c.dom.*;
import javaxt.xml.DOM;
//import javaxt.geospatial.geometry.Geometry;
//import javaxt.geospatial.coordinate.Parser;

//******************************************************************************
//**  RSS Feed
//******************************************************************************
/**
 *   Used to represent an RSS feed/channel. Returns a list of entries and other
 *   attributes associated with the feed.
 *
 ******************************************************************************/

public class Feed {
    
    private String title = "";
    private String description = "";
    private java.net.URL link = null;
    private Object geometry = null;

    private javaxt.utils.Date lastUpdate = null;
    private Integer interval = null;
    
    private Item[] Items = null;

    
  //**************************************************************************
  //** Instantiate Feed
  //**************************************************************************
  /** Creates a new instance of Feed */
    
    protected Feed(org.w3c.dom.Node node) {
        java.util.Vector vec = new java.util.Vector();
        NodeList nodeList = node.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++){
             node = nodeList.item(i);
             String nodeName = node.getNodeName().toLowerCase();
             String nodeValue = DOM.getNodeValue(node);
             //System.out.println(nodeName + ": " + nodeValue);
             if (nodeName.equals("title")) title = nodeValue;
             if (nodeName.equals("description") || nodeName.equals("subtitle")){
                 description = nodeValue;
             }
             if (nodeName.equals("where") || nodeName.equals("georss:where") || 
                 nodeName.equals("point") || nodeName.equals("georss:point") ||
                 nodeName.equals("line") || nodeName.equals("georss:line") ||
                 nodeName.equals("polygon") || nodeName.equals("georss:polygon") ||
                 nodeName.equals("box") || nodeName.equals("georss:box")){
                 try{
                     //geometry = new Parser(nodeValue).getGeometry();

                    Class classToLoad = Class.forName("javaxt.geospatial.coordinate.Parser");//, true, child);
                    java.lang.reflect.Constructor constructor = classToLoad.getDeclaredConstructor(new Class[] {String.class});
                    java.lang.reflect.Method method = classToLoad.getDeclaredMethod ("getGeometry");
                    Object instance = constructor.newInstance();
                    geometry = method.invoke(instance);

                 }
                 catch(Exception e){}
             }


             if (nodeName.equals("link")){
                 String url = nodeValue.trim();
                 if (url.length()==0){
                     //get href attribute
                 }
                 try{
                     link = new java.net.URL(url);
                 }
                 catch(Exception e){}
             }
             


             if (nodeName.equals("item") || nodeName.equals("entry")){
                 vec.add(new Item(node));
             }

             if (nodeName.equalsIgnoreCase("lastBuildDate")){
                 if (nodeValue!=null){
                     try{
                         lastUpdate = new javaxt.utils.Date(nodeValue);
                     }
                     catch(java.text.ParseException e){
                         lastUpdate = null;
                     }
                 }
             }

             if (nodeName.equals("ttl")){
                 try{
                     interval = javaxt.utils.string.toInt(nodeValue);
                 }
                 catch(Exception e){
                 }
             }
             
        }
        
        
      //Convert Vector to Array
        Object[] arr = vec.toArray();
        Items = new Item[arr.length];
        for (int i=0; i<Items.length; i++){
             Items[i] = (Item) arr[i];
        }
        
    }
    
    public String getTitle(){ return title; }
    public String getDescription(){ return description; }
    public java.net.URL getLink(){ return link; }
    public Item[] getItems(){ return Items; }
    public Object getLocation(){ return geometry; }

    public javaxt.utils.Date getLastUpdate(){
        return lastUpdate;
    }
    
    
  //**************************************************************************
  //** getRefreshInterval
  //**************************************************************************
  /** Returns the number of minutes that the channel can be cached before
   *  refreshing from the source. Derived from the ttl tag in RSS feeds.
   *  Returns null if the refresh interval is not specified or unknown.
   */
    public Integer getRefreshInterval(){
        return interval;
    }
    
    public String toString(){
        StringBuffer out = new StringBuffer();
        String br = "\r\n";
        out.append("Title: " + getTitle() + br);
        out.append("Description: " + getDescription() + br);
        out.append("Last Update: " + getLastUpdate() + br);
        out.append("Link: " + getLink() + br);
        if (geometry!=null){
            out.append("Location: " + geometry + br);
        }
        return out.toString();
    }

}