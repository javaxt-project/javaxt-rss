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
    private Object geometry = null;

    private java.util.Date lastUpdate = null;
    private Integer interval = null;
    
    private Item[] Items = null;

    
  //**************************************************************************
  //** Instantiate Feed
  //**************************************************************************
  /** Creates a new instance of Feed */
    
    protected Feed(org.w3c.dom.Node node) {
        java.util.ArrayList<Item> items = new java.util.ArrayList<Item>();
        NodeList nodeList = node.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++){
            node = nodeList.item(i);
            if (node.getNodeType()==1){
                String nodeName = node.getNodeName().toLowerCase();
                String nodeValue = Parser.getNodeValue(node).trim();
                if (nodeName.equals("title")) title = nodeValue;
                if (nodeName.equals("description") || nodeName.equals("subtitle")){
                    description = nodeValue;
                }


                //Parse Location Information (GeoRSS)
                if (nodeName.equals("where") || nodeName.equals("georss:where")){
                    NodeList nodes = node.getChildNodes();
                    for (int j=0; j<nodes.getLength(); j++){
                        if (nodes.item(j).getNodeType()==1){
                            if (Item.isGeometryNode(nodes.item(j).getNodeName().toLowerCase())){
                                geometry = Item.getGeometry(Parser.getNodeValue(nodes.item(j)).trim());
                                if (geometry!=null) break;
                            }
                        }
                    }
                }
                if (Item.isGeometryNode(nodeName)){
                    geometry = Item.getGeometry(nodeValue);
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
                    items.add(new Item(node));
                }

                if (nodeName.equalsIgnoreCase("lastBuildDate")){
                    if (nodeValue!=null){
                        try{
                            lastUpdate = Parser.getDate(nodeValue);
                        }
                        catch(java.text.ParseException e){
                            lastUpdate = null;
                        }
                    }
                }

                if (nodeName.equals("ttl")){
                    try{
                        interval = Integer.parseInt(nodeValue);
                    }
                    catch(Exception e){
                    }
                }
            }
        }

        this.Items = items.toArray(new Item[items.size()]);
    }
    
    public String getTitle(){ return title; }
    public String getDescription(){ return description; }
    public java.net.URL getLink(){ return link; }
    public Item[] getItems(){ return Items; }
    public Object getLocation(){ return geometry; }

    public java.util.Date getLastUpdate(){
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