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
		  

		  
	    //[START original_image]
	    // Read the image.jpg resource into a ByteBuffer.
		  /*System.out.println("Test");
		ServletContext context=getServletContext();
		URL resource=context.getResource("/WEB-INF/image.jpg");
		File file = null;
		try {
			file = new File(resource.toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    FileInputStream fileInputStream = new FileInputStream(file);
	    FileChannel fileChannel = fileInputStream.getChannel();
	    ByteBuffer byteBuffer = ByteBuffer.allocate((int)fileChannel.size());
	    fileChannel.read(byteBuffer);

	    byte[] imageBytes = byteBuffer.array();
	    System.out.println("Test2");
	    
	    
	    
	    
	    
	    // Write the original image to Cloud Storage
	    gcsService.createOrReplace(
	        new GcsFilename(bucket, "image.jpg"),
	        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
	        ByteBuffer.wrap(imageBytes));
	    //[END original_image]

	    //[START resize]
	    // Get an instance of the imagesService we can use to transform images.
	    ImagesService imagesService = ImagesServiceFactory.getImagesService();

	    // Make an image directly from a byte array, and transform it.
	    Image image = ImagesServiceFactory.makeImage(imageBytes);
	    Transform resize = ImagesServiceFactory.makeResize(100, 50);
	    Image resizedImage = imagesService.applyTransform(resize, image);
	    System.out.println("----------------------------");
	    System.out.println(resizedImage);

	    // Write the transformed image back to a Cloud Storage object.
	    gcsService.createOrReplace(
	        new GcsFilename(bucket, "resizedImage.jpeg"),
	        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
	        ByteBuffer.wrap(resizedImage.getImageData()));
	    //[END resize]
*/	    
		  ImagesService imagesService = ImagesServiceFactory.getImagesService();


	    //[START rotate]
	    // Make an image from a Cloud Storage object, and transform it.
	    
	    //BlobstoreService allows you to manage the creation and serving of large, immutable blobs to users. 
	    System.out.println("Test3");
	    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	    BlobKey blobKey = blobstoreService.createGsBlobKey("/gs/" + bucket + "/image.jpg"); // Creating a BlobKey for a Google Storage File.
	    //BlobKey blobKey = blobstoreService.createGsBlobKey("//storage.googleapis.com/" + bucket + "/Test/unnamed.jpg");
	    
	    Image blobImage = ImagesServiceFactory.makeImageFromBlob(blobKey); // Create an image backed by the specified blobKey.
	    Transform rotate = ImagesServiceFactory.makeResize(125, 75);
	    Image rotatedImage = imagesService.applyTransform(rotate, blobImage);

	    // Write the transformed image back to a Cloud Storage object.
	    gcsService.createOrReplace(
	        new GcsFilename(destinationFolder, "rotatedImage.jpeg"),
	        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
	        ByteBuffer.wrap(rotatedImage.getImageData()));
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
