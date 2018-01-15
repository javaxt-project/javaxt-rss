package javaxt.rss;
import org.w3c.dom.*;

//******************************************************************************
//**  RSS Item
//******************************************************************************
/**
 *   Used to represent an entry in an RSS feed.
 *
 ******************************************************************************/

public class Item {
    
    private String title;
    private String description;
    private String author = null;
    private String creator = null;
    private String category = null;
    private java.net.URL link = null;
    private java.net.URL origLink = null; //<--FeedBurner
    private java.util.Date date = null;
    private Location location = null;
    private NodeList nodeList = null;
    private java.util.ArrayList<Media> media = new java.util.ArrayList<Media>();

    
  //**************************************************************************
  //** Constructor
  //**************************************************************************
    public Item(){}


  //**************************************************************************
  //** Constructor
  //**************************************************************************
    public Item(String title, java.net.URL link, java.util.Date date){
        this.title = title;
        this.link = link;
        this.date = date;
    }
    

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of this class using an XML node from an RSS Feed. 
   */
    protected Item(org.w3c.dom.Node node, java.util.HashMap<String, String> namespaces) {

        String mediaNS = namespaces.get("http://search.yahoo.com/mrss");
        if (mediaNS==null) mediaNS = "media";

        String geoNS = namespaces.get("http://www.w3.org/2003/01/geo/wgs84_pos#");
        if (geoNS==null) geoNS = "geo";
        

        nodeList = node.getChildNodes();
        String pubDate = null;
        String dcDate = null;
        String lat = null;
        String lon = null;
        java.util.ArrayList<org.w3c.dom.Node> mediaNodes = new java.util.ArrayList<org.w3c.dom.Node>();
        
        for (int i=0; i<nodeList.getLength(); i++){
            node = nodeList.item(i);
            if (node.getNodeType()==1){
                String nodeName = node.getNodeName().toLowerCase();
                String nodeValue = Parser.getNodeValue(node).trim();
                if (nodeValue.length()==0) nodeValue = null;

              //Parse Common Attributes
                if (nodeName.equals("title")) title = nodeValue;
                else if (nodeName.equals("author")) author = nodeValue;
                else if (nodeName.endsWith("creator")) creator = nodeValue;
                else if (nodeName.equalsIgnoreCase("pubDate")) pubDate = nodeValue;
                else if (nodeName.equalsIgnoreCase("dc:date")) dcDate = nodeValue;
                else if(nodeName.equals("description") || nodeName.equals("subtitle")){
                    if (description==null || description.length()==0){
                        description = nodeValue;
                    }
                }

              //Parse Link
                else if(nodeName.equals("link")){
                    if (nodeValue!=null){
                        String url = nodeValue.replace("\"", "");
                        if (url.length()==0){
                          //get href attribute
                            url = Parser.getAttributeValue(node,"href").trim();
                        }
                        if (url.length()>0){
                           try{ link = new java.net.URL(url); }
                           catch(Exception e){}
                        }
                    }
                }

              //Parse FeedBurner Link
                else if(nodeName.equals("feedburner:origLink")){
                    if (nodeValue!=null){
                        try{ origLink = new java.net.URL(nodeValue); }
                        catch(Exception e){}
                    }
                }
                
              //Enclosure (e.g. TASS News agency)
                else if (nodeName.equals("enclosure")){
                    addMedia(new Media(node));
                }
                else if (nodeName.equals("category")){
                    category = nodeValue;
                }
                
                else if(Location.isLocationNode(nodeName, namespaces)){
                    location = new Location(node, namespaces);
                }
                else if (nodeName.equals("lat") || nodeName.equals(geoNS + ":lat")){
                    lat = nodeValue;
                }
                else if (nodeName.equals("long") || nodeName.equals(geoNS + ":long")){
                    lon = nodeValue;
                }
                else{
                    
                    if (nodeName.startsWith(mediaNS + ":")){
                        mediaNodes.add(node);
                    }
                    
                }
            }
        }

        
      //Parse date
        String date = pubDate;
        if (date==null || date.length()==0) date = dcDate;
        if (date!=null && date.length()>0){
            try{
                this.date = Parser.getDate(date);
            }
            catch(java.text.ParseException e){
            }
        }
        
        
      //Parse media nodes
        if (!mediaNodes.isEmpty()){
            
          //Check if there are any content nodes and if those nodes have children (e.g. The Guardian News Feed)
            for (org.w3c.dom.Node mediaNode : mediaNodes){
                String nodeName = mediaNode.getNodeName().toLowerCase();
                if (nodeName.equals(mediaNS + ":content")){

                  
                    org.w3c.dom.NodeList nodeList = mediaNode.getChildNodes();
                    for (int i=0; i<nodeList.getLength(); i++){
                        node = nodeList.item(i);
                        if (node.getNodeType()==1){
                            addMedia(new Media(mediaNode));
                            break;
                        }   
                    }

                }
            }
            
            
          //If none of the of the content nodes have children (standard use case)
            if (media.isEmpty()){
                addMedia(new Media(mediaNodes.toArray(new org.w3c.dom.Node[mediaNodes.size()])));
            }
        }

        
        
      //Set location
        if (lat!=null && lon!=null){
            location = new Location(lat, lon);
        }
    }


  //**************************************************************************
  //** getTitle
  //**************************************************************************
    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    
  //**************************************************************************
  //** getDescription
  //**************************************************************************
    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

  //**************************************************************************
  //** getAuthor
  //**************************************************************************
    public String getAuthor(){
        if (author==null && creator!=null) return creator;
        return author;
    }
    
    public void setAuthor(String author){
        this.author = author;
    }
    
    
  //**************************************************************************
  //** getCategory
  //**************************************************************************
    public String getCategory(){
        return category;
    }

    public void setCategory(String category){
        this.category = category;
    }
    
    
  //**************************************************************************
  //** getLink
  //**************************************************************************
  /** Returns a link/url associated with the current entry. Returns the
   *  'feedburner:origLink' if found. Otherwise returns a url associated with
   *  the 'link' node.
   */
    public java.net.URL getLink(){
        if (origLink!=null) return origLink;
        else return link;
    }

    
  //**************************************************************************
  //** setLink
  //**************************************************************************
    public void setLink(java.net.URL url){
        origLink = null;
        link = url;
    }


  //**************************************************************************
  //** getDate
  //**************************************************************************
  /** Return the date/time stamp associated with the current entry. Uses the
   *  pubDate if it exists. Otherwise, returns dc:date
   */
    public java.util.Date getDate(){
        return date;
    }


  //**************************************************************************
  //** setDate
  //**************************************************************************
    public void setDate(java.util.Date date){
        this.date = date;
    }


  //**************************************************************************
  //** addMedia
  //**************************************************************************
    public void addMedia(Media media){
        this.media.add(media);
    }

    
  //**************************************************************************
  //** getMedia
  //**************************************************************************
  /**  Returns an array of media items associated with the current entry.
   */
    public Media[] getMedia(){
        return media.toArray(new Media[media.size()]);
    }


  //**************************************************************************
  //** getLocation
  //**************************************************************************
  /** Returns location information associated with the current entry (e.g.
   *  GeoRSS element).
   */
    public Location getLocation(){
        return location;
    }

    
  //**************************************************************************
  //** getNodeList
  //**************************************************************************
  /** Returns the NodeList used to instantiate this class via the RSS Parser.
   *  @deprecated This method will be removed in future releases.
   */
    public NodeList getNodeList(){
        return nodeList;
    }

    
  //**************************************************************************
  //** toString
  //**************************************************************************
    public String toString(){
        StringBuffer out = new StringBuffer();
        String br = "\r\n";
        out.append("Title: " + getTitle() + br);
        //out.append("Description: " + getDescription() + br);
        out.append("Author: " + getAuthor() + br);
        out.append("Link: " + getLink() + br);
        out.append("Date: " + getDate() + br);
        
        if (location!=null){
            out.append("Location: " + location.toWKT() + br);
            //out.append("Geometry Name: " + geometry.getName() + br);
            //out.append("Geometry SRS: " + geometry.getSRS() + br);
        }

        for (int i=0; i<media.size(); i++){
            System.out.println(media.get(i));
        }
        
        return out.toString();
    }
    

  //**************************************************************************
  //** toXML
  //**************************************************************************
  /** Returns an XML fragment used by the Feed class to generate an RSS/XML
   *  document.
   */
    protected String toXML(){
        StringBuffer str = new StringBuffer();

        java.util.HashMap<String, Object> info = new java.util.HashMap<String, Object>();

        String title = getTitle();
        if (title!=null){
            title = title.trim();
            if (title.length()>0) info.put("title", title);
        }

        String desc = getDescription();
        if (desc!=null){
            desc = desc.trim();
            if (desc.length()>0) info.put("description", desc);
        }
        
        if (link!=null) info.put("link", link);
        if (date!=null) info.put("pubDate", date);


        if (!info.isEmpty()){
            str.append("  <item>\n");
            java.util.Iterator<String> it = info.keySet().iterator();
            while (it.hasNext()){
                String key = it.next();
                Object val = info.get(key);
                str.append("    <" + key + ">");
                if (val instanceof String){
                    str.append("<![CDATA[");
                    str.append(val);
                    str.append("]]>");
                }
                else if (val instanceof java.util.Date){
                    String d = Feed.formatDate((java.util.Date) val);
                    str.append(d);
                }
                else{
                    str.append(val);
                }
                str.append("</" + key + ">\n");
            }
            str.append("  </item>\n");
        }
        
        return str.toString();
    }
}