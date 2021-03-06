package grabber;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Collection of shared functions.
 */
public class shared {
    private static final String textEncoding = "UTF-8";
    private static final String NL = System.getProperty("line.separator");
    private static String htmlHead = "<!DOCTYPE html>" + NL + "<html lang=\"en\">" + NL + "<head>" + NL
            + "<meta charset=\"utf-8\" />" + NL + "</head>" + NL + "<body>" + NL;
    private static String htmlFoot = "</body>" + NL + "</html>";

    /**
     * Freeze thread for selected wait time.
     */
    static void sleep(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Processes a successful chapter.
     */
    private static void successfulChapter(String fileName, String chapterName, AutoNovel currGrab) {
        currGrab.successfulChapterNames.add(chapterName);
        currGrab.successfulFilenames.add(fileName);
        currGrab.gui.appendText(currGrab.window, "[INFO]" + chapterName + " saved.");
        currGrab.gui.updateProgress(currGrab.window);
        currGrab.gui.pagesCountLbl.setText(String.valueOf(currGrab.wordCount / 300));
    }

    /**
     * Prints potential failed chapters after chapter grabs.
     */
    static void report(AutoNovel currGrab) {
        if (currGrab.export.equals("EPUB")) {
            currGrab.gui.appendText(currGrab.window, "[INFO]Finished! "
                    + ((currGrab.successfulChapterNames.size()) - currGrab.failedChapters.size()) + " of "
                    + (currGrab.successfulChapterNames.size()) + " chapters successfully grabbed.");
        } else {
            currGrab.gui.appendText(currGrab.window, "[INFO]Finished! "
                    + ((currGrab.successfulChapterNames.size()) - currGrab.failedChapters.size()) + " of "
                    + (currGrab.successfulChapterNames.size()) + " chapters successfully grabbed.");
        }
        if (!currGrab.failedChapters.isEmpty()) {
            currGrab.gui.appendText(currGrab.window, "[ERROR]Failed to grab the following chapters:");
            for (String failedChapter : currGrab.failedChapters) {
                currGrab.gui.appendText(currGrab.window, failedChapter);
            }
        }
    }

    /**
     * Returns the image name without the href address/path
     */
    private static String getImageName(String src) {
        String imageName;
        int indexname = src.lastIndexOf("/");
        if (indexname == src.length()) {
            src = src.substring(1, indexname);
        }
        indexname = src.lastIndexOf("/");
        imageName = src.substring(indexname + 1);
        // Check against popular formats with regex
        if (imageName.contains(".png")) imageName = imageName.replaceAll("\\.png(.*)", ".png");
        else if (imageName.contains(".jpg")) imageName = imageName.replaceAll("\\.jpg(.*)", ".jpg");
        else if (imageName.contains(".gif")) imageName = imageName.replaceAll("\\.gif(.*)", ".gif");
        else return "could_not_rename_image";
        return imageName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * Adjust chapter names for chapter numeration.
     */
    private static String setFilename(int chapterNumber, String fileName) {
        return String.format("%05d", chapterNumber) + "-" + fileName.replaceAll("[^\\w]+", "-");
    }

    static void downloadImage(String src, AutoNovel currGrab) {
        if (!currGrab.imageLinks.contains(src)) {
            // Try to set the image name
            String name = getImageName(src);
            // If image could not be renamed correctly, the hashCode of the source + .jpg
            // will be set as the image name.
            if (name.equals("could_not_rename_image")) {
                name = src.hashCode() + ".jpg";
            }
            //For LiberSpark
            if (src.startsWith("//")) src = src.replace("//", "https://");
            try {
                // Connect to image source
                URL url = new URL(src);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                http.connect();
                InputStream input = http.getInputStream();
                byte[] buffer = new byte[4096];
                // Create imageLinks folder
                String filepath = currGrab.saveLocation + File.separator + "images";
                File dir = new File(filepath);
                if (!dir.exists()) dir.mkdirs();
                // Save image to file
                try (OutputStream output = new FileOutputStream(new File(filepath + File.separator + name))) {
                    int n;
                    while ((n = input.read(buffer)) != -1) {
                        output.write(buffer, 0, n);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    currGrab.gui.appendText(currGrab.window, e.getMessage());
                }
                currGrab.imageLinks.add(src);
                currGrab.imageNames.add(name);
                currGrab.gui.appendText(currGrab.window, "[INFO]" + name + " saved.");
                //General catch
            } catch (Throwable e) {
                e.printStackTrace();
                currGrab.gui.appendText(currGrab.window, "[ERROR]Failed to save " + name);
            }
        }
    }

    /**
     * Saves the autoNovel cover into a buffered Image
      */
    static BufferedImage getBufferedCover(String src, AutoNovel currGrab) {
        if (!currGrab.imageLinks.contains(src)) {
            // Try to set the image name
            String name = getImageName(src);
            // If image could not be renamed correctly, the hashCode of the source + .jpg
            // will be set as the image name.
            if (name.equals("could_not_rename_image")) {
                name = src.hashCode() + ".jpg";
            }
            //For LiberSpark
            if (src.startsWith("//")) src = src.replace("//", "https://");
            try {
                URL url = new URL(src);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                http.connect();
                BufferedImage imageInput = ImageIO.read(http.getInputStream());
                currGrab.bufferedCoverName = name;
                currGrab.imageLinks.add(src);
                currGrab.imageNames.add(name);
                return imageInput;
                //General catch
            } catch (Throwable e) {
                e.printStackTrace();
                currGrab.gui.appendText(currGrab.window, "[ERROR]Failed to get" + name);
            }
        }
        return null;
    }

    /**
     * Creates a 'Table of Contents' file of successfully grabbed chapters and imageLinks.
     * (Calibre needs links to the imageLinks to display them)
     */
    static void createToc(AutoNovel currGrab) {
        if (!currGrab.successfulChapterNames.isEmpty()) {
            String fileName = currGrab.tocFileName;
            String filePath = currGrab.saveLocation + File.separator + fileName;
            // Change the save location to path /chapters instead
            if (currGrab.export.equals("EPUB")) {
                filePath = currGrab.saveLocation + File.separator + "chapters" + File.separator + fileName;
            }
            try (PrintStream out = new PrintStream(filePath + ".html", textEncoding)) {
                out.print(htmlHead + "<b>Table of Contents</b>" + NL + "<p style=\"text-indent:0pt\">" + NL);
                //Print chapter links
                if (currGrab.export.equals("EPUB")) {
                    for (int i = 0; i < currGrab.successfulChapterNames.size(); i++) {
                        out.println("<a href=\""
                                + currGrab.successfulFilenames.get(i) + ".html\">" + currGrab.successfulChapterNames.get(i)
                                + "</a><br/>");
                    }
                } else {
                    for (int i = 0; i < currGrab.successfulChapterNames.size(); i++) {
                        out.println("<a href=\"chapters/" + currGrab.successfulFilenames.get(i) + ".html\">"
                                + currGrab.successfulChapterNames.get(i) + "</a><br/>");
                    }
                }
                //Print image links (for calibre)
                if (!currGrab.imageLinks.isEmpty() && !currGrab.export.equals("EPUB")) {
                    for (String image : currGrab.imageLinks) {
                        // Use hashCode of src + .jpg as the image name if renaming wasn't successful.
                        String imageName = getImageName(image);
                        if (imageName.equals("could_not_rename_image")) {
                            imageName = image.hashCode() + ".jpg";
                        }
                        out.println("<img src=\"images/" + imageName + "\" style=\"display:none;\" /><br/>");
                    }
                }
                out.print("</p>" + NL + htmlFoot);
                currGrab.successfulExtraPagesNames.add("Table of Contents");
                currGrab.successfulExtraPagesFilenames.add(fileName);
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                currGrab.gui.appendText(currGrab.window, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    static void createCoverPage(AutoNovel currGrab) {
        String fileName = "coverPage";
        String filePath = currGrab.saveLocation + File.separator + "chapters" + File.separator + fileName;
        String imageName = currGrab.bookCover;
        imageName = getFileName(imageName);
        try (PrintStream out = new PrintStream(filePath + ".html", textEncoding)) {
            out.print(htmlHead + "<div class=\"cover\" style=\"padding: 0pt; margin:0pt; text-align: center; padding:0pt; margin: 0pt;\">" + NL);
            out.println("<img src=\"" + imageName + "\" class=\"cover.img\" style=\"width: 600px; height: 800px;\" />");
            out.print("</div>" + NL + htmlFoot);
            currGrab.successfulExtraPagesNames.add("Cover");
            currGrab.successfulExtraPagesFilenames.add(fileName);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            currGrab.gui.appendText(currGrab.window, e.getMessage());
            e.printStackTrace();
        }
    }

    static void createDescPage(AutoNovel currGrab) {
        String fileName = "descPage";
        String filePath = currGrab.saveLocation + File.separator + "chapters" + File.separator + fileName;
        try (PrintStream out = new PrintStream(filePath + ".html", textEncoding)) {
            out.print(htmlHead + "<div><b>Description<b>" + NL);
            out.println("<p>" + currGrab.bookDesc.get(0) + "</p>");
            out.print("</div>" + NL + htmlFoot);
            currGrab.successfulExtraPagesNames.add("Description");
            currGrab.successfulExtraPagesFilenames.add(fileName);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            currGrab.gui.appendText(currGrab.window, e.getMessage());
            e.printStackTrace();
        }
    }

    static String getFileName(String imageName) {
        if (imageName != null && imageName.contains("/"))
            imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
        if (imageName != null && imageName.contains("\\"))
            imageName = imageName.substring(imageName.lastIndexOf("\\") + 1);
        return imageName;
    }

    /**
     * Main method to save chapter content.
     */
    static void saveChapterWithHTML(String url, int chapterNumber, String chapterName, String chapterContainer, AutoNovel currGrab) {
        //Manual grabbing got it's own file naming method
        String fileName = setFilename(chapterNumber, chapterName);

        try {
            Document doc;
            if (currGrab.window.equals("auto") && currGrab.currHostSettings.host.equals("https://wattpad.com/")) {
                doc = Jsoup.connect(xhrRequest.wattpadGetChapterTextURL(url.substring(24, url.indexOf("-")))).timeout(30 * 1000).get();
            } else {
                doc = Jsoup.connect(url).timeout(30 * 1000).get();
            }

            // Remove styling
            if (currGrab.noStyling) doc.select("[style]").removeAttr("style");

            // Getting the next chapter URL from the "nextChapterBtn" href for Chapter-To-Chapter.
            if (!currGrab.nextChapterBtn.equals("NOT_SET"))
                currGrab.nextChapterURL = doc.select(currGrab.nextChapterBtn).first().absUrl("href");

            Element chapterContent = doc.select(chapterContainer).first();
            if (chapterContent == null)
                throw new NullPointerException("Chapter container (" + chapterContainer + ") not found.");

            if (currGrab.window.equals("auto") && currGrab.currHostSettings.host.equals("https://flying-lines.com/")) {
                String tempChapterText = "<div>" + doc.select("p").toString() + "</div>";
                chapterContent = Jsoup.parse(tempChapterText);
            }

            // Remove unwanted tags from chapter container.
            if (currGrab.blacklistedTags != null && !currGrab.blacklistedTags.isEmpty()) {
                for (String tag : currGrab.blacklistedTags) {
                    if (!chapterContent.select(tag).isEmpty()) {
                        chapterContent.select(tag).remove();
                    }
                }
            }
            // AutoNovel images of chapter container.
            if (currGrab.getImages) {
                for (Element image : chapterContent.select("img")) {
                    downloadImage(image.absUrl("src"), currGrab);
                }
            }
            // Add word count of chapter to total word count
            if (currGrab.window.equals("auto")) {
                currGrab.wordCount = currGrab.wordCount + getWordCount(chapterContent.toString());
            }
            // Create chapters folder if it doesn't exist.
            File dir = new File(currGrab.saveLocation + File.separator + "chapters");
            if (!dir.exists()) dir.mkdirs();
            try (PrintStream out = new PrintStream(dir.getPath() + File.separator + fileName + ".html", textEncoding)) {
                if (chapterContent.select("img").size() > 0) {
                    // Iterate each image in chapter content.
                    for (Element image : chapterContent.select("img")) {
                        // Check if the image was successfully downloaded.
                        String src = image.absUrl("src");
                        if (currGrab.imageLinks.contains(src)) {
                            // Use hashCode of src + .jpg as the image name if renaming wasn't successful.
                            String imageName = getImageName(image.attr("src"));
                            if (imageName.equals("could_not_rename_image")) {
                                imageName = src.hashCode() + ".jpg";
                            }
                            // Replace href for image to point to local path.
                            image.attr("src", imageName);
                            // Remove the img tag if image wasn't downloaded.
                        } else chapterContent.select("img").remove();
                    }
                }
                // Write text content to file.
                if (currGrab.displayChapterTitle)
                    chapterContent.prepend("<span style=\"font-weight: 700; text-decoration: underline;\">" + chapterName + "</span><br>");
                out.println(chapterContent);
            }
            successfulChapter(fileName, chapterName, currGrab);
        } catch (Throwable e) {
            currGrab.failedChapters.add(chapterName);
            currGrab.gui.appendText(currGrab.window, "[ERROR]" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save chapter content from string
     */
    static void saveChapterFromString(String chapterContentString, int chapterNumber, String chapterName, String chapterContainer, AutoNovel currGrab) {

        if (chapterContentString == null)
            throw new NullPointerException("Chapter container (" + chapterContainer + ") not found.");

        //Manual grabbing got it's own file naming method
        String fileName = setFilename(chapterNumber, chapterName);

        try {
            Document doc = Jsoup.parse(chapterContentString);
            // Remove unwanted tags from chapter container.
            if (currGrab.blacklistedTags != null && !currGrab.blacklistedTags.isEmpty()) {
                for (String tag : currGrab.blacklistedTags) {
                    if (!doc.select(tag).isEmpty()) {
                        doc.select(tag).remove();
                    }
                }
            }
            // Remove styling
            if (currGrab.noStyling) doc.select("[style]").removeAttr("style");

            // Getting the next chapter URL from the "nextChapterBtn" href for Chapter-To-Chapter.
            if (!currGrab.nextChapterBtn.equals("NOT_SET"))
                currGrab.nextChapterURL =
                        doc.select(currGrab.nextChapterBtn).first().absUrl("href");

            Element chapterContent = doc.select(chapterContainer).first();
            if (currGrab.currHostSettings.host.equals("https://tapread.com/")) chapterContent = doc;

            // AutoNovel images of chapter container.
            if (currGrab.getImages) {
                for (Element image : chapterContent.select("img")) {
                    downloadImage(image.absUrl("src"), currGrab);
                }
            }
            // Add word count of chapter to total word count
            currGrab.wordCount = currGrab.wordCount + getWordCount(chapterContent.toString());
            // Create chapters folder if it doesn't exist.
            File dir = new File(currGrab.saveLocation + File.separator + "chapters");
            if (!dir.exists()) dir.mkdirs();
            try (PrintStream out = new PrintStream(dir.getPath() + File.separator + fileName + ".html", textEncoding)) {
                if (chapterContent.select("img").size() > 0) {
                    // Iterate each image in chapter content.
                    for (Element image : chapterContent.select("img")) {
                        // Check if the image was successfully downloaded.
                        String src = image.absUrl("src");
                        if (currGrab.imageLinks.contains(src)) {
                            // Use hashCode of src + .jpg as the image name if renaming wasn't successful.
                            String imageName = getImageName(image.attr("src"));
                            if (imageName.equals("could_not_rename_image")) {
                                imageName = src.hashCode() + ".jpg";
                            }
                            // Replace href for image to point to local path.
                            image.attr("src", imageName);
                            // Remove the img tag if image wasn't downloaded.
                        } else chapterContent.select("img").remove();
                    }
                }
                // Add chapter title at the start of chapter
                if (currGrab.displayChapterTitle)
                    chapterContent.prepend("<span style=\"font-weight: 700; text-decoration: underline;\">" + chapterName + "</span><br>");
                // Write text content to file.
                out.println(chapterContent);
            }
            successfulChapter(fileName, chapterName, currGrab);
        } catch (Throwable e) {
            currGrab.failedChapters.add(chapterName);
            currGrab.gui.appendText(currGrab.window, "[ERROR]" + e.getMessage());
            e.printStackTrace();
        }
    }

    // Utility
    static int ordinalIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1)
            pos = str.indexOf(substr, pos + 1);
        return pos;
    }

    public static void deleteFolderAndItsContent(final Path folder) throws IOException {
        Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    private static int getWordCount(String html) {
        org.jsoup.nodes.Document dom = Jsoup.parse(html);
        String text = dom.text();

        return text.split(" ").length;
    }
}