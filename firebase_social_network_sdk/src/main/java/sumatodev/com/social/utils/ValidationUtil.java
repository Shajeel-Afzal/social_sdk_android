/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package sumatodev.com.social.utils;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sumatodev.com.social.Constants;

/**
 * Created by Kristina on 8/8/15.
 */
public class ValidationUtil {
    private static final String[] IMAGE_TYPE = new String[]{"jpg", "png", "jpeg", "bmp", "jp2", "psd", "tif", "gif"};

    public static boolean isEmailValid(String email) {
        String stricterFilterString = "[A-Z0-9a-z\\._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}";
        return email.matches(stricterFilterString);
    }

    public static boolean isOnlyLatinLetters(String text) {
        String regular = "[a-zA-Z]+";
        return text.matches(regular);
    }

    public static boolean isYouTubeUrl(String url) {
        String regular = "http(?:s?):\\/\\/(?:www\\.)?youtu(?:be\\.com\\/watch\\?v=|\\.be\\/)([\\w\\-\\_]*)(&(amp;)?\u200C\u200B[\\w\\?\u200C\u200B=]*)?";
        return url.matches(regular);
    }

    public static String getYoutubeThumbnailUrlFromVideoUrl(String videoUrl) {
        return "http://img.youtube.com/vi/"+getYoutubeVideoIdFromUrl(videoUrl) + "/0.jpg";
    }

    public static String getYoutubeVideoIdFromUrl(String inUrl) {
        if (inUrl.toLowerCase().contains("youtu.be")) {
            return inUrl.substring(inUrl.lastIndexOf("/") + 1);
        }
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(inUrl);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static boolean isMonthValid(String monthString) {
        if (monthString != null) {
            if (monthString.length() != 2) {
                return false;
            }

            int month = Integer.valueOf(monthString);
            if (month >= 1 && month <= 12) {
                return true;
            }
        }
        return false;
    }

    public static boolean isYearValid(String monthString) {
        if (monthString != null) {
            if (monthString.length() == 2) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPostTitleValid(String name) {
        return name.length() <= Constants.Post.MAX_POST_TITLE_LENGTH;
    }

    public static boolean isNameValid(String name) {
        return name.length() <= Constants.Profile.MAX_NAME_LENGTH;
    }

    public static boolean isImage(Uri uri, Context context) {
        String mimeType = context.getContentResolver().getType(uri);

        if (mimeType != null) {
            return mimeType.contains("image");
        } else {
            String filenameArray[] = uri.getPath().split("\\.");
            String extension = filenameArray[filenameArray.length - 1];

            if (extension != null) {
                for (String type : IMAGE_TYPE) {
                    if (type.toLowerCase().equals(extension.toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean hasExtension(String path) {
        String extension = null;
        String filenameArray[] = path.split("\\.");

        if (filenameArray.length > 1) {
            extension = filenameArray[filenameArray.length - 1];
        }

        if (extension != null) {
            return true;
        }
        return false;
    }

    public static boolean containsInvalidSymbol(String name) {
        return name.contains("@");
    }

    public static void containUrl(String string) {

        String[] parts = string.split("\\s+");

        // get every part
        for (String item : parts) {
            if (urlPattern.matcher(item).matches()) {
                //it's a good url
                System.out.print("<a href=\"" + item + "\">" + item + "</a> ");
            } else {
                // it isn't a url
                System.out.print(item + " ");
            }
        }
    }

    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public static boolean checkImageMinSize(Rect rect) {
        return rect.height() >= Constants.Profile.MIN_AVATAR_SIZE && rect.width() >= Constants.Profile.MIN_AVATAR_SIZE;
    }
}
