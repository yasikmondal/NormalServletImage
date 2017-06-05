/**
* Copyright 2015 Google Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.laykart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
//@MultipartConfig
@SuppressWarnings("serial")
public class ImagesServlet  extends HttpServlet {
	

	  final String bucket = "laykart-165108.appspot.com";
	final String destinationFolder = "laykart-165108.appspot.com/1xConvert";
	  
	  
	  

	  // [START gcs]
	  
	  //Allows creating and accessing files in Google Cloud Storage.
	  private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
	      .initialRetryDelayMillis(10)
	      .retryMaxAttempts(10)
	      .totalRetryPeriodMillis(15000)
	      .build());
	  // [END gcs]
	  
	  /*@Override
	  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
		  
		  
		    Part filePart = req.getPart("fileName"); // Retrieves <input type="file" name="file">
		    //String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
		    //InputStream fileContent = filePart.getInputStream();
		  
		  String newImageUrl = null;
		    CloudStorageHelper cludhelp=new CloudStorageHelper();
			  newImageUrl = cludhelp.uploadFile( filePart , bucket);
			  
		  //doGet(req, resp);
			  System.out.println(newImageUrl);
			  
	  }*/

	  @SuppressWarnings("resource")
	@Override
	  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		  

	    	int[] sizes = {125,75,300,250,90,90,350,175};
		  ImagesService imagesService = ImagesServiceFactory.getImagesService();


	    //[START rotate]
	    // Make an image from a Cloud Storage object, and transform it.
	    
	    //BlobstoreService allows you to manage the creation and serving of large, immutable blobs to users. 
	    System.out.println("Test3");
	    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	    BlobKey blobKey = blobstoreService.createGsBlobKey("/gs/" + bucket + "/image.jpg"); // Creating a BlobKey for a Google Storage File.
	    //BlobKey blobKey = blobstoreService.createGsBlobKey("//storage.googleapis.com/" + bucket + "/Test/unnamed.jpg");
	    
	    Image blobImage = ImagesServiceFactory.makeImageFromBlob(blobKey); // Create an image backed by the specified blobKey.
		  for(int i=0; i< sizes.length; i++){
	    	
	    	int width =(Integer)sizes[i];
            int height = (Integer)sizes[i+1];
            System.out.println(width + "X" + height);
            
            Transform resize_125x75 = ImagesServiceFactory.makeResize(width, height);
    	    Image resizeImage = imagesService.applyTransform(resize_125x75, blobImage);

    	    // Write the transformed image back to a Cloud Storage object.
    	    gcsService.createOrReplace(
    	        new GcsFilename(destinationFolder, "resizeImage"+width + "x" + height+ ".jpeg"),
    	        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
    	        ByteBuffer.wrap(resizeImage.getImageData()));

	    	i++;
	    }
		  
		  
		  
		  
// 	    Transform resize_125x75 = ImagesServiceFactory.makeResize(125, 75);
// 	    Image resizeImage_125x75 = imagesService.applyTransform(resize_125x75, blobImage);

// 	    // Write the transformed image back to a Cloud Storage object.
// 	    gcsService.createOrReplace(
// 	        new GcsFilename(destinationFolder, "resizeImage_125x75.jpeg"),
// 	        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
// 	        ByteBuffer.wrap(resizeImage_125x75.getImageData()));
	    //[END rotate]
	    System.out.println("Test4");
	    // Output some simple HTML to display the images we wrote to Cloud Storage
	    // in the browser.
	    PrintWriter out = resp.getWriter();
	    out.println("<html><body>\n");
	    out.println("<img src='https://storage.googleapis.com/" + bucket
	        + "/image.jpeg' alt='AppEngine logo' />");
	    out.println("<img src='https://storage.googleapis.com/" + bucket
	        + "/resizedImage.jpeg' alt='AppEngine logo resized' />");
	    out.println("<img src='https://storage.googleapis.com/" + bucket
	        + "/rotatedImage.jpeg' alt='AppEngine logo rotated' />");
	    out.println("</body></html>\n");
	  }


}
