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
    
    
    private String title = "";
    private String description = "";
    private String author = null;
    private String creator = null;
    private java.net.URL link = null;
    private java.net.URL origLink = null; //<--FeedBurner
    private Location location = null;
    private NodeList nodeList = null;

    private String pubDate = null;
    private String dcDate = null;
    
    private java.util.ArrayList<Media> media = new java.util.ArrayList<Media>();


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of this class using an XML node. */

    protected Item(org.w3c.dom.Node node, java.util.HashMap<String, String> namespaces) {

        String mediaNS = namespaces.get("http://search.yahoo.com/mrss");
        if (mediaNS==null) mediaNS = "media";

        String geoNS = namespaces.get("http://www.w3.org/2003/01/geo/wgs84_pos#");
        if (geoNS==null) geoNS = "geo";
        

        nodeList = node.getChildNodes();
        String lat = null;
        String lon = null;
        for (int i=0; i<nodeList.getLength(); i++){
            node = nodeList.item(i);
            if (node.getNodeType()==1){
                String nodeName = node.getNodeName().toLowerCase();
                String nodeValue = Parser.getNodeValue(node).trim();

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
                    String url = nodeValue;
                    if (url.length()==0){
                      //get href attribute
                        url = Parser.getAttributeValue(node,"href").trim();
                    }
                    if (url.length()>0){
                       try{ link = new java.net.URL(url); }
                       catch(Exception e){}
                    }
                }

              //Parse FeedBurner Link
                else if(nodeName.equals("feedburner:origLink")){
                    String url = nodeValue.trim();
                    if (url.length()>0){
                        try{ origLink = new java.net.URL(url); }
                        catch(Exception e){}
                    }
                }

                else if (nodeName.equals(mediaNS + ":content")){
                    media.add(new Media(node));
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
            }
        }

        if (lat!=null && lon!=null){
            location = new Location(lat, lon);
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
    public java.util.Date getDate(){
        String date = pubDate;
        if (date==null || date.length()==0) date = dcDate;
        if (date!=null && date.length()>0){
            try{
                return Parser.getDate(date);
            }
            catch(java.text.ParseException e){
            }
        }
        return null;
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

}