package javaxt.rss;
import org.w3c.dom.*;

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
    private Location location = null;

    private java.util.Date lastUpdate = null;
    private Integer interval = null;
    
    private java.util.ArrayList<Item> items = new java.util.ArrayList<Item>();

    
  //**************************************************************************
  //** Constructor
  //**************************************************************************
    protected Feed(){}

    
  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of this class using an XML node from an RSS XML
   *  document. 
   */ 
    protected Feed(org.w3c.dom.Node node, java.util.HashMap<String, String> namespaces) {
        //java.util.ArrayList<Item> items = new java.util.ArrayList<Item>();
        NodeList nodeList = node.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++){
            node = nodeList.item(i);
            if (node.getNodeType()==1){
                String nodeName = node.getNodeName().toLowerCase();
                String nodeValue = Parser.getNodeValue(node).trim();
                if (nodeName.equals("title")) title = nodeValue;
                else if(nodeName.equals("description") || nodeName.equals("subtitle")){
                    description = nodeValue;
                }


              //Parse Location Information (GeoRSS)
                else if (Location.isLocationNode(nodeName, namespaces)){
                    location = new Location(node, namespaces);
                }
                
                else if (nodeName.equals("link")){
                    String url = nodeValue.trim();
                    if (url.length()==0){
                        //get href attribute?
                    }
                    try{
                        link = new java.net.URL(url);
                    }
                    catch(Exception e){}
                }

                else if (nodeName.equals("item") || nodeName.equals("entry")){
                    items.add(new Item(node, namespaces));
                }
                else if (nodeName.equalsIgnoreCase("lastBuildDate")){ //pubDate?
                    try{
                        lastUpdate = Parser.getDate(nodeValue);
                    }
                    catch(java.text.ParseException e){
                    }
                }
                else if (nodeName.equals("ttl")){
                    try{
                        interval = Integer.parseInt(nodeValue);
                    }
                    catch(Exception e){
                    }
                }
            } 
        }

    }
    
    
  //**************************************************************************
  //** getTitle
  //**************************************************************************
    public String getTitle(){ return title; }
    public void setTitle(String title){ this.title = title; }
    
    
  //**************************************************************************
  //** getDescription
  //**************************************************************************
    public String getDescription(){ return description; }
    public void setDescription(String description){ this.description = description; }
    
    
  //**************************************************************************
  //** getLink
  //**************************************************************************
    public java.net.URL getLink(){ return link; }
    public void setLink(java.net.URL link){ this.link = link; }
    

  //**************************************************************************
  //** getLocation
  //**************************************************************************
  /** Returns location information associated with the current feed (e.g.
   *  GeoRSS element).
   */
    public Location getLocation(){
        return location;
    }

    
  //**************************************************************************
  //** getLastUpdate
  //**************************************************************************
    public java.util.Date getLastUpdate(){
        return lastUpdate;
    }
    
    
  //**************************************************************************
  //** getItems
  //**************************************************************************
  /** Returns a list of items found in an RSS feed. 
   */ 
    public Item[] getItems(){ 
        return items.toArray(new Item[items.size()]);
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

    
  //**************************************************************************
  //** toString
  //**************************************************************************
    public String toString(){
        StringBuffer out = new StringBuffer();
        String br = "\r\n";
        out.append("Title: " + getTitle() + br);
        out.append("Description: " + getDescription() + br);
        out.append("Last Update: " + getLastUpdate() + br);
        out.append("Link: " + getLink() + br);
        if (location!=null){
            out.append("Location: " + location.toWKT() + br);
        }
        return out.toString();
    }

}