/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.channels;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jasig.portal.IMimeResponse;
import org.jasig.portal.PortalException;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.PersonDirectory;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * This channel demonstrates the method of obtaining and displaying
 * standard uPortal person attributes.
 *
 * Implements IMimeResponse in order to support the inline display of jpegPhotos
 * Note:  for proper operation, one should use an idempotent baseActionURL.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Yuji Shinozaki, ys2n@virginia.edu
 * @version $Revision$ $Date$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class CPersonAttributes extends BaseChannel implements IMimeResponse {

  private static final String sslLocation = "CPersonAttributes/CPersonAttributes.ssl";

  public void renderXML (ContentHandler out) throws PortalException {
    IPerson person = staticData.getPerson();
    Document doc = DocumentFactory.getNewDocument();

    Element attributesE = doc.createElement("attributes");
    
    IPersonAttributeDao pa = PersonDirectory.getPersonAttributeDao();
    Set<String> possibleAttrs = pa.getPossibleUserAttributeNames();
    
    if (possibleAttrs != null)
        possibleAttrs = new HashSet<String>(possibleAttrs);
    else
        possibleAttrs = new HashSet<String>();
        
    for (Map.Entry<String,List<Object>> y : pa.getPerson(person.getUserName()).getAttributes().entrySet()) {
        
      // Remove this attr from the list of possible attrs
      possibleAttrs.remove(y.getKey());
      
      // Set the attribute
      Element attributeE = doc.createElement("attribute");

      Element nameE = doc.createElement("name");
      nameE.appendChild(doc.createTextNode(y.getKey()));
      attributeE.appendChild(nameE);

      // Get the IPerson attribute value for this eduPerson attribute name
      if (y.getValue() != null) {
        Object[] values = y.getValue().toArray();
        for (int i = 0; i < values.length; i++) {
           if (log.isTraceEnabled())
               log.trace("type of value["+i+"] is " + values[i].getClass().getName());
           String value = String.valueOf(values[i]);
           Element valueE = doc.createElement("value");
           valueE.appendChild(doc.createTextNode(value));
           attributeE.appendChild(valueE);
        }
      }

      attributesE.appendChild(attributeE);
    }
    
    //Sort the set of possible attributes
    possibleAttrs = new TreeSet<String>(possibleAttrs);
    
    //Add the unknown attributes to the element list.
    for (Iterator<String> attribs = possibleAttrs.iterator(); attribs.hasNext(); ) {
        // Get the attribute name
        String attName = attribs.next();
        
        // Set the attribute
        Element attributeE = doc.createElement("attribute");

        Element nameE = doc.createElement("name");
        nameE.appendChild(doc.createTextNode(attName));
        attributeE.appendChild(nameE);

        attributesE.appendChild(attributeE);
    }

    doc.appendChild(attributesE);

    XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
    xslt.setXML(doc);
    xslt.setStylesheetParameter("baseActionURL",runtimeData.getBaseActionURL());
    xslt.setStylesheetParameter("downloadWorkerURL",
                                 runtimeData.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER,true));
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.transform();
  }


  // IMimeResponse implementation -- ys2n@virginia.edu
    /**
     * Returns the MIME type of the content.
     */
    public java.lang.String getContentType () {
        // In the future we will need some sort of way of grokking the
        // mime-type of the byte-array and returning an appropriate mime-type
        // Right now there is no good way of doing that, and we will
        // assume that the only thing we will be delivering is a jpegPhoto.
        // attribute with a mimetype of image/jpeg
        // In the future however, we may need a way to deliver different
        // attributes as differenct mimetypes (e.g certs).
        //
        String mimetype;
        // runtime parameter "attribute" determines which attribute to return when
        // called as an IMimeResponse.
        String attrName = runtimeData.getParameter("attribute");

        if ("jpegPhoto".equals(attrName)) {
            mimetype="image/jpeg";
        }
        else {
            // default -- an appropriate choice?
            mimetype="application/octet-stream";
        }
        return mimetype;
    }

    /**
     * Returns the MIME content in the form of an input stream.
     * Returns null if the code needs the OutputStream object
     */
    public java.io.InputStream getInputStream () throws IOException {
        String attrName = runtimeData.getParameter("attribute");
        IPerson person = staticData.getPerson();

        if ( attrName == null ) {
            attrName = "";
        }

        // get the image out of the IPerson as a byte array.
        // Note:  I am assuming here that the only thing that this
        // IMimeResponse will return is a jpegPhoto.  Some other
        // generalized mechanism will need to be inserted here to
        // support other mimetypes and IPerson attributes.
        byte[] imgBytes = (byte [])person.getAttribute(attrName);

        // need to create a ByteArrayInputStream()

        if ( imgBytes == null ) {
            imgBytes = new byte[0]; // let's avoid a null pointer
        }
        java.io.InputStream is = (java.io.InputStream) new java.io.ByteArrayInputStream(imgBytes);

        return is;
    }

    /**
     * Pass the OutputStream object to the download code if it needs special handling
     * (like outputting a Zip file).  Unimplemented.
     */
    public void downloadData (OutputStream out) throws IOException {
    }

    /**
     * Returns the name of the MIME file.
     */
    public java.lang.String getName () {
        // As noted above the only attribute we support right now is "image/jpeg" for
        // the jpegPhoto attribute.

        String payloadName;
        if ("jpegPhoto".equals(runtimeData.getParameter("attribute")))
            payloadName = "image.jpg";
        else
            payloadName = "unknown";
        return payloadName;
    }

    /**
     * Returns a list of header values that can be set in the HttpResponse.
     * Returns null if no headers need to be set.
     */
    public Map getHeaders () {
        return null;
    }

    /**
     * Let the channel know that there were problems with the download
     * @param e
     */
    public void reportDownloadError(Exception e) {
      log.error(e.getMessage(), e);
    }

}