/**
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version. 
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/
package ddf.catalog.transformer.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.log4j.Logger;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.BinaryContentImpl;
import ddf.catalog.data.Metacard;
import ddf.catalog.operation.ResourceRequest;
import ddf.catalog.operation.ResourceRequestById;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.resource.ResourceNotFoundException;
import ddf.catalog.resource.ResourceNotSupportedException;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.MetacardTransformer;

/**
 * 
 * This transformer uses the Catalog Framework to obtain and return
 * the resource based on the metacard id.
 * 
 * @author Tim Anderson
 * @author ddf.isgs@lmco.com
 *
 */
public class ResourceMetacardTransformer implements MetacardTransformer {

   private static final Logger LOGGER = Logger
         .getLogger(ResourceMetacardTransformer.class);

   private CatalogFramework catalogFramework;
   
   private static final String DEFAULT_MIME_TYPE_STR = "application/octet-stream";
   

   public ResourceMetacardTransformer(CatalogFramework framework) {
      LOGGER.debug("constructing resource metacard transformer");
      this.catalogFramework = framework;
   }

   @Override
   public BinaryContent transform(Metacard metacard,
         Map<String, Serializable> arguments)
         throws CatalogTransformerException {

      if ( LOGGER.isTraceEnabled() ) {
         LOGGER.trace("Entering resource ResourceMetacardTransformer.transform");
      }
      
      if ( ! isValid( metacard )) {
         throw new CatalogTransformerException( "Could not transform metacard to a resource because the metacard is not valid.");
      }
      
      String id = metacard.getId();
      
      BinaryContent transformedContent = null;

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("executing resource request with id '" + id + "'");
      }
      final ResourceRequest resourceRequest = new ResourceRequestById(id, arguments);
      
      ResourceResponse resourceResponse = null;

      try {

         resourceResponse = catalogFramework.getResource(resourceRequest,
               catalogFramework.getId());

      } catch (IOException e) {
         throw new CatalogTransformerException(
               "Unable to retrieve resource for the requested metacard with id: '" + id + "'.", e);
      } catch (ResourceNotFoundException e) {
         throw new CatalogTransformerException(
               "Unable to retrieve resource for the requested metacard with id: '" + id + "'.", e);
      } catch (ResourceNotSupportedException e) {
         throw new CatalogTransformerException(
               "Unable to retrieve resource for the requested metacard with id: '" + id + "'.", e);
      }

      if (resourceResponse == null) {
         throw new CatalogTransformerException(
               "Resource response is null: Unable to retrieve the product for the metacard with id: '" + id + "'.");
      }

      final InputStream inputStream = resourceResponse.getResource().getInputStream();
      MimeType mimeType = resourceResponse.getResource().getMimeType();

      if (mimeType == null) {
         try {
            mimeType = new MimeType(DEFAULT_MIME_TYPE_STR);
         } catch (MimeTypeParseException e) {
            throw new CatalogTransformerException( "Could not create default mime type upon null mimeType, for default mime type '" + DEFAULT_MIME_TYPE_STR + "'.", e);
         }
      }
      if ( LOGGER.isDebugEnabled() ) {
         LOGGER.debug("Found mime type: '" + mimeType.toString() + "'" + 
                   " for product of metacard with id: '" + id + "'." +
                   "\nGetting associated resource from input stream. \n");
      }
      transformedContent = new BinaryContentImpl(inputStream, mimeType);
      if ( LOGGER.isTraceEnabled() ) {
         LOGGER.trace("Exiting resource transform for metacard id: '"
            + id + "'");
      }
      return transformedContent;
   }
   
   /**
    * Checks to see whether the given metacard is valid.  If it is not valid,
    * it will return false, otherwise true.
    * 
    * @param metacard The metacard to be validated.
    * @return boolean indicating valid.
    */
   private boolean isValid(Metacard metacard ) {
      boolean valid = true;
      if ( metacard == null ) {
         LOGGER.warn("Metacard cannot be null");
         return false;
      }
      if ( metacard.getId() == null ) {
         LOGGER.warn("Metacard id cannot be null");
         return false;
      }
     return valid;
   }

}
