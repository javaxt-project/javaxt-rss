package javaxt.rss;

//******************************************************************************
//**  Media Class
//******************************************************************************
/**
 *   Used to parse a media content node associated with an RSS entry
 *
 ******************************************************************************/

public class Media {

    private String type;
    private java.net.URL url;
    private String credit;
    private String description;



  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of Media. */

    protected Media(org.w3c.dom.Node node) {
        type = Parser.getAttributeValue(node,"type").trim();

      //Parse url
        String link = Parser.getAttributeValue(node,"url").trim();
        if (link.length()>0){
            try{ url = new java.net.URL(link); }
            catch(Exception e){}
        }

        org.w3c.dom.NodeList nodeList = node.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++){
            node = nodeList.item(i);
            if (node.getNodeType()==1){
                String nodeName = node.getNodeName();
                if (nodeName.endsWith("credit")){
                    credit = Parser.getNodeValue(node).trim();
                }
                else if (nodeName.endsWith("description")){
                    description = Parser.getNodeValue(node).trim();
                }
            }
        }
    }

    public String getType(){ return type; }
    public String getCredit(){ return credit; }
    public String getDescription(){ return description; }
    public java.net.URL getLink(){ return url; }

    public String toString(){

        StringBuffer out = new StringBuffer();
        String br = "\r\n";

        String mimeType = getType();
        if (mimeType.contains("/")) mimeType = mimeType.substring(0, mimeType.indexOf("/"));
        mimeType = mimeType.substring(0,1).toUpperCase() + mimeType.substring(1);
        out.append(mimeType + ": " + getLink() + br);
        out.append("Description: " + getDescription() + br);
        out.append("Credit: " + getCredit() + br);
        return out.toString();

    }

}