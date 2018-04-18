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

package sumatodev.com.social.model;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sumatodev.com.social.enums.ItemType;
import sumatodev.com.social.utils.FormatterUtil;
import sumatodev.com.social.views.mention.Mentionable;

public class Comment implements Serializable, LazyLoading {

    private String id;
    private String postId;
    private String text;
    private String authorId;
    private long likesCount;
    private long createdDate;
    private ItemType itemType;

    private List<Mentionable> mentions;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


    public Comment() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
        this.createdDate = Calendar.getInstance().getTimeInMillis();
        itemType = ItemType.ITEM;
    }

    public Comment(ItemType itemType) {

        this.itemType = itemType;
        setId(itemType.toString());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public List<Mentionable> getMentions() {
        return mentions;
    }

    public void setMentions(List<Mentionable> mentions) {
        this.mentions = mentions;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("id", id);
        result.put("postId", postId);
        result.put("text", text);
        result.put("authorId", authorId);
        result.put("likesCount", likesCount);
        result.put("createdDate", createdDate);
        result.put("mentions", mentions);

        return result;
    }


    @Override
    public ItemType getItemType() {
        return itemType;
    }

    @Override
    public void setItemType(ItemType itemType) {

    }
}
