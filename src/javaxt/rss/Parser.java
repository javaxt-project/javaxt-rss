package javaxt.rss;
import org.w3c.dom.*;

//******************************************************************************
//**  RSS Parser
//******************************************************************************
/**
 *   Used to parse an RSS document and create a list of feeds/channels.
 *
 ******************************************************************************/

public class Parser {
    
    private Feed[] feeds = null;
    
    
  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of the RSS Parser */
    
    public Parser(org.w3c.dom.Document doc) {
      
        Node node = getOuterNode(doc);
        String nodeName = node.getNodeName();

      //Get namespaces
        java.util.HashMap<String, String> namespaces = new java.util.HashMap<String, String>();
        NamedNodeMap attributes = node.getAttributes();
        for (int i=0; i<attributes.getLength(); i++){
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            if (attrName.startsWith("xmlns:")){
                attrName = attrName.substring(attrName.indexOf(":")+1);
                String attrValue = attr.getNodeValue(); 
                if (attrValue!=null){
                    if (attrValue.endsWith("/")) attrValue = attrValue.substring(0, attrValue.length()-1);
                    namespaces.put(attrValue.toLowerCase(), attrName);
                }
            }
        }

        
        java.util.ArrayList<Feed> feeds = new java.util.ArrayList<Feed>();
        if (nodeName.equals("rss")){
            NodeList Channels = doc.getElementsByTagName("channel");
            for (int i=0; i<Channels.getLength(); i++){
                 feeds.add(new Feed(Channels.item(i), namespaces));
            }
        }
        else if (nodeName.equals("feed")){
            feeds.add(new Feed(node, namespaces));
        }
        else{
            //throw an error?
        }

        this.feeds = feeds.toArray(new Feed[feeds.size()]);
        
    }


  //**************************************************************************
  //** getFeeds
  //**************************************************************************
  /** Returns an array of "feeds". A "feed" in RSS is a called a "channel" */

    public Feed[] getFeeds(){
        return feeds;
    }



// <editor-fold defaultstate="collapsed" desc="XML and Date utilities copied from javaxt-core.">


  //**************************************************************************
  //** getOuterNode
  //**************************************************************************
  /** Returns the outer node for a given xml document.
   *  @param xml A org.w3c.dom.Document
   */
    protected static Node getOuterNode(Document xml){
        if (xml==null) return null;
        NodeList OuterNodes = xml.getChildNodes();
        for (int i=0; i<OuterNodes.getLength(); i++ ) {
             if (OuterNodes.item(i).getNodeType() == 1){
                 return OuterNodes.item(i);
             }
        }
        return null;
    }

  //**************************************************************************
  //** getAttributeValue
  //**************************************************************************
  /**  Used to return the value of a given node attribute. The search is case
   *   insensitive. If no match is found, returns an empty string.
   */
    protected static String getAttributeValue(Node node, String attrName){

        NamedNodeMap attrCollection = node.getAttributes();
        if (attrCollection!=null){
            for (int i=0; i < attrCollection.getLength(); i++ ) {
                Node attr = attrCollection.item(i);
                if (attr.getNodeName().equalsIgnoreCase(attrName)) {
                    return attr.getNodeValue();
                }
            }
        }
        return "";
    }


  //**************************************************************************
  //** getNodeValue
  //**************************************************************************
  /** Returns the value of a given node as text.
   */
    protected static String getNodeValue(Node node){

        String nodeValue = "";

        if (hasChildren(node)) {

            StringBuffer xmlTree = new StringBuffer();
            traverse(node, xmlTree);
            nodeValue = xmlTree.toString();

        }
        else{
            nodeValue = node.getTextContent();
        }

        if (nodeValue == null){
            return "";
        }
        else{
            return nodeValue;
        }
    }

    private static void traverse(Node tree, StringBuffer xmlTree){
        if (tree.getNodeType()==1){
            String Attributes = getAttributes(tree);
            xmlTree.append("<" + tree.getNodeName() + Attributes + ">");
            if (hasChildren(tree)) {

                NodeList xmlNodeList = tree.getChildNodes();
                for (int i=0; i<xmlNodeList.getLength(); i++){
                    traverse(xmlNodeList.item(i), xmlTree);
                }

            }
            else{

                String nodeValue = tree.getTextContent();
                if (nodeValue == null){
                    nodeValue = "";
                }

                xmlTree.append(nodeValue);
            }

            xmlTree.append("</" + tree.getNodeName() + ">");
        }
    }

  //**************************************************************************
  //** getAttributes
  //**************************************************************************
  /** Used to retrieve all of the attributes for a given node.   */

    protected static String getAttributes(Node node){
        if (node==null) return "";
        NamedNodeMap attr = node.getAttributes();
        String Attributes = "";
        if (attr!=null){
            for (int j=0; j<attr.getLength(); j++){
                 String name = attr.item(j).getNodeName();
                 String value = attr.item(j).getTextContent();
                 if (value==null) value = attr.item(j).getNodeValue();
                 if (value==null) value = "";
                 //System.out.println(name + "=" + attr.item(j).getNodeValue());
                 Attributes += " " + name + "=\"" + value + "\"";
            }
        }
        return Attributes;
    }

  //**************************************************************************
  //** hasChildren
  //**************************************************************************
  /** Used to determine whether a given node has any children. Differs from the
   *  native DOM implementation in that this function only considers child
   *  nodes that have a node type value equal to 1.
   */
    private static boolean hasChildren(Node node){

        NodeList nodeList = node.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++ ) {
            if (nodeList.item(i).getNodeType()==1){
                return true;
            }
        }
        return false;
    }


    private static String[] SupportedFormats = new String[] {

         "EEE, d MMM yy HH:mm:ss z",   // Mon, 07 Jun 76 13:02:09 EST

         "EEE, d MMM yyyy HH:mm:ss z",  // Mon, 7 Jun 1976 13:02:09 EST
         "EEE, dd MMM yyyy HH:mm:ss z", // Mon, 07 Jun 1976 13:02:09 EST

         "EEE MMM dd HH:mm:ss z yyyy",  // Mon Jun 07 13:02:09 EST 1976
         "EEE MMM d HH:mm:ss z yyyy",   // Mon Jun 7 13:02:09 EST 1976

         "EEE MMM dd HH:mm:ss yyyy",    // Mon Jun 07 13:02:09 1976
         "EEE MMM d HH:mm:ss yyyy",     // Mon Jun 7 13:02:09 1976

         "yyyy-MM-dd HH:mm:ss.SSSZ",    // 1976-06-07 01:02:09.000-0500
         "yyyy-MM-dd HH:mm:ss.SSS",     // 1976-06-07 01:02:09.000

         "yyyy-MM-dd HH:mm:ssZ",        // 1976-06-07 13:02:36-0500
         "yyyy-MM-dd HH:mm:ss",         // 1976-06-07 01:02:09


         "yyyy:MM:dd HH:mm:ss",         // 1976:06:07 01:02:09 (exif metadata)

         "yyyy-MM-dd-HH:mm:ss.SSS",     // 1976-06-07-01:02:09.000
         "yyyy-MM-dd-HH:mm:ss",         // 1976-06-07-01:02:09

       //"yyyy-MM-ddTHH:mm:ss.SSS",     // 1976-06-07T01:02:09.000
       //"yyyy-MM-ddTHH:mm:ss",         // 1976-06-07T01:02:09

         "dd-MMM-yyyy h:mm:ss a",       // 07-Jun-1976 1:02:09 PM
         "dd-MMM-yy h:mm:ss a",         // 07-Jun-76 1:02:09 PM
       //"d-MMM-yy h:mm:ss a",          // 7-Jun-76 1:02:09 PM


         "yyyy-MM-dd HH:mmZ",           // 1976-06-07T13:02-0500
         "yyyy-MM-dd HH:mm",            // 1976-06-07T13:02
         "yyyy-MM-dd",                  // 1976-06-07

         "dd-MMM-yy",                   // 07-Jun-76
       //"d-MMM-yy",                    // 7-Jun-76
         "dd-MMM-yyyy",                 // 07-Jun-1976

         "MMMMMM d, yyyy",              // June 7, 1976

         "M/d/yy h:mm:ss a",            // 6/7/1976 1:02:09 PM
         "M/d/yy h:mm a",               // 6/7/1976 1:02 PM

         "MM/dd/yyyy HH:mm:ss",         // 06/07/1976 13:02:09
         "MM/dd/yyyy HH:mm",            // 06/07/1976 13:02

         "M/d/yy",                      // 6/7/76
         "MM/dd/yyyy",                  // 06/07/1976
         "M/d/yyyy",                    // 6/7/1976

         "yyyyMMddHHmmssSSS",           // 19760607130200000
         "yyyyMMddHHmmss",              // 19760607130200
         "yyyyMMdd"                     // 19760607

    };

  //**************************************************************************
  //** getDate
  //**************************************************************************
  /**  Used to convert a string to a date
   */
    protected static java.util.Date getDate(String date) throws java.text.ParseException {

        try{

          //Special Case: Java fails to parse the "T" in strings like
          //"1976-06-07T01:02:09.000" and "1976-06-07T13:02-0500"
            if (date.length()>="1976-06-07T13:02".length()){
                if (date.substring(10, 11).equalsIgnoreCase("T")){
                    date = date.replace("T", " ");
                }
            }


          //Loop through all known date formats and try to convert the string to a date
            for (int i=0; i<SupportedFormats.length; i++){

              //Special Case: Java fails to parse the "Z" in "1976-06-07 00:00:00Z"
                if (date.endsWith("Z") && SupportedFormats[i].endsWith("Z")){
                    date = date.substring(0, date.length()-1) + "UTC";
                }

                try{
                    return parseDate(date, SupportedFormats[i]);
                }
                catch(java.text.ParseException e){
                }
            }

        }
        catch(Exception e){
        }

      //If we're still here, throw an exception
        throw new java.text.ParseException("Failed to parse date: " + date, 0);

    }

  //**************************************************************************
  //** ParseDate
  //**************************************************************************
  /**  Attempts to convert a String to a Date via the user-supplied Format */

    private static java.util.Date parseDate(String date, String format)
            throws java.text.ParseException {

        if (date!=null){
            date = date.trim();
            if (date.length()==0) date = null;
        }
        if (date==null) throw new java.text.ParseException("Date is null.", 0);

        java.text.SimpleDateFormat formatter =
                new java.text.SimpleDateFormat(format);

        return formatter.parse(date);
    }
  // </editor-fold>
}