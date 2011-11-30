package javaxt.rss;
import org.w3c.dom.*;
import javaxt.xml.DOM;
//import javaxt.geospatial.geometry.Geometry;
//import javaxt.geospatial.coordinate.Parser;

//******************************************************************************
//**  RSS Item
//******************************************************************************
/**
 *   Used to represent an entry in an RSS feed.
 *
 ******************************************************************************/

public class Item {
    
    
    private String title = "";
    private String description = "";
    private String author = null;
    private String creator = null;
    private java.net.URL link = null;
    private java.net.URL origLink = null; //<--FeedBurner
    private Object geometry = null;
    private NodeList nodeList = null;

    private String pubDate = null;
    private String dcDate = null;
    
    private java.util.List<Media> media = new java.util.ArrayList<Media>();


    /** Creates a new instance of Item */
    protected Item(org.w3c.dom.Node node) {
        nodeList = node.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++){
             node = nodeList.item(i);
             String nodeName = node.getNodeName().toLowerCase();
             String nodeValue = DOM.getNodeValue(node).trim();
             if (nodeName.equals("title")) title = nodeValue;

           //Parse Description
             if (nodeName.equals("description") || nodeName.equals("subtitle")){
                 if (description==null || description.length()==0){
                     description = nodeValue;
                 }
             }

           //Parse Link
             if (nodeName.equals("link")){
                 String url = nodeValue;
                 if (url.length()==0){
                     //get href attribute
                     url = DOM.getAttributeValue(node,"href").trim();
                 }
                 if (url.length()>0){
                     try{ link = new java.net.URL(url); }
                     catch(Exception e){}
                 }
             }

           //Parse FeedBurner Link
             if (nodeName.equals("feedburner:origLink")){
                 String url = nodeValue.trim();
                 if (url.length()>0){
                     try{ origLink = new java.net.URL(url); }
                     catch(Exception e){}
                 }
             }

             if (nodeName.equals("author")) author = nodeValue;
             if (nodeName.endsWith("creator")) creator = nodeValue;
             if (nodeName.equalsIgnoreCase("pubDate")) pubDate = nodeValue;
             if (nodeName.equalsIgnoreCase("dc:date")) dcDate = nodeValue;


           //Parse Location Information (GeoRSS)
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


             if (nodeName.startsWith("media:")){
                 media.add(new Media(node));
             }
        }
    }


    
    
    public String getTitle(){ return title; }
    public String getDescription(){ return description; }

    public String getAuthor(){
        if (author==null && creator!=null) return creator;
        return author;
    }


  //**************************************************************************
  //** getLink
  //**************************************************************************
  /**  Returns a link/url associated with the current entry. Returns the
   *   'feedburner:origLink' if found. Otherwise returns a url associated with
   *   the 'link' node.
   */
    public java.net.URL getLink(){
        if (origLink!=null) return origLink;
        else return link;
    }


  //**************************************************************************
  //** getDate
  //**************************************************************************
  /**  Return the date/time stamp associated with the current entry. Uses the
   *   pubDate if it exists. Otherwise, returns dc:date
   */
    public javaxt.utils.Date getDate(){
        String date = pubDate;
        if (date.length()==0) date = dcDate;
        if (date.length()>0){
            try{
                return new javaxt.utils.Date(date);
            }
            catch(java.text.ParseException e){
                return null;
            }
        }
        else return null;
    }


    public Media[] getMedia(){
        Media[] arr = new Media[media.size()];
        for (int i=0; i<media.size(); i++){
            arr[i] = media.get(i);
        }
        return arr;
    }



  //**************************************************************************
  //** getLocation
  //**************************************************************************
  /**  Returns a geometry associated with the current entry.
   */
    public Object getLocation(){ return geometry; }


    
    public NodeList getNodeList(){
        return nodeList;
    }

    
    
    public String toString(){
        StringBuffer out = new StringBuffer();
        String br = "\r\n";
        out.append("Title: " + getTitle() + br);
        //out.append("Description: " + getDescription() + br);
        out.append("Author: " + getAuthor() + br);
        out.append("Link: " + getLink() + br);
        out.append("Date: " + getDate() + br);
        
        if (geometry!=null){
            out.append("Location: " + geometry + br);
            //out.append("Geometry Name: " + geometry.getName() + br);
            //out.append("Geometry SRS: " + geometry.getSRS() + br);
        }

        for (int i=0; i<media.size(); i++){
            System.out.println(media.get(i));
        }
        
        return out.toString();
    }
    
}
