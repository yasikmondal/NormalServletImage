
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
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
                

                String bucket = null;
                String thumbnailDestinationFolder[] = null;
                String productDetailDestinationFolder [] = null;
                String productSmallDestinationFolder [] = null;
                String bannerDestinationFolder []= null;
                  
                  

                  // [START gcs]
                  
                  //Allows creating and accessing files in Google Cloud Storage.
                  private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
                      .initialRetryDelayMillis(50)
                      .retryMaxAttempts(50)
                      .totalRetryPeriodMillis(150000)
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
                  
                  public static List<StorageObject> listBucket(String bucketName) throws IOException, GeneralSecurityException {
                                                Storage client = StorageFactory.getService();
                                                Storage.Objects.List listRequest = client.objects().list(bucketName);

                                                List<StorageObject> results = new ArrayList<StorageObject>();
                                                Objects objects;

                                                // Iterate through each page of results, and add them to our results
                                                // list.
                                                do {
                                                                objects = listRequest.execute();
                                                                // Add the items in this page of results to the list we'll return.
                                                                results.addAll(objects.getItems());

                                                                // Get the next page, in the next iteration of this loop.
                                                                listRequest.setPageToken(objects.getNextPageToken());
                                                } while (null != objects.getNextPageToken());

                                                return results;
                                }
                  
                 

                  @SuppressWarnings("resource")
                @Override
                  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                                  
                                String [] thumbnail =null;
                                String [] productDetail = null;
                                String [] productSmall = null;
                                String [] banner = null;
                                
                                List<StorageObject> bucketContents = null;
                                try {
                                                File file = new File("WEB-INF/application.properties");
                                                FileInputStream fileInput = new FileInputStream(file);
                                                Properties properties = new Properties();
                                                properties.load(fileInput);
                                                
                                                bucket = properties.getProperty("bucket");
                                                
                                                String thumbnailDestinationFolderString = properties.getProperty("thumbnailDestinationFolder");
                                                thumbnailDestinationFolder = thumbnailDestinationFolderString.split(",");
                                                
                                                String productDetailDestinationFolderString = properties.getProperty("productDetailDestinationFolder");
                                                productDetailDestinationFolder = productDetailDestinationFolderString.split(",");
                                                
                                                String productSmallDestinationFolderString = properties.getProperty("productSmallDestinationFolder");
                                                productSmallDestinationFolder = productSmallDestinationFolderString.split(",");
                                                
                                                String bannerDestinationFolderString = properties.getProperty("bannerDestinationFolder");
                                                bannerDestinationFolder = bannerDestinationFolderString.split(",");
                                                
                                                
                                                String thumbnailString = properties.getProperty("thumbnail");
                                                thumbnail = thumbnailString.split(",");
                                                
                                                String productDetailString = properties.getProperty("productDetail");
                                                productDetail = productDetailString.split(",");
                                                
                                                String productSmallString = properties.getProperty("productSmall");
                                                productSmall = productSmallString.split(",");
                                                
                                                String bannerString = properties.getProperty("banner");
                                                banner = bannerString.split(",");

                
                                                fileInput.close();
                                                bucketContents = listBucket(bucket);
                                } catch (GeneralSecurityException e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                } catch (IOException e) {
                                                e.printStackTrace();
                                }

                                  
                                  ImagesService imagesService = ImagesServiceFactory.getImagesService();
                                  
                                  if (null == bucketContents) {
                                                                System.out.println("There were no objects in the given bucket; try adding some and re-running.");
                                                }

                                                for (StorageObject object : bucketContents) {
                                                                
                                                                String objectName = object.getName();
                                                if(objectName.endsWith(".png") || objectName.endsWith(".jpg")){
                                                                objectName = objectName.substring(7, (objectName.length()-4));
                                                                System.out.println(objectName);
                                                }else if(objectName.endsWith(".jpeg")){
                                                                objectName = objectName.substring(7, (objectName.length()-5));
                                                                System.out.println(objectName);
                                                }
                                                                
                                                                if ("image/png".equals(object.getContentType())) {
                                                                                
                                                                                
                                                                
                                                                
                                                
                      // Create a temp file to upload
                     // Path tempPath = Files.createTempFile("StorageSample", "txt");
                     // Files.write(tempPath, "Sample file".getBytes());
                     // File tempFile = tempPath.toFile();
                     // tempFile.deleteOnExit();
                      
                                  //uploadFile(TEST_FILENAME, "image/png", tempFile, bucketName);
                                  
                    //[START rotate]
                    // Make an image from a Cloud Storage object, and transform it.
                    
                    //BlobstoreService allows you to manage the creation and serving of large, immutable blobs to users. 
                    System.out.println("Test3");
                    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
                    BlobKey blobKey = blobstoreService.createGsBlobKey("/gs/" + bucket + "/" + object.getName()); // Creating a BlobKey for a Google Storage File.
                    //BlobKey blobKey = blobstoreService.createGsBlobKey("//storage.googleapis.com/" + bucket + "/Test/unnamed.jpg");
                    
                    Image blobImage = ImagesServiceFactory.makeImageFromBlob(blobKey); // Create an image backed by the specified blobKey.
                    
                    
                    // For Thumbnail
                    
                    for(int i=0, j=0; i< thumbnail.length; i++, j++){
                                
                                int width =Integer.parseInt(thumbnail[i]);
            int height = Integer.parseInt(thumbnail[i+1]);
            System.out.println(width + "X" + height);
            
            Transform resize1 = ImagesServiceFactory.makeResize(width, height);
                    Image resizeImage1 = imagesService.applyTransform(resize1, blobImage);

                    // Write the transformed image back to a Cloud Storage object.
                    gcsService.createOrReplace(
                        new GcsFilename(thumbnailDestinationFolder[j], objectName + "_"+width + "x" + height+ ".jpeg"),
                        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
                        ByteBuffer.wrap(resizeImage1.getImageData()));

                                i++;
                    }
                    
                    
                 // For productDetail
                    
                                                for(int i=0, j=0 ; i< productDetail.length; i++, j++){
                                                                                
                                                                                int width =Integer.parseInt(productDetail[i]);
                                                            int height = Integer.parseInt(productDetail[i+1]);
                                                            System.out.println(width + "X" + height);
                                                            
                                                            Transform resize_1_5 = ImagesServiceFactory.makeResize(width, height);
                                                                    Image resizeImage1_5 = imagesService.applyTransform(resize_1_5, blobImage);
                                                
                                                                    // Write the transformed image back to a Cloud Storage object.
                                                                    gcsService.createOrReplace(
                                                                        new GcsFilename(productDetailDestinationFolder[j], objectName + "_"+width + "x" + height+ ".jpeg"),
                                                                        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
                                                                        ByteBuffer.wrap(resizeImage1_5.getImageData()));
                                                
                                                                                i++;
                    }
                                                
                // For productSmall
                                                
                                                for(int i=0, j=0; i< productSmall.length; i++, j++){
                                                
                                                int width =Integer.parseInt(productSmall[i]);
                            int height = Integer.parseInt(productSmall[i+1]);
                            System.out.println(width + "X" + height);
                            
                            Transform resize2x = ImagesServiceFactory.makeResize(width, height);
                                    Image resizeImage2 = imagesService.applyTransform(resize2x, blobImage);
                
                                    // Write the transformed image back to a Cloud Storage object.
                                    gcsService.createOrReplace(
                                        new GcsFilename(productSmallDestinationFolder[j], objectName + "_"+width + "x" + height+ ".jpeg"),
                                        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
                                        ByteBuffer.wrap(resizeImage2.getImageData()));
                
                                                i++;
                                                }
                                                
                                                
                //For banner      
                                                
                                                                for(int i=0, j=0; i< banner.length; i++, j++){
                                                
                                                int width =Integer.parseInt(banner[i]);
                            int height = Integer.parseInt(banner[i+1]);
                            System.out.println(width + "X" + height);
                            
                            Transform resize3x = ImagesServiceFactory.makeResize(width, height);
                                    Image resizeImage3 = imagesService.applyTransform(resize3x, blobImage);
                
                                    // Write the transformed image back to a Cloud Storage object.
                                    gcsService.createOrReplace(
                                        new GcsFilename(bannerDestinationFolder[j], objectName + "_"+width + "x" + height+ ".jpeg"),
                                        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
                                        ByteBuffer.wrap(resizeImage3.getImageData()));
                
                                                i++;
                                                }
                                                                /*for(int i=0; i< sizes4x.length; i++){
                                                                
                                                                int width =(Integer)sizes4x[i];
                                            int height = (Integer)sizes4x[i+1];
                                            System.out.println(width + "X" + height);
                                            
                                            Transform resize4x = ImagesServiceFactory.makeResize(width, height);
                                                    Image resizeImage4 = imagesService.applyTransform(resize4x, blobImage);
                                
                                                    // Write the transformed image back to a Cloud Storage object.
                                                    gcsService.createOrReplace(
                                                        new GcsFilename(destinationFolder4x, "resizeImage_"+width + "x" + height+ ".jpeg"),
                                                        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
                                                        ByteBuffer.wrap(resizeImage4.getImageData()));
                                
                                                                i++;
                                                                }*/
                    
                    
                    
                    
                                                }
                                                }
                                  
                                  // close for loop
                                                
                                //[END rotate]
                    System.out.println("Test4");
                    // Output some simple HTML to display the images we wrote to Cloud Storage
                    // in the browser.
                    PrintWriter out = resp.getWriter();
                    out.println("<html><body>\n");
                    out.println("Converted Successfully !! Please check in cloud storage");
                                }


}
