package in.tech_camp.pictweet.entity;

import lombok.Data;

@Data
public class CommentEntity {
  private Integer id;
  private String text;
  private UserEntity user;  // usersテーブルとのアソシエーション
  private TweetEntity tweet;  // tweetsテーブルとのアソシエーション
}