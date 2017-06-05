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
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
//package com.example.appengine.images;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
public class ImagesServlet extends HttpServlet {
  final String bucket = "laykart-165108.appspot.com";
  String bucketName = "laykart-165108.appspot.com";
  String destinationFolder = "laykart-165108.appspot.com/1xConvert";

  // [START gcs]
  private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
      .initialRetryDelayMillis(10)
      .retryMaxAttempts(10)
      .totalRetryPeriodMillis(15000)
      .build());
  // [END gcs]
  
  public static List<StorageObject> listBucket(String bucketName)
                      throws IOException, GeneralSecurityException {
                    Storage client = StorageFactory.getService();
                    Storage.Objects.List listRequest = client.objects().list(bucketName);

                    List<StorageObject> results = new ArrayList<StorageObject>();
                    Objects objects;

                    // Iterate through each page of results, and add them to our results list.
                    do {
                      objects = listRequest.execute();
                      // Add the items in this page of results to the list we'll return.
                      results.addAll(objects.getItems());

                      // Get the next page, in the next iteration of this loop.
                      listRequest.setPageToken(objects.getNextPageToken());
                    } while (null != objects.getNextPageToken());

                    return results;
                  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

                  
                 /* List<StorageObject> bucketContents = null;
                try {
                                bucketContents = listBucket(bucketName);
                } catch (GeneralSecurityException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                }
                  if (null == bucketContents) {
                        System.out.println(
                            "There were no objects in the given bucket; try adding some and re-running.");
                      }*/
                  //for (StorageObject object : bucketContents) {
                                  
                                //System.out.print(object.getName());  
                  
    //[START original_image]
    // Read the image.jpg resource into a ByteBuffer.
                  ServletContext context = getServletContext();
                  //if("https://storage.googleapis.com/laykart-165108.appspot.com/leyKart-images/B1/G1.png".equals(object.getName())){
                                  
                  
                URL resource = context.getResource("https://storage.googleapis.com/laykart-165108.appspot.com/leyKart-images/B1/G1.png");
                  /*//URL resource = context.getResource(imgPath + object.getName());
                                File file = null;

                                try {
                                                file = new File(resource.toURI());
                                } catch (URISyntaxException e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                }
                                //System.out.println(resource);
                                System.out.println(file);
                                
                                
    FileInputStream fileInputStream = new FileInputStream(file);
    FileChannel fileChannel = fileInputStream.getChannel();
    ByteBuffer byteBuffer = ByteBuffer.allocate((int)fileChannel.size());
    fileChannel.read(byteBuffer);

    byte[] imageBytes = byteBuffer.array();

    // Write the original image to Cloud Storage
   gcsService.createOrReplace(
        new GcsFilename(bucket, "image.jpeg"),
        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
        ByteBuffer.wrap(imageBytes));*/
    //[END original_image]

    //[START resize]
    // Get an instance of the imagesService we can use to transform images.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();

    /*// Make an image directly from a byte array, and transform it.
    Image image = ImagesServiceFactory.makeImage(imageBytes);
    Transform resize = ImagesServiceFactory.makeResize(100, 50);
    Image resizedImage = imagesService.applyTransform(resize, image);

    // Write the transformed image back to a Cloud Storage object.
    gcsService.createOrReplace(
        new GcsFilename(bucket, "resizedImage2.jpeg"),
        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
        ByteBuffer.wrap(resizedImage.getImageData()));*/
    //[END resize]

    //[START rotate]
    // Make an image from a Cloud Storage object, and transform it.
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    BlobKey blobKey = blobstoreService.createGsBlobKey("/gs/" + bucket + "/image2.jpeg");
    Image blobImage = ImagesServiceFactory.makeImageFromBlob(blobKey);
    Transform rotate = ImagesServiceFactory.makeRotate(90);
    Image rotatedImage = imagesService.applyTransform(rotate, blobImage);

    // Write the transformed image back to a Cloud Storage object.
    gcsService.createOrReplace(
        new GcsFilename(bucket, "rotatedImage2.jpeg"),
        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
        ByteBuffer.wrap(rotatedImage.getImageData()));
    //[END rotate]

    // Output some simple HTML to display the images we wrote to Cloud Storage
    // in the browser.
    PrintWriter out = resp.getWriter();
    out.println("<html><body>\n");
    out.println("<img src='//storage.cloud.google.com/" + bucket
        + "/image.jpeg' alt='AppEngine logo' />");
    out.println("<img src='//storage.cloud.google.com/" + bucket
        + "/resizedImage.jpeg' alt='AppEngine logo resized' />");
    out.println("<img src='//storage.cloud.google.com/" + bucket
        + "/rotatedImage.jpeg' alt='AppEngine logo rotated' />");
    out.println("</body></html>\n");
                  //}
                  //}
                  }
}
// [END example]
