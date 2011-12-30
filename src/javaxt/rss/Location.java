package javaxt.rss;
import org.w3c.dom.*;

//******************************************************************************
//**  Location Class
//******************************************************************************
/**
 *   Used to represent a location information associated with an RSS feed or
 *   entry. Supports GeoRSS and W3C Basic Geometry.
 *
 ******************************************************************************/

public class Location {

    private org.w3c.dom.Node node;
    private Object geometry;
    private String lat;
    private String lon;
    private Boolean hasGeometry = null; //Has 3 states: true, false, and null

    private static String[] SupportedGeometryTypes = new String[]{
        "Point", "Line", "Polygon", "LineString", "Box", "Envelope",
        "MultiPoint", "MultiLine", "MultiPolygon", "MultiLineString"
    };

    /** GeoRSS NameSpace */
    private String georss = "georss";

    /** GML NameSpace */
    private String gml = "gml";


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of this class using an XML node. */

    protected Location(org.w3c.dom.Node node, java.util.HashMap<String, String> namespaces) {
        this.node = node;

        String georss = namespaces.get("http://www.georss.org/georss");
        if (georss!=null) this.georss = georss;

        String gml = namespaces.get("http://www.opengis.net/gml");
        if (gml==null) this.gml = gml;
    }

    
  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of this class using a point. */

    protected Location(String lat, String lon){
        this.lat = lat;
        this.lon = lon;
    }


    /*
    public String toGML(){
        return null;
    }*/


  //**************************************************************************
  //** toWKT
  //**************************************************************************
  /**  Used to return a Well-known Text (WKT) representation of the location.
   */
    public String toWKT(){
        if (lat!=null && lon!=null){
            return "POINT(" + lon + " " + lat + ")";
        }
        return (getGeometry()==null ? null : getGeometry().toString());
    }

  //**************************************************************************
  //** toString
  //**************************************************************************
  /**  Used to return a Well-known Text (WKT) representation of the location.
   */
    public String toString(){
        return toWKT();
    }



  //**************************************************************************
  //** getGeometry
  //**************************************************************************
  /** Used convert the location into a geometry object.
   *  @return Returns a javaxt.geospatial.geometry.Geometry or a
   *  com.vividsolutions.jts.geom.Geometry, depending on which library is found
   *  in the classpath. If both libraries are present, will return a
   *  javaxt.geospatial.geometry.Geometry object.
   */
    public Object getGeometry(){

        if (hasGeometry==null){

            if (lat!=null && lon!=null){

                String nodeName = "Point";
                String nodeValue =
                    "<gml:" + nodeName + ">"  +
                    "<gml:coordinates cs=\" \" ts=\",\">" + lon + " " + lat +
                    "</gml:coordinates>" +
                    "</gml:" + nodeName + ">";
                geometry = getGeometry(nodeName, nodeValue);
            }
            else{
            
                String nodeName = node.getNodeName().toLowerCase();
                String nodeValue = Parser.getNodeValue(node).trim();


                if (nodeName.equals("where") || nodeName.equals(georss + ":where")){
                    NodeList nodes = node.getChildNodes();
                    for (int j=0; j<nodes.getLength(); j++){
                        node = nodes.item(j);
                        if (node.getNodeType()==1){
                            nodeName = node.getNodeName();
                            if (isGeometryNode(nodeName.toLowerCase(), gml, georss)){
                                geometry = getGeometry(nodeName, Parser.getNodeValue(node));
                                if (geometry!=null) break;
                            }
                        }
                    }
                }
                else if(isGeometryNode(nodeName, gml, georss)){
                    geometry = getGeometry(nodeName, nodeValue);
                }
            }

            hasGeometry = (geometry==null);
        }

        return geometry;
    }


  //**************************************************************************
  //** isLocationNode
  //**************************************************************************
  /**  Protected method used to help determine whether a node represents a
   *   location.
   */
    protected static boolean isLocationNode(String nodeName, java.util.HashMap<String, String> namespaces){
        String georss = namespaces.get("http://www.georss.org/georss");
        if (georss==null) georss = "georss";

        String gml = namespaces.get("http://www.opengis.net/gml");
        if (gml==null) gml = "gml";

        return (nodeName.equals("where") || nodeName.equals(georss + ":where") ||
            isGeometryNode(nodeName, gml, georss));
    }


  //**************************************************************************
  //** isGeometryNode
  //**************************************************************************
  /**  Private method used to determine whether a node represents a geometry.
   *   @param gml GML NameSpace
   *   @param georss GeoRSS NameSpace
   */
    private static boolean isGeometryNode(String nodeName, String gml, String georss){
        String namespace = null;
        if (nodeName.contains(":")){
            namespace = nodeName.substring(0, nodeName.lastIndexOf(":"));
            nodeName = nodeName.substring(nodeName.lastIndexOf(":")+1);
        }
        if (namespace==null || namespace.equals(gml) || namespace.equals(georss)){
            for (String geometryType : SupportedGeometryTypes){
                if (nodeName.equalsIgnoreCase(geometryType)) return true;
            }
        }
        return false;
    }





  //**************************************************************************
  //** getGeometry
  //**************************************************************************
  /**  Calls javaxt-gis or jts to try to parse location information.
   *   @param nodeName XML node name (e.g. "gml:Point" or "Point"). This
   *   parameter is required to instantiate the JTS Parser. Note that the
   *   namespace is ignored.
   */
    private Object getGeometry(String nodeName, String nodeValue){
        if (nodeValue!=null){
            nodeValue = nodeValue.trim();
            if (nodeValue.length()==0) nodeValue = null;
        }
        if (nodeValue==null) return null;

        try{
          //Try to parse the geometry using the javaxt-gis library
            Class CoordinateParser = new ClassLoader("javaxt.geospatial.coordinate.Parser", "javaxt-gis.jar").load();
            java.lang.reflect.Constructor constructor = CoordinateParser.getDeclaredConstructor(new Class[] {String.class});
            java.lang.reflect.Method method = CoordinateParser.getDeclaredMethod("getGeometry");
            Object instance = constructor.newInstance(new Object[] { nodeValue });
            return method.invoke(instance);
        }
        catch(java.lang.ClassNotFoundException e){

          //Try to parse the geometry using JTS
            try{

              //Hack for JTS Parser to deal with GeoRSS Simple Geometries
                if (!nodeValue.startsWith("<")){
                    String Attributes = "";
                    if (nodeName.contains(":")){
                        nodeName = nodeName.substring(nodeName.indexOf(":")+1);
                    }
                    if (nodeName.equals("point")) nodeName = "Point";
                    else if(nodeName.equals("line")) nodeName = "LineString";
                    else if(nodeName.equals("polygon")) nodeName = "Polygon";

                    String p1 = (nodeName.equals("Polygon") ? "<gml:outerBoundaryIs><gml:LinearRing>" : "" );
                    String p2 = (nodeName.equals("Polygon") ? "</gml:LinearRing></gml:outerBoundaryIs>" : "" );

                    nodeValue =
                        "<gml:" + nodeName + Attributes + ">" + p1 +
                        "<gml:coordinates cs=\" \" ts=\",\">" + fixCoords(nodeValue) +
                        "</gml:coordinates>" + p2 +
                        "</gml:" + nodeName + ">";
                }


              //Hack for JTS Parser to deal with GML pos and posList tags
                for (String pos : new String[]{"pos>", "posList>"}){
                    if (nodeValue.contains(pos)){
                        StringBuffer str = new StringBuffer();
                        String[] arr = nodeValue.split(pos);
                        for (int n=0; n<arr.length; n++){
                            str.append(arr[n]);
                            if (n<arr.length-1){
                                if ((n % 2 == 0)){
                                    str.append("coordinates cs=\" \" ts=\",\">");
                                    String coords = arr[n+1];
                                    arr[n+1] = coords.substring(coords.indexOf("<"));
                                    str.append(fixCoords(coords.substring(0, coords.indexOf("<"))));

                                }
                                else str.append("coordinates>");
                            }
                        }
                        nodeValue = str.toString().trim();
                    }
                }


                if (nodeValue.startsWith("<")){ //GML
                    Class GMLReader = new ClassLoader("com.vividsolutions.jts.io.gml2.GMLReader", "jts").load();
                    for (java.lang.reflect.Method method : GMLReader.getDeclaredMethods()){
                        if (method.getName().equals("read")){
                            Class[] parameters = method.getParameterTypes();
                            if (parameters.length==2){
                                if (parameters[0].getCanonicalName().equals("java.lang.String") &&
                                    parameters[1].getCanonicalName().equals("com.vividsolutions.jts.geom.GeometryFactory") ){

                                    Object instance = GMLReader.newInstance();
                                    return method.invoke(instance, new Object[] { nodeValue, parameters[1].newInstance() });
                                }

                            }
                        }
                    }
                }


            }
            catch(java.lang.reflect.InvocationTargetException ex){
                Throwable cause = ex.getCause();
                if (cause != null){
                    String msg = cause.getLocalizedMessage();
                    if (msg!=null) System.err.println(cause.getLocalizedMessage());
                }

            }
            catch(Exception ex){
                //ex.printStackTrace();
            }


        }
        catch(java.lang.InstantiationException e){}
        catch(java.lang.NoSuchMethodException e){}
        catch(java.lang.IllegalAccessException e){}
        catch(java.lang.reflect.InvocationTargetException e){}
        return null;
    }


  //**************************************************************************
  //** fixCoords
  //**************************************************************************
  /** Used to add commas between coordinate tuples. */

    private static String fixCoords(String coords){
        coords = coords.trim();
        StringBuffer str = new StringBuffer();
        String[] arr = coords.split(" ");
        for (int n=0; n<arr.length; n++){
            str.append(arr[n]);
            if (n<arr.length-1){
                if ((n % 2 == 0)) str.append(" ");
                else str.append(", ");
            }
        }
        return str.toString().trim();
    }

}


//******************************************************************************
//**  ClassLoader
//******************************************************************************
/**
 *   Simple class loader. Loads a class with a given name.
 *
 ******************************************************************************/

class ClassLoader {
    private String className;
    private String jarFile;

    public ClassLoader(String className){
        this.className = className;
    }

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Used to dynamically Load a jar file along with the the given a class.
   *  @param jarFile Base file name of a jar file. Assumes the file location
   *  is in the same directory as this jar file.
   */
    public ClassLoader(String className, String jarFile){
        this.className = className;
        this.jarFile = jarFile;
    }

    public Class load() throws java.lang.ClassNotFoundException {
        try{
            return Class.forName(className);
        }
        catch(java.lang.ClassNotFoundException e){

            if (jarFile!=null){
                try{
                    java.io.File jar = findJar(jarFile);
                    java.net.URLClassLoader child = new java.net.URLClassLoader(
                    new java.net.URL[]{jar.toURL()}, this.getClass().getClassLoader());
                    return Class.forName(className, true, child);
                }
                catch(Exception ex){}
            }

            throw e;
        }
    }

    private java.io.File findJar(String prefix){

        java.lang.Class Class = this.getClass();
        java.lang.Package Package = Class.getPackage();
        java.io.File file = null;

      //Find physical path of this jar file
        String path = Package.getName().replace((CharSequence)".",(CharSequence)"/");
        String url = Class.getClassLoader().getResource(path).toString();
        url = url.replace((CharSequence)" ",(CharSequence)"%20");
        try{
            java.net.URI uri = new java.net.URI(url);
            if (uri.getPath()==null){
                path = uri.toString();
                if (path.startsWith("jar:file:")){

                  //Update Path and Define Zipped File
                    path = path.substring(path.indexOf("file:/"));
                    path = path.substring(0,path.toLowerCase().indexOf(".jar")+4);

                    if (path.startsWith("file://")){ //UNC Path
                        path = "C:/" + path.substring(path.indexOf("file:/")+7);
                        path = "/" + new java.net.URI(path).getPath();
                    }
                    else{
                        path = new java.net.URI(path).getPath();
                    }
                    file = new java.io.File(path);
                }
            }
            else{
                file = new java.io.File(uri);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }


        for (String fileName : file.getParentFile().list()){
            if (fileName.toLowerCase().startsWith(prefix.toLowerCase()) &&
                fileName.toLowerCase().endsWith(".jar"))
            {
                return new java.io.File(file.getParentFile(), fileName);
            }
        }
        return null;

    }
}