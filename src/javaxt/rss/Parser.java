package javaxt.rss;
import org.w3c.dom.*;
import javaxt.xml.DOM;

//******************************************************************************
//**  RSS Parser
//******************************************************************************
/**
 *   Used to parse an RSS document and create a list of feeds/channels.
 *
 ******************************************************************************/

public class Parser {
    
    private Feed[] Feeds = null;
    
    
  //**************************************************************************
  //** Instantiate Parser
  //**************************************************************************
  /** Creates a new instance of Parser */
    
    public Parser(org.w3c.dom.Document doc) {
      //Get Root Node
        Node RootNode = DOM.getOuterNode(doc);
        String RootNodeName = RootNode.getNodeName();
        
        java.util.Vector vec = new java.util.Vector();
        
        if (RootNodeName.equals("rss")){
            NodeList Channels = doc.getElementsByTagName("channel");
            for (int i=0; i<Channels.getLength(); i++){
                 vec.add(new Feed(Channels.item(i)));
            }
        }
        else if (RootNodeName.equals("feed")){
            vec.add(new Feed(RootNode));
        }
        else{
            //throw an error?
            System.out.println(RootNodeName);
        }
        
      //Convert Vector to Array
        Object[] arr = vec.toArray();
        Feeds = new Feed[arr.length];
        for (int i=0; i<Feeds.length; i++){
             Feeds[i] = (Feed) arr[i];
        }
        
    }


    public Parser(String url) {
        this(new javaxt.http.Request(url).getResponse().getXML());
    }
    

    
  //**************************************************************************
  //** getFeeds
  //**************************************************************************
  /** Returns an array of "feeds". A "feed" in RSS is a called a "channel" */
    
    public Feed[] getFeeds(){
        return Feeds;
    }
    
    
}
