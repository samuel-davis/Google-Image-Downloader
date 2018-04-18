package com.davis.google.image.downloader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This software was created for rights to this software belong to appropriate licenses and
 * restrictions apply.
 *
 * @author Samuel Davis created on 1/2/18.
 */
public class ImageDownloader {

  private static final String URL = "https://www.google.com/search?tbm=isch&q=";
  private static final Logger log =
      LoggerFactory.getLogger(ImageDownloader.class.getName().toString());
  /**
   * When you search for images, TBM=isch, you can also use the following TBS values:
   *
   * <p>Large images: tbs=isz:l Medium images: tbs=isz:m Icon sized images: tba=isz:i Image size
   * larger than 400×300: tbs=isz:lt,islt:qsvga Image size larger than 640×480: tbs=isz:lt,islt:vga
   * Image size larger than 800×600: tbs=isz:lt,islt:svga Image size larger than 1024×768:
   * tbs=isz:lt,islt:xga Image size larger than 1600×1200: tbs=isz:lt,islt:2mp Image size larger
   * than 2272×1704: tbs=isz:lt,islt:4mp Image sized exactly 1000×1000:
   * tbs=isz:ex,iszw:1000,iszh:1000 Images in full color: tbs=ic:color Images in black and white:
   * tbs=ic:gray Images that are red: tbs=ic:specific,isc:red [orange, yellow, green, teal, blue,
   * purple, pink, white, gray, black, brown] Image type Face: tbs=itp:face Image type Photo:
   * tbs=itp:photo Image type Clipart: tbs=itp:clipart Image type Line drawing: tbs=itp:lineart
   * Image type Animated (gif): tbs=itp:animated (thanks Dan) Group images by subject: tbs=isg:to
   * Show image sizes in search results: tbs=imgo:1
   *
   * <p>*
   */
  private static OkHttpClient client;

  public static void main(String[] args) {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder
        .interceptors()
        .add(
            new UserAgentInterceptor(
                "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.2 (KHTML, like Gecko) Chrome/22.0.1216.0 Safari/537.2"));
    client = builder.build();
    String query = "M1A1 Abrams Tank";
    File targetDir = new File(query.replaceAll(" ", "-"));
    targetDir.mkdirs();
    List<String> htmls = getHtmlFor1000FromGoogle(URL + query);
    List<String> urls = parseHtmlListForImages(htmls);
    String fileName = query.replaceAll(" ", "-") + "/" + query.replaceAll(" ", "-");
    int count = 0;
    for (String url : urls) {
      count = count + 1;
      try {
        downloadFileSync(url, fileName + String.valueOf(count) + ".jpg");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    log.info("Found {} urls ", urls.size());
  }

  public static List<String> getHtmlFor1000FromGoogle(String url) {
    List<String> htmlList = new ArrayList<>();
    htmlList.add(doGetRequest(url));
    htmlList.add(doGetRequest(url + "&start=100"));
    htmlList.add(doGetRequest(url + "&start=200"));
    htmlList.add(doGetRequest(url + "&start=300"));
    htmlList.add(doGetRequest(url + "&start=400"));
    htmlList.add(doGetRequest(url + "&start=500"));
    htmlList.add(doGetRequest(url + "&start=600"));
    htmlList.add(doGetRequest(url + "&start=700"));
    htmlList.add(doGetRequest(url + "&start=800"));
    htmlList.add(doGetRequest(url + "&start=900"));
    return htmlList;
  }

  public static List<String> parseHtmlListForImages(List<String> htmls) {
    List<String> urls = new ArrayList<>();
    for (String html : htmls) {
      Document doc = Jsoup.parse(html);
      Elements img = doc.getElementsByTag("img");
      for (Element e : img) {

        for (Attribute a : e.attributes()) {
          if (a.getKey().toString().equalsIgnoreCase("data-src")) {
            urls.add(a.getValue());
          }
        }
      }
    }
    return urls;
  }

  public static void downloadFileSync(String downloadUrl, String targetFile) throws Exception {
    Request request = new Request.Builder().url(downloadUrl).build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new IOException("Failed to download file: " + response);
    }
    try (FileOutputStream fos = new FileOutputStream(targetFile)) {
      fos.write(response.body().bytes());
      fos.close();
    }
  }

  public static String doGetRequest(String url) {
    Request request = new Request.Builder().url(url).build();

    Response response = null;
    String resultResponse = null;
    try {
      response = client.newCall(request).execute();
      resultResponse = response.body().string();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return resultResponse;
  }
}
